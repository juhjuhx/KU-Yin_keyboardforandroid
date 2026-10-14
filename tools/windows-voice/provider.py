"""Opt-in API ASR transport. Voice bytes leave the machine only when selected.

This module has no dependency on Fcitx5, Rime or Android runtime.
"""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from urllib.parse import urlsplit
from urllib.request import Request, urlopen
import json
import uuid


@dataclass(frozen=True)
class ApiSettings:
    endpoint: str
    model: str
    api_key: str = ""
    timeout: int = 120
    language: str = "zh"


def validate_endpoint(url: str) -> str:
    parsed = urlsplit(url)
    if (parsed.username is not None or parsed.password is not None or
            parsed.fragment or not parsed.hostname or
            parsed.scheme not in ("http", "https")):
        raise ValueError("ASR endpoint must be an HTTPS URL without credentials or fragment")
    if parsed.scheme == "http" and parsed.hostname not in {"localhost", "127.0.0.1", "::1"}:
        raise ValueError("Unencrypted ASR transport is restricted to loopback")
    if not parsed.path:
        raise ValueError("ASR endpoint requires an absolute API path")
    return url


def build_multipart(wav: bytes, model: str, language: str, boundary: str):
    if not model or "\r" in model or "\n" in model:
        raise ValueError("Invalid ASR model")
    if language not in {"zh", "en", "auto"}:
        raise ValueError("Invalid ASR language")
    out = bytearray()
    for field, value in (("model", model), ("language", language)):
        if field == "language" and value == "auto":
            continue
        out += ("--" + boundary + "\r\n"
                + 'Content-Disposition: form-data; name="' + field + '"\r\n\r\n'
                + value + "\r\n").encode("utf-8")
    out += ("--" + boundary + "\r\n"
            + 'Content-Disposition: form-data; name="file"; filename="audio.wav"\r\n'
            + "Content-Type: audio/wav\r\n\r\n").encode("utf-8")
    out += wav
    out += ("\r\n--" + boundary + "--\r\n").encode("utf-8")
    return bytes(out), "multipart/form-data; boundary=" + boundary


def transcribe_api(wav_path: Path, settings: ApiSettings, opener=urlopen) -> str:
    endpoint = validate_endpoint(settings.endpoint)
    if settings.timeout <= 0 or settings.timeout > 300:
        raise ValueError("ASR timeout must be 1..300 seconds")
    if wav_path.stat().st_size > 30 * 1024 * 1024:
        raise ValueError("Audio too large for single ASR upload")
    boundary = "ku-yin-" + uuid.uuid4().hex
    data, content_type = build_multipart(
        wav_path.read_bytes(), settings.model, settings.language, boundary)
    headers = {"Content-Type": content_type, "Accept": "application/json"}
    if settings.api_key:
        headers["Authorization"] = "Bearer " + settings.api_key
    request = Request(endpoint, data=data, headers=headers, method="POST")
    try:
        with opener(request, timeout=settings.timeout) as response:
            raw = response.read(5 * 1024 * 1024 + 1)
    except Exception as exc:
        # Never surface remote error bodies: they may contain audio/transcripts/secrets.
        raise RuntimeError("ASR transport failed (" + type(exc).__name__ + ")") from None
    if len(raw) > 5 * 1024 * 1024:
        raise RuntimeError("ASR response too large")
    try:
        payload = json.loads(raw)
    except (ValueError, UnicodeError):
        raise RuntimeError("Invalid ASR JSON") from None
    if not isinstance(payload, dict) or not isinstance(payload.get("text"), str):
        raise RuntimeError("API response missing text")
    return payload["text"].strip()
