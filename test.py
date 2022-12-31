import os
import shutil
import subprocess

for i in range(10,31):
    shutil.copyfile(os.path.join("testcase/testfiles-only/B",'testfile{}.txt'.format(i)),'./testfile.txt')
    shutil.copyfile(os.path.join("testcase/full/B",'input{}.txt'.format(i)),'./stdin.txt')
    shutil.copyfile(os.path.join("testcase/full/B",'output{}.txt'.format(i)),'./stdout.txt')
    print("test"+str(i)+" begin: ")
    t=os.system("java -jar ./out/artifacts/Compiler_jar/Compiler.jar")
    t=os.system("java -jar mars.jar mips.txt nc < stdin.txt > mips_output.txt")
    t=subprocess.Popen(args=["git","diff","mips_output.txt","stdout.txt",'-b']).wait()
    
