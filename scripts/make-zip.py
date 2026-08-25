"""生成正斜杠路径的 zip，避免 Windows Compress-Archive 产生反斜杠文件名。
用法: python make-zip.py <srcDir> <outZip> [zipPrefix]
  zipPrefix 为空时内容在 zip 根目录；非空时内容在 zipPrefix/ 下。
"""
import os
import sys
import zipfile

def main():
    if len(sys.argv) < 3:
        print("usage: make-zip.py <srcDir> <outZip> [zipPrefix]")
        sys.exit(1)
    src = os.path.abspath(sys.argv[1])
    out = os.path.abspath(sys.argv[2])
    prefix = sys.argv[3] if len(sys.argv) > 3 else ""
    prefix = prefix.strip("/\\")
    if not os.path.isdir(src):
        print("source dir not found:", src)
        sys.exit(1)
    count = 0
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        for root, _dirs, files in os.walk(src):
            for f in files:
                p = os.path.join(root, f)
                rel = os.path.relpath(p, src).replace("\\", "/")
                arc = (prefix + "/" + rel) if prefix else rel
                z.write(p, arc)
                count += 1
    print("zip created: %s (%d files)" % (out, count))

if __name__ == "__main__":
    main()