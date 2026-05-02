with open('compile2.log', encoding='utf-8', errors='ignore') as f:
    content = f.read()

for search in ['FAILURE', 'Could not resolve', 'Could not find', 'jei']:
    idx = content.lower().find(search.lower())
    if idx >= 0:
        print(f'=== Found "{search}" at offset {idx} ===')
        print(content[max(0,idx-200):idx+500])
        print()
