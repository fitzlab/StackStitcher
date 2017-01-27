# Renames the Leica-format files so that they will be in order for stitching

import glob
import re
import os

def rename_files():
     
     imageDir = r'/home/theo/repos_data/StackStitcher/tutorial/two_by_two'  # <---- change this to your image path!
     
     for filePath in glob.glob(imageDir + "/*.tif"):
          sliceNum = ""
          channelNum = ""
          zPos = ""
     
          (dirPath, tif) = os.path.split(filePath)
     
          try:
               print(filePath)
               m = re.search('(s\d+)', tif)
               sliceNum = m.group(1)
               
               m = re.search('(z\d+)', tif)
               zPos = m.group(1)
               
               m = re.search('(ch\d+)', tif)
               channelNum = m.group(1)
     
          except:
               print "skipping file: " + filePath
               continue
     
          #print filePath + " " + sliceNum + " " + zPos + " " + channelNum
          newFileName = channelNum + "_" + zPos + "_" + sliceNum + ".tif" 
     
          destDir = dirPath + "/" + channelNum
     
          try:
               os.makedirs(destDir)
          except:
               #it will complain if the dir already exists, but we don't care
               pass
          
          target = destDir + "/" + newFileName
          print "Moved " + filePath + " to " + target
          os.rename(filePath, target)

if __name__ == "__main__":
     rename_files()