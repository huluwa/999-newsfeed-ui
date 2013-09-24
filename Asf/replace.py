import sys
import os
import re
import shutil
def replace(strFileName):
  fileFile = open(strFileName, 'r')
  string = fileFile.read()
  fileFile.close()
  for oldValue, newValue in replaceList:
    string = re.sub ( oldValue , newValue , string )
    fileFile = open(strFileName, 'w')
    fileFile.write(string)
    fileFile.close()

def visitDir(dir):
  if os.path.isfile( dir ):  
    replace(dir)
    print dir
    return
  for file in os.listdir(dir):
    filePath = os.path.join( dir, file )
    visitDir(filePath)

length=len(sys.argv)
if length < 4:
  sys.exit()
[dir_old,token_old,token_new ]= sys.argv[1:]
replaceList = [(token_old, token_new)]
print 'old token : ',sys.argv[2],'\nnew token : ',sys.argv[3]
dir_new = dir_old + "_new"
if(os.path.exists(dir_new)):
  shutil.rmtree(dir_new)
shutil.copytree(dir_old , dir_new)
visitDir(dir_new)
