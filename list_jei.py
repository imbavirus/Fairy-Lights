import urllib.request, json, os

url = 'https://api.modrinth.com/v2/project/jei/version'
req = urllib.request.Request(url, headers={'User-Agent': 'Infernos-AI'})
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read())
        if data:
            print("Found versions on Modrinth:")
            for item in data[:50]:
                print(f"{item['version_number']} - MC {item['game_versions']} - Loaders {item['loaders']}")
except Exception as e:
    print(f'Error: {e}')
