lines = []
with open('1.21.2_baseline.log', encoding='utf-8', errors='ignore') as f:
    for i, line in enumerate(f):
        if 'error:' in line:
            lines.append(line.strip() + '\n')

with open('err_1_21_2_all.txt', 'w', encoding='utf-8') as f:
    f.writelines(lines[:500])
