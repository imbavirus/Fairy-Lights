import re
from collections import defaultdict
with open('compile4.log', encoding='utf-8', errors='ignore') as f:
    content = f.read()
error_lines = re.findall(r'([^\n]*\.java:\d+): error: ([^\n]*)', content)
file_errors = defaultdict(set)
for path, msg in error_lines:
    basename = path.split('\\')[-1]
    file_errors[basename].add(msg.strip()[:120])
for fname in sorted(file_errors):
    print(f'{fname}:')
    for msg in sorted(file_errors[fname]):
        print(f'  - {msg}')
    print()
print(f'Total errors: {len(error_lines)}')
