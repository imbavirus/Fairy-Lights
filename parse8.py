import re
from collections import defaultdict
with open('compile8.log', encoding='utf-8', errors='ignore') as f:
    content = f.read()
error_lines = re.findall(r'([^\r\n]*\.java:\d+): error: ([^\r\n]*)', content)
file_errors = defaultdict(set)
for path, msg in error_lines:
    basename = path.split('\\')[-1]
    file_errors[basename].add(msg.strip()[:150])
for fname in sorted(file_errors):
    print(f'{fname}:')
    for msg in sorted(file_errors[fname]):
        print(f'  - {msg}')
    print()
print(f'Total errors: {len(error_lines)}')
