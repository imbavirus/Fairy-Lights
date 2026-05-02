def get_packages(filename):
    packages = set()
    with open(filename, 'r') as f:
        for line in f:
            if line.startswith('net/minecraft/'):
                parts = line.strip().split('/')
                if len(parts) > 3:
                    packages.add('/'.join(parts[0:3]))
    return sorted(list(packages))

fairy_pkgs = get_packages('fairy_jar.txt')
mdk_pkgs = get_packages('mdk_jar.txt')

print("=== FAIRY LIGHTS MC PACKAGES ===")
for p in fairy_pkgs:
    print(p)

print("\n=== MDK MC PACKAGES ===")
for p in mdk_pkgs:
    print(p)

print("\n=== MISSING IN FAIRY ===")
for p in mdk_pkgs:
    if p not in fairy_pkgs:
        print(p)
