import zipfile, glob, subprocess

print("=== JEI API ===")
jars = glob.glob('C:/Users/imba/.gradle/caches/modules-2/files-2.1/mezz.jei/**/*common-api-19.21.*.jar', recursive=True)
for j in jars:
    z = zipfile.ZipFile(j)
    for n in z.namelist():
        if "IRecipeSlotBuilder" in n or "ICraftingGridHelper" in n:
            print("  ", n)
