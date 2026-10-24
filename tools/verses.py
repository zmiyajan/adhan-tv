"""Build app/src/main/assets/verses.json from the official KFGQPC Hafs data (kfgqpc_hafs_v30.json).

The text is never typed by hand: each entry names a verse and the word to start from, and the words are copied
from the Complex's data, which is made to pair with the bundled KFGQPC font.

Usage: python3 tools/verses.py <path to kfgqpc_hafs_v30.json>
"""
import json
import pathlib
import sys

# (surah, ayah, first word, surah name for the reference line, use)
VERSES = [
    (4, 103, 14, "النساء", "adhan"),   # إِنَّ ٱلصَّلَوٰةَ كَانَتۡ عَلَى ٱلۡمُؤۡمِنِينَ كِتَٰبࣰا مَّوۡقُوتࣰا
]

data = {(a["sura_no"], a["aya_no"]): a for a in json.load(open(sys.argv[1], encoding="utf-8"))}
out = []
for sura, aya, first, name, use in VERSES:
    words = data[(sura, aya)]["aya_text_unicode"].split()
    words = [w for w in words if not w.startswith("۝")]          # drop the end-of-ayah number
    text = " ".join(words[first - 1:])
    out.append({"surah": sura, "ayah": aya, "name": name, "use": use, "text": text})
    print(sura, aya, text)

path = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/verses.json"
path.write_text(json.dumps(out, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
