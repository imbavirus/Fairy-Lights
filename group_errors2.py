import re
from collections import Counter

with open('compile3.log', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Find all error lines
error_lines = re.findall(r'(.*?\.java:\d+): error: (.*)', content)
    
# Group by file basename
files = Counter()
for path, msg in error_lines:
    basename = path.split('\\')[-1]
    files[basename] += 1

print("Errors by file:")
for fname, count in files.most_common():
    print(f"  {fname}: {count}")

print(f"\nTotal: {len(error_lines)}")

# Show unique error messages per file
print("\nUnique errors per file:")
from collections import defaultdict
file_errors = defaultdict(set)
for path, msg in error_lines:
    basename = path.split('\\')[-1]
    file_errors[basename].add(msg.strip())

for fname in sorted(file_errors):
    print(f"\n  {fname}:")
    for msg in sorted(file_errors[fname]):
        print(f"    - {msg[:100]}")
