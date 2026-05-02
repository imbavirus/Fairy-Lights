with open('compile2.log', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Find FAILURE line and print surrounding context
lines = content.split('\n')
for i, line in enumerate(lines):
    if 'FAILURE' in line or 'Could not find' in line or 'Could not resolve' in line:
        start = max(0, i-2)
        end = min(len(lines), i+10)
        for j in range(start, end):
            print(f"{j}: {lines[j].rstrip()}")
        print("---")
