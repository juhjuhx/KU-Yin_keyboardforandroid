#!/usr/bin/env python3
"""由固定 AOSP 英文詞頻檔重建 10k 離線 TSV；不連線、不修改上游詞頻。"""

import argparse
import gzip
import hashlib
from pathlib import Path
import re

UPSTREAM_REVISION = "127336e9f29d69607eab55982324b210279ae8c5"
UPSTREAM_URL = (
    "https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/"
    + UPSTREAM_REVISION
    + "/dictionaries/en_wordlist.combined.gz?format=TEXT"
)
UPSTREAM_GZIP_SHA256 = "07682388185c285d307e341d1733331af8699f735b4137e9f22571017fab69d2"
OUTPUT_SHA256 = "e44d3b87e830de6e57cc493f3cdb090404a477781299648464b2824e3b488e97"
ENTRY_LIMIT = 10_000
DEFAULT_OUTPUT = Path(__file__).resolve().parents[1] / "app/src/main/assets/english/word_frequency.tsv"


def extract_entries(text: str) -> list[tuple[str, int]]:
    frequencies: dict[str, int] = {}
    for line in text.splitlines():
        # A single indentation level identifies unigrams, never nested shortcuts/bigrams.
        match = re.match(r"^ word=([a-z]+),f=([0-9]+),flags=([^,]*)(?:,|$)", line)
        if match is None:
            continue
        word, frequency, flags = match.groups()
        score = int(frequency)
        attributes = dict(part.split("=", 1) for part in line.strip().split(",") if "=" in part)
        if flags or not 1 <= score <= 255 or attributes.get("not_a_word") == "true":
            continue
        frequencies[word] = max(score, frequencies.get(word, 0))
    return sorted(frequencies.items(), key=lambda item: (-item[1], item[0]))[:ENTRY_LIMIT]


def generate(source: Path, output: Path) -> None:
    source_bytes = source.read_bytes()
    if hashlib.sha256(source_bytes).hexdigest() != UPSTREAM_GZIP_SHA256:
        raise ValueError("來源 gzip SHA256 不符固定 AOSP 版本")
    entries = extract_entries(gzip.decompress(source_bytes).decode("utf-8"))
    data = "".join(f"{word}\t{frequency}\n" for word, frequency in entries).encode("utf-8")
    if len(entries) != ENTRY_LIMIT or hashlib.sha256(data).hexdigest() != OUTPUT_SHA256:
        raise ValueError("擷取結果與固定的 10k TSV 不符")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_bytes(data)
    print(f"{len(entries)} 詞，{len(data)} bytes，SHA256 {OUTPUT_SHA256}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", required=True, type=Path, help="已 base64 解碼的官方 .gz 檔")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    generate(args.source, args.output)


if __name__ == "__main__":
    main()
