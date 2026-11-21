"""Binary-compatibility check: plugin bytecode vs. the Paper API.

The plugin is compiled against the Spigot API but must also run on Paper.
A few Bukkit types are interfaces in one API and classes in the other
(e.g. org.bukkit.GameRule in 26.x), which makes direct calls fail at runtime
with IncompatibleClassChangeError. This script lists every org.bukkit method
call in the compiled classes and verifies the owner kind matches Paper.
Method *existence* is covered separately by `./mvnw -Ppaper compile`.

Usage: python -I check_paper_abi.py <javap> <target/classes> <paper-api.jar>
"""
import re, subprocess, sys, zipfile
javap, classes_dir, paper_jar = sys.argv[1:4]
import glob, os
files = glob.glob(os.path.join(classes_dir, '**', '*.class'), recursive=True)
out = subprocess.run([javap, '-c', '-p'] + files, capture_output=True, text=True).stdout
refs = set(re.findall(r'(invoke\w+)\s+#\d+(?:,\s*\d+)?\s+// (InterfaceMethod|Method) (org/bukkit/[\w/$]+)\.([\w<>$]+):(\S+)', out))
z = zipfile.ZipFile(paper_jar)
cache = {}
def info(owner):
    if owner in cache: return cache[owner]
    name = owner + '.class'
    if name not in z.namelist():
        cache[owner] = None; return None
    r = subprocess.run([javap, '-p', '-s', '-cp', paper_jar, owner.replace('/', '.')], capture_output=True, text=True).stdout
    cache[owner] = r; return r
problems = 0
for op, kind, owner, meth, desc in sorted(refs):
    r = info(owner)
    if r is None:
        print('MISSING CLASS', owner); problems += 1; continue
    header = next((l for l in r.splitlines() if (' class ' in l or ' interface ' in l)), '')
    is_iface = ' interface ' in header
    if (kind == 'InterfaceMethod') != is_iface:
        print(f'KIND MISMATCH {op} {owner}.{meth}{desc}  (compiled as {kind}, Paper has {"interface" if is_iface else "class"})'); problems += 1
print(f'{len(refs)} refs checked, {problems} problems')
sys.exit(1 if problems else 0)
