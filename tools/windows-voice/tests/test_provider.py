import json
import io
import sys
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from provider import ApiSettings, transcribe_api, build_multipart, validate_endpoint


class FakeResponse:
    def __init__(self, body):
        self.body = body
    def __enter__(self):
        return self
    def __exit__(self, *args):
        pass
    def read(self, count):
        return self.body[:count]


class ApiProviderTests(unittest.TestCase):
    def test_http_rejected_unless_loopback(self):
        with self.assertRaises(ValueError):
            validate_endpoint("http://example.com/v1/audio/transcriptions")
        self.assertEqual(validate_endpoint("http://127.0.0.1:8000/v1/audio/transcriptions"),
                         "http://127.0.0.1:8000/v1/audio/transcriptions")

    def test_rejects_embedded_credentials_and_bad_scheme(self):
        for target in ["file:///etc/passwd", "https://u:p@example.com/path", "https://example.com/a#x"]:
            with self.subTest(target=target), self.assertRaises(ValueError):
                validate_endpoint(target)

    def test_multipart_contains_exact_wav_and_fields(self):
        wav = b"RIFF\x00abcd"
        body, content_type = build_multipart(wav, "tw-test", "zh", "TESTBOUNDARY")
        self.assertIn(wav, body)
        self.assertIn(b'name="file"; filename="audio.wav"', body)
        self.assertIn(b'name="model"', body)
        self.assertIn(b"tw-test", body)
        self.assertIn(b'name="language"', body)
        self.assertIn(b"zh", body)
        self.assertIn("boundary=TESTBOUNDARY", content_type)

    def test_remote_api_returns_text(self):
        with TemporaryDirectory() as td:
            p = Path(td) / "sample.wav"
            p.write_bytes(b"RIFF-privacy-test")
            def fake_open(req, timeout):
                self.assertEqual(timeout, 20)
                self.assertEqual(req.get_header("Authorization"), "Bearer secret")
                self.assertEqual(req.get_method(), "POST")
                self.assertIn(b"RIFF-privacy-test", req.data)
                return FakeResponse(json.dumps({"text": "臺灣注音"}).encode())
            s = ApiSettings(endpoint="https://example.com/v1/audio/transcriptions",
                            model="whisper-1", api_key="secret", timeout=20)
            self.assertEqual(transcribe_api(p, s, opener=fake_open), "臺灣注音")

    def test_api_error_not_echo_response_or_key(self):
        with TemporaryDirectory() as td:
            p = Path(td) / "sample.wav"
            p.write_bytes(b"RIFF")
            s = ApiSettings(endpoint="https://example.com/v1/audio/transcriptions", model="m",
                            api_key="super-secret")
            def fake_open(req, timeout):
                return FakeResponse(b'{"error":"private transcript super-secret"}')
            with self.assertRaisesRegex(RuntimeError, "API response missing text") as e:
                transcribe_api(p, s, opener=fake_open)
            self.assertNotIn("super-secret", str(e.exception))


if __name__ == "__main__":
    unittest.main()
