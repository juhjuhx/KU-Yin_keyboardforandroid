"""P0 英文資料的來源與擷取規則，不依賴 Android 或網路。"""

import hashlib
import importlib.util
from pathlib import Path
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("english_converter", ROOT / "scripts/generate_english_lexicon.py")
CONVERTER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CONVERTER)


class EnglishLexiconDataTest(unittest.TestCase):
    def test_filters_unsafe_nonword_case_and_nested_entries(self):
        text = "\n".join([
            "dictionary=main:en,version=54",
            " word=hello,f=120,flags=,originalFreq=100",
            " word=held,f=158,flags=",
            " word=helen,f=120,flags=",
            " word=hello,f=110,flags=",
            " word=bad,f=130,flags=offensive",
            " word=zero,f=0,flags=",
            " word=alias,f=150,flags=,not_a_word=true",
            " word=Name,f=170,flags=",
            " word=can't,f=180,flags=",
            "  word=nested,f=200,flags=",
        ])
        self.assertEqual([("held", 158), ("helen", 120), ("hello", 120)], CONVERTER.extract_entries(text))

    def test_wrong_source_hash_cannot_overwrite_output(self):
        with tempfile.TemporaryDirectory() as folder:
            source = Path(folder) / "source.gz"
            output = Path(folder) / "output.tsv"
            source.write_bytes(b"not the pinned AOSP file")
            output.write_bytes(b"existing")
            with self.assertRaisesRegex(ValueError, "SHA256"):
                CONVERTER.generate(source, output)
            self.assertEqual(b"existing", output.read_bytes())

    def test_vendored_dictionary_is_exact_pinned_10k_and_contains_real_completion(self):
        data = (ROOT / "app/src/main/assets/english/word_frequency.tsv").read_bytes()
        self.assertEqual(CONVERTER.OUTPUT_SHA256, hashlib.sha256(data).hexdigest())
        self.assertEqual(10_000, len(data.splitlines()))
        self.assertIn(b"hello\t120\n", data)
        self.assertIn(b"project\t148\n", data)
        self.assertTrue(data.startswith(b"the\t222\nto\t215\n"))


if __name__ == "__main__":
    unittest.main()
