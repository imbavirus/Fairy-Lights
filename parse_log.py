with open('compile3.log', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

errors = [l.rstrip() for l in lines if 'error:' in l]
failures = [l.rstrip() for l in lines if 'FAIL' in l or 'Could not' in l]

print(f"Java errors: {len(errors)}")
for e in errors[:20]:
    print(f"  {e}")

print(f"\nBuild failures: {len(failures)}")
for f in failures[:10]:
    print(f"  {f}")

# Show last 15 lines
print("\n--- Last 15 lines ---")
for l in lines[-15:]:
    print(l.rstrip())
