lines = []
with open('test_error4.log', encoding='utf-8', errors='ignore') as f:
    for i, line in enumerate(f):
        if 'error:' in line:
            lines.append(line.strip() + '\n')

with open('err2.txt', 'w', encoding='utf-8') as f:
    f.writelines(lines[:30])
