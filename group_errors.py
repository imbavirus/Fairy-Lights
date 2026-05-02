with open('compile3.log', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

errors = [l.rstrip() for l in lines if 'error:' in l]

# Group by file
from collections import Counter
files = Counter()
for e in errors:
    # Extract file path
    parts = e.split(':')
    if len(parts) >= 2:
        fname = parts[0].split('\\')[-1]
        files[fname] += 1

print("Errors by file:")
for fname, count in files.most_common():
    print(f"  {fname}: {count}")

print(f"\nTotal: {len(errors)}")

# Show first 30 unique errors
print("\nFirst 30 errors:")
for e in errors[:30]:
    # Just show the filename + line + error
    short = e.replace('C:\\Users\\imba\\Git\\Fairy-Lights\\src\\main\\java\\za\\co\\infernos\\fairylights\\', '')
    print(f"  {short}")
