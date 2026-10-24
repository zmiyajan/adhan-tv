"""Build app/src/main/assets/cities.tsv from GeoNames (CC BY 4.0, https://www.geonames.org/).

Download first, into one folder:
  cities15000.zip  (https://download.geonames.org/export/dump/cities15000.zip), unzipped
  alternatenames/<CC>.zip for Arabic-speaking countries, unzipped into <folder>/alt/

Usage: python3 tools/cities.py <folder>

Output columns: id, Arabic name (may be empty), English name, country code, latitude, longitude, time zone.
Rows are sorted by population, largest first, so search and "nearest city" prefer real cities.
"""
import csv
import pathlib
import sys

src = pathlib.Path(sys.argv[1])
out = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/cities.tsv"

arabic = {}   # geonameid -> (preferred?, short?, name)
for f in (src / "alt").glob("*.txt"):
    if f.name == "readme.txt":
        continue
    for row in csv.reader(f.open(encoding="utf-8"), delimiter="\t", quoting=csv.QUOTE_NONE):
        if len(row) < 4 or row[2] != "ar" or (len(row) > 7 and row[7] == "1"):   # skip historic names
            continue
        rank = (row[4] == "1", row[5] != "1")
        if row[1] not in arabic or rank > arabic[row[1]][0]:
            arabic[row[1]] = (rank, row[3].strip())

rows = []
for r in csv.reader((src / "cities15000.txt").open(encoding="utf-8"), delimiter="\t", quoting=csv.QUOTE_NONE):
    gid, name, ascii_, lat, lng, cc, pop, tz = r[0], r[1], r[2], r[4], r[5], r[8], int(r[14] or 0), r[17]
    ar = arabic.get(gid, (None, ""))[1]
    rows.append((pop, [gid, ar, name, cc, f"{float(lat):.4f}", f"{float(lng):.4f}", tz]))

rows.sort(key=lambda x: -x[0])
out.parent.mkdir(parents=True, exist_ok=True)
with out.open("w", encoding="utf-8") as f:
    for _, r in rows:
        f.write("\t".join(c.replace("\t", " ") for c in r) + "\n")
print(f"{len(rows)} cities, {sum(1 for _, r in rows if r[1])} with Arabic names -> {out}")
