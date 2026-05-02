lines = open('test_error3.log', encoding='utf-8', errors='ignore').readlines()
targets = ['ServerProxy.java:13:', 'DataGatherer.java:42:', 'ClientEventHandler.java:407', 'FenceFastenerEntity.java:195', 'AbstractFastener.java:368', 'LightModel.java:46', 'LightModel.java:116']

for i, l in enumerate(lines):
    for t in targets:
        if t in l:
            print("---")
            print(''.join(lines[max(0, i-1):i+3]))
