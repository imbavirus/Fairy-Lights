import zipfile
import glob
import os

start_dir = os.path.expanduser("~/.gradle/caches/neoformruntime/")
jars = glob.glob(start_dir + "**/*minecraft-1.21.11*.jar", recursive=True)

for jar in jars:
    print(f"Checking {jar}...")
    try:
        with zipfile.ZipFile(jar, 'r') as zf:
            for entry in zf.namelist():
                if "ResourceLocation.class" in entry or "RenderType.class" in entry or "ModelData.class" in entry or "Minecraft.class" in entry:
                    print(f"  Found: {entry}")
    except Exception as e:
        print(f"Error opening {jar}: {e}")
