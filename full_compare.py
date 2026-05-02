def get_files(filename):
    with open(filename, 'r') as f:
        return set(line.strip() for line in f if line.strip())

fairy = get_files('fairy_jar.txt')
mdk = get_files('mdk_jar.txt')

missing = sorted(list(mdk - fairy))
extra = sorted(list(fairy - mdk))

print(f"Total files in Fairy: {len(fairy)}")
print(f"Total files in MDK: {len(mdk)}")

print("\n=== TOP 20 MISSING FROM FAIRY ===")
for f in missing[:20]:
    print(f)

print("\n=== TOP 20 EXTRA IN FAIRY ===")
for f in extra[:20]:
    print(f)

# Search specifically for ResourceLocation in Fairy set
print("\n=== RESOURCE LOCATION SEARCH IN FAIRY SET ===")
found_rl = [f for f in fairy if 'ResourceLocation' in f]
for f in sorted(found_rl):
    print(f)
