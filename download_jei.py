import urllib.request, json, os, urllib.parse

# 1. Fetch from Modrinth
url = 'https://api.modrinth.com/v2/project/jei/version?loaders=["neoforge"]&game_versions=["1.21.2"]'
# encode the parameters properly
params = {'loaders': '["neoforge"]', 'game_versions': '["1.21.2"]'}
url = 'https://api.modrinth.com/v2/project/jei/version?' + urllib.parse.urlencode(params)

req = urllib.request.Request(url, headers={'User-Agent': 'Infernos-AI'})
try:
    print(f"Fetching {url}")
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read())
        if data:
            latest = data[0]
            print(f"Found version: {latest['version_number']}")
            file_url = latest['files'][0]['url']
            file_name = latest['files'][0]['filename']
            print(f"File URL: {file_url}")
            print(f"Filename: {file_name}")
            
            target_dir = r'C:\Users\imba\AppData\Roaming\infernos-mc-launcher\instances\1.21.2\minecraft\mods'
            os.makedirs(target_dir, exist_ok=True)
            out_path = os.path.join(target_dir, file_name)
            
            # Download file
            print(f"Downloading to {out_path}...")
            urllib.request.urlretrieve(file_url, out_path)
            
            # Remove previous wrong version if it's there
            wrong_path = os.path.join(target_dir, 'jei-1.21.1-neoforge-19.21.2.313.jar')
            if os.path.exists(wrong_path):
                os.remove(wrong_path)
                print("Removed wrong 1.21.1 version")
            print("Download Complete!")
        else:
            print('No versions found on Modrinth for 1.21.2')
except Exception as e:
    print(f'Error: {e}')
