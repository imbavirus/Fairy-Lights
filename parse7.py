import re
from collections import defaultdict
with open('compile7.log', encoding='utf-8', errors='ignore') as f:
    content = f.read()
error_lines = re.findall(r'([^\r\n]*\.java:\d+): error: ([^\r\n]*)', content)
file_errors = defaultdict(set)
for path, msg in error_lines:
    basename = path.split('\\')[-1]
    file_errors[basename].add(msg.strip()[:150])
with open('errors7.txt', 'w', encoding='utf-8') as out:
    for fname in sorted(file_errors):
        out.write(f'{fname}:\n')
        for msg in sorted(file_errors[fname]):
            out.write(f'  - {msg}\n')
        out.write('\n')
    out.write(f'Total errors: {len(error_lines)}\n')
print(f'Total errors: {len(error_lines)}')
