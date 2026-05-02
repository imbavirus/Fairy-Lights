import subprocess
out = subprocess.check_output(['javap', '-p', '-cp', 'custom-minecraft-classes.jar', 'net.minecraft.client.model.Model']).decode('utf-8')
print('--- Model ---')
for l in out.splitlines():
    if 'public net.minecraft.client.model.Model' in l or 'renderToBuffer' in l: print(l.strip())

out2 = subprocess.check_output(['javap', '-p', '-cp', 'custom-minecraft-classes.jar', 'net.minecraft.world.entity.Entity']).decode('utf-8')
print('--- Entity ---')
for l in out2.splitlines():
    if 'spawnAtLocation' in l: print(l.strip())
