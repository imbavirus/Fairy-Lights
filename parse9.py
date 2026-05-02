import re
from collections import defaultdict
with open('compile9.log', encoding='utf-8', errors='ignore') as f:
    content = f.read()
error_lines = re.findall(r'([^\r\n]*\.java:\d+): error: ([^\r\n]*)', content)
file_errors = defaultdict(list)
for path, msg in error_lines:
    basename = path.split('\\')[-1]
    file_errors[basename].append(msg.strip()[:150])
with open('errors9.txt', 'w', encoding='utf-8') as out:
    for fname in sorted(file_errors):
        errs = sorted(set(file_errors[fname]))
        out.write(f'{fname} ({len(file_errors[fname])} errors):\n')
        for msg in errs:
            out.write(f'  - {msg}\n')
        out.write('\n')
    out.write(f'Total errors: {len(error_lines)}\n')
