#!/usr/bin/env python3
"""Offline, deterministic donation QR assets; no network or Android runtime dependency.

Regenerate with opencv-python-headless==4.13.0.92 and numpy==2.4.6.
OpenCV encoder: Apache-2.0; Python packaging wrapper: MIT. See QR_PROVENANCE.md.
"""
import argparse
import hashlib
import importlib.metadata
import json
from pathlib import Path

URLS = {
    "fsf": "https://my.fsf.org/donate",
    "asf": "https://donate.apache.org/",
    "osi": "https://opensource.org/donate",
    "eff": "https://eff.org/donate",
}


def generate(root: Path, check: bool) -> None:
    import cv2
    import numpy as np

    if (cv2.__version__, np.__version__) != ("4.13.0", "2.4.6"):
        raise SystemExit("Refusing unreviewed encoder version; use pinned offline toolchain")
    wheel = importlib.metadata.version("opencv-python-headless")
    if wheel != "4.13.0.92":
        raise SystemExit("Refusing unreviewed OpenCV wheel")
    parameters = cv2.QRCodeEncoder_Params()
    parameters.mode = cv2.QRCodeEncoder_MODE_BYTE
    parameters.correction_level = cv2.QRCodeEncoder_CORRECT_LEVEL_M
    parameters.version = 0  # smallest sufficient QR version
    encoder = cv2.QRCodeEncoder_create(parameters)
    outputs = {}
    entries = []
    for identity, url in URLS.items():
        encoded = encoder.encode(url)
        # Pinned OpenCV emits two white modules. Verify before replacing with four.
        if not (np.all(encoded[:2] == 255) and np.all(encoded[-2:] == 255)
                and np.all(encoded[:, :2] == 255) and np.all(encoded[:, -2:] == 255)):
            raise SystemExit("Unexpected OpenCV border; refusing ambiguous quiet zone")
        symbol = encoded[2:-2, 2:-2]
        modules = int(symbol.shape[0])
        if not (21 <= modules <= 177 and (modules - 21) % 4 == 0):
            raise SystemExit("Invalid standard QR symbol size")
        if symbol.shape != (modules, modules) or not np.isin(symbol, [0, 255]).all():
            raise SystemExit("Unexpected QR symbol shape or colors")
        image = np.repeat(np.repeat(np.pad(symbol, 4, constant_values=255), 8, axis=0), 8, axis=1)
        ok, png = cv2.imencode(".png", image, [cv2.IMWRITE_PNG_COMPRESSION, 9])
        if not ok:
            raise SystemExit("PNG encode failed")
        data = png.tobytes()
        relative = f"app/src/main/res/drawable-nodpi/foundation_qr_{identity}.png"
        outputs[relative] = data
        entries.append({"id": identity, "url": url, "drawable": f"foundation_qr_{identity}",
                        "source_path": relative, "symbol_modules": modules,
                        "width_px": int(image.shape[1]), "height_px": int(image.shape[0]),
                        "sha256": hashlib.sha256(data).hexdigest()})
    manifest = {
        "schema_version": 1,
        "generator": "scripts/generate_foundation_qr.py",
        "generator_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
        "encoder": {"library": "OpenCV", "version": cv2.__version__,
                    "wheel": f"opencv-python-headless=={wheel}", "numpy": np.__version__,
                    "upstream_license": "Apache-2.0", "wrapper_license": "MIT"},
        "encoding": "BYTE", "error_correction": "M", "quiet_zone_modules": 4,
        "pixels_per_module": 8, "png_compression": 9,
        "provenance": "Project-generated from public official URLs; not foundation-provided artwork or endorsement",
        "hash_scope": "Source PNG bytes; Android resource packaging may transform PNG encoding",
        "phone_camera_scan": "NOT_TESTED", "foundations": entries,
    }
    outputs["app/src/main/assets/foundation_qr_manifest.json"] = (json.dumps(
        manifest, ensure_ascii=False, indent=2) + "\n").encode()
    for relative, data in outputs.items():
        path = root / relative
        if check:
            if not path.is_file() or path.read_bytes() != data:
                raise SystemExit(f"Generated asset mismatch: {relative}")
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
        print(("VERIFIED " if check else "WROTE ") + relative)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Verify exact regeneration without writing")
    args = parser.parse_args()
    generate(Path(__file__).resolve().parents[1], args.check)
