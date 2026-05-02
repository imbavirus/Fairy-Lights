import urllib.request, json, os, zipfile, tempfile, shutil

target_dir = r'C:\Users\imba\AppData\Roaming\infernos-mc-launcher\instances\1.21.2\minecraft\mods'
os.makedirs(target_dir, exist_ok=True)

url = 'https://api.modrinth.com/v2/project/jei/version'
req = urllib.request.Request(url, headers={'User-Agent': 'Infernos-AI'})

try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read())
        
        # Find latest 1.21.4 neoforge version
        target_version = None
        for item in data:
            if 'neoforge' in item['loaders'] and '1.21.4' in item['game_versions']:
                target_version = item
                break
        
        if not target_version:
            print("No 1.21.4 neoforge version found!")
            exit(1)
            
        file_info = target_version['files'][0]
        file_url = file_info['url']
        original_filename = file_info['filename']
        print(f"Downloading {original_filename}...")
        
        temp_jar = os.path.join(tempfile.gettempdir(), original_filename)
        urllib.request.urlretrieve(file_url, temp_jar)
        
        # Unzip, modify neoforge.mods.toml, rezip
        extract_dir = os.path.join(tempfile.gettempdir(), 'jei_extract')
        if os.path.exists(extract_dir):
            shutil.rmtree(extract_dir)
        os.makedirs(extract_dir)
        
        print("Extracting...")
        with zipfile.ZipFile(temp_jar, 'r') as zip_ref:
            zip_ref.extractall(extract_dir)
            
        toml_path = os.path.join(extract_dir, 'META-INF', 'neoforge.mods.toml')
        if os.path.exists(toml_path):
            print("Patching neoforge.mods.toml...")
            with open(toml_path, 'r', encoding='utf-8') as f:
                toml_content = f.read()
            
            # modify version requirements
            # Usually looks like: versionRange="[1.21.4,1.22)" for minecraft
            toml_content = toml_content.replace('versionRange="[1.21.4,1.22)"', 'versionRange="[1.21.2,1.22)"')
            toml_content = toml_content.replace('versionRange="1.21.4"', 'versionRange="[1.21.2,1.22)"')
            
            # NeoForge version range might also need a bump down
            # "neo_version_range": "[21.4.0,)" -> "[21.2.0,)"
            toml_content = toml_content.replace('versionRange="[21.4.0,)"', 'versionRange="[21.2.0,)"')
            toml_content = toml_content.replace('versionRange="[21.4,)"', 'versionRange="[21.2,)"')
            
            with open(toml_path, 'w', encoding='utf-8') as f:
                f.write(toml_content)
        else:
            print("No META-INF/neoforge.mods.toml found!")
            
        new_filename = original_filename.replace('1.21.4', '1.21.2-patched')
        new_jar_path = os.path.join(target_dir, new_filename)
        
        print(f"Repacking to {new_jar_path}...")
        # create zip archive
        shutil.make_archive(new_jar_path.replace('.jar', ''), 'zip', extract_dir)
        # rename .zip to .jar
        if os.path.exists(new_jar_path):
            os.remove(new_jar_path)
        os.rename(new_jar_path.replace('.jar', '.zip'), new_jar_path)
        
        print("Done patching JEI!")
        
except Exception as e:
    print(f'Error: {e}')
