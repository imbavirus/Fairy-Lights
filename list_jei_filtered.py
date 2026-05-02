import urllib.request, json
url = 'https://api.modrinth.com/v2/project/jei/version'
req = urllib.request.Request(url, headers={'User-Agent': 'Infernos-AI'})
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read())
        found = False
        for item in data:
            if 'neoforge' in item['loaders'] and ('1.21.2' in item['game_versions'] or '1.21.3' in item['game_versions']):
                print(f"{item['version_number']} - MC {item['game_versions']}")
                print(f"URL: {item['files'][0]['url']}")
                found = True
                break
        if not found:
            print("No 1.21.2 or 1.21.3 NeoForge versions found for JEI on Modrinth.")
except Exception as e:
    print(e)
