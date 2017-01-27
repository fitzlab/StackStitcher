"""
For use in cases where the stage motions worked right and no algorithmic correction is needed.
Generates a coordinates file for use with StackStitcher.

To use:
In StackStitcher, enter your image dir path and coordinate info as usual.
Then select "Apply Known Coordinates" and put this coordinates file in.
"""

# replace with your actual grid size
gridX = 7
gridY = 7

# Subtract overlap from imageSize, e.g. 512px with 10% overlap would be size (512 - 51) = 461.
imageSizeX = 512
imageSizeY = 512

outfile = r"c:\coords.txt"

# generate coordinates file contents
lines = []
for y in range(gridY):
    yPos = y * imageSizeY
    for x in range(gridX):
        xPos = x * imageSizeX
        s = "block x=" + str(x) + " y=" + str(y) + \
            " upper left at (" + str(xPos) + "," + str(yPos) + ")"
        lines.append(s)

#write to file
with open(outfile, 'w') as fh:
    for line in lines:
        fh.write(line + "\n")

#done
print("Wrote coordinates to " + outfile)