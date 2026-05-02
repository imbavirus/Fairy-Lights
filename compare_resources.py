def get_resources_files(filename):
    files = set()
    with open(filename, 'r') as f:
        for line in f:
            if line.startswith('net/minecraft/resources/'):
                files.add(line.strip())
    return files

fairy_files = get_resources_files('fairy_jar.txt')
mdk_files = get_resources_files('mdk_jar.txt')

print("=== IN FAIRY BUT NOT IN MDK ===")
for f in sorted(list(fairy_files - mdk_files)):
    print(f)

print("\n=== IN MDK BUT NOT IN FAIRY ===")
for f in sorted(list(mdk_files - fairy_files)):
    print(f)

print("\n=== IN BOTH ===")
for f in sorted(list(mdk_files & fairy_files)):
    print(f)
