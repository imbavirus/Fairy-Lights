import os
import subprocess

test_file = '''
public class TestEnv {
    public void test() {
        Object x = net.neoforged.fml.loading.FMLLoader.getDist();
        Object y = net.neoforged.fml.loading.FMLEnvironment.dist;
        Object z = net.neoforged.api.distmarker.Dist.CLIENT;
    }
}
'''
with open('src/main/java/TestEnv.java', 'w') as f:
    f.write(test_file)

print("Compiling...")
subprocess.run(['./gradlew', 'compileJava'], stdout=open('test_env3.log', 'w'), stderr=subprocess.STDOUT)

print("Errors:")
with open('test_env3.log', 'r', encoding='utf-8', errors='ignore') as f:
    for line in f:
        if 'TestEnv' in line:
            print(line.strip())
