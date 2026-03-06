lines = []
with open('test_error3.log', encoding='utf-8', errors='ignore') as f:
    for i, line in enumerate(f):
        if 'error:' in line:
            lines.append(line.strip())

for l in lines[:30]:
    print(l)
