import os
import zipfile

def find_in_file(jar, class_name):
    if not os.path.exists(jar): return False
    try:
        with zipfile.ZipFile(jar, 'r') as z:
            return any(class_name in name for name in z.namelist())
    except: return False

jars = [
    r'C:\Users\imba\Git\Fairy-Lights\build\moddev\artifacts\neoforge-21.11.38-beta.jar',
    r'C:\Users\imba\Git\Fairy-Lights\build\moddev\artifacts\neoforge-21.11.38-beta-merged.jar',
    r'C:\Users\imba\Git\Fairy-Lights\build\moddev\artifacts\neoforge-21.11.38-beta-client-extra-aka-minecraft-resources.jar'
]

classes = [
    'net/minecraft/resources/ResourceLocation.class',
    'net/minecraft/world/level/Level.class',
    'net/minecraft/client/Minecraft.class',
    'net/minecraft/server/MinecraftServer.class',
    'net/minecraft/Util.class'
]

for clz in classes:
    print(f"\n--- SEARCHING FOR {clz} ---")
    for j in jars:
        if find_in_file(j, clz):
            print(f"FOUND IN: {j}")
        else:
            print(f"NOT FOUND IN: {j}")
