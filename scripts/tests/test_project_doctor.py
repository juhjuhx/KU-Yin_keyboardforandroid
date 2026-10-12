"""Doctor must inspect the same native staging location as bootstrap/CMake."""
import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location(
    "project_doctor", Path(__file__).resolve().parents[1] / "project_doctor.py"
)
doctor = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(doctor)


class ProjectDoctorTest(unittest.TestCase):
    def native_status(self, root):
        output = io.StringIO()
        with patch.object(doctor, "ROOT", root), patch.object(
            doctor.shutil, "which", return_value=None
        ), contextlib.redirect_stdout(output):
            doctor.main()
        return next(line.split()[0] for line in output.getvalue().splitlines()
                    if "native .deps" in line)

    def test_bootstrap_native_stage_is_reported_ready(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "app/src/main/cpp/.deps/libchewing").mkdir(parents=True)
            self.assertEqual("OK", self.native_status(root))

    def test_obsolete_app_deps_does_not_hide_missing_stage(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "app/.deps").mkdir(parents=True)
            self.assertEqual("MISSING", self.native_status(root))


if __name__ == "__main__":
    unittest.main()
