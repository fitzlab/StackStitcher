/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools.imageTools;

import confocaltools.Constants;
import confocaltools.FileAndDirOperations;
import confocaltools.Log;
import confocaltools.Utility;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JTextPane;
import stitching.Point2D;

/**
 *
 * @author walkert
 */
public class ImageStitcher {
    
    //populated during stitching
    StitchCoordinates[][] stitches;
    String[][][] imagePathGrid;
    
    int gridXSize;
    int gridYSize;
    int gridZSize;
    
    int imageWidth;
    int imageHeight;
    
    String overlapType = Constants.OVERLAP_AVERAGE;
    boolean applyFFT = true;
    
    int numPeaks = 100;
    double zPercent = 15;
    double maxOverlap = 15;
    JTextPane txtLog = null;
    
    private int[] nextIndex = {0,0};
    private synchronized int[] getNextIndex(){
        if(nextIndex[0] < gridXSize-1){
            //not at limit of x yet, so increment x
            nextIndex[0]++;
        }
        else if(nextIndex[1] < gridYSize-1){
            //we ran out of x, so increment y and reset x
            nextIndex[1]++;
            nextIndex[0] = 0;
        }
        else{
            //we're out of everything. Increment to
            //something stupid, and the threads will quit.
            nextIndex[0]++;
            nextIndex[1]++;
        }
        return nextIndex;
    }
    
    private int numThreads = 5;
    private int numThreadsCompleted = 0;
    private synchronized void incrementCompletedThreads(){
        numThreadsCompleted++;
    }
    
    private class StitchCoordinates {
        //regression of image overlap (0..1, higher is better)
        public double regression = 0.0;
        
        //coordinates of the upper left pixel of this image
        public int[] coords = new int[2];
        
        //all potential stitches. One of these will be used to place
        //the tile at the end.
        public double regressionUp = 0.0;
        public double regressionDown = 0.0;
        public double regressionLeft = 0.0;
        public double regressionRight = 0.0;
        
        public int[] coordsUp = new int[2];
        public int[] coordsDown = new int[2];
        public int[] coordsLeft = new int[2];
        public int[] coordsRight = new int[2];
    }
    
    private class StitchEdge implements Comparable {
        //holds the same info as StitchCoordinates,
        //but organizes in a manner that makes spanning tree 
        //calculation easier to write
        
        //edge weight
        public double regression = 0.0;
        
        //first node index
        int[] node1Index = new int[2];

        //second node index
        int[] node2Index = new int[2];
        
        int direction = Constants.EDGE_RIGHT;
        
        public int compareTo(Object o){
            StitchEdge e = (StitchEdge) o;
            if(this.regression > e.regression){
                return 1;
            }
            else if(this.regression < e.regression){
                return -1;
            }
            else{
                return 0;
            }
        }
    }
    
    public class FourierStitchThread extends Thread{
        //uses fourier phase correlation to find 
        //where each pari of slices overlaps
        public void run(){
            //Go through the Z-series, find the stitch appropriate to
            //the image at nextIndex
            //do this forever until all valid indices are consumed
            //assumes all data structures are set up before this is called.
            while(true){
                int[] myJob = getNextIndex();
                if(myJob[0] >= gridXSize || myJob[1] >= gridYSize){
                    //no more stitching tasks are available. We're done!
                    incrementCompletedThreads();
                    break;
                }
                
                int x = myJob[0];
                int y = myJob[1];
                                
                //do stitching on this spot
                Log.write("Finding overlaps for x=" + x + " y=" + y, Color.BLACK, txtLog);
                if(imagePathGrid[x][y][0].isEmpty()){
                    //blank image, so skip it
                    Log.write("Skipping x=" + x + " y=" + y, Color.ORANGE, txtLog);
                    continue;
                }
                
                //perform stitching with top and left neighbors
                //(right / down neighbor calculations will be the same)
                int zSizeToUse = (int) Math.round(gridZSize * (zPercent/100));
                
                //run fourier phase correllations on each point in the stack                
                int nextReportAt = 5;
                int reportIncrement = 10;
                for(int z = 0; z < zSizeToUse; z++){
                    double percentDone = Math.round((double)(z) / zSizeToUse * 100);
                    if(percentDone > nextReportAt){
                        Log.write("x=" + x + " y=" + y + " : " + percentDone + "% done.", Color.CYAN, txtLog);
                        nextReportAt += reportIncrement;
                    }
                    
                    BufferedImage toBeStitched = ImageFileIO.readImageFromPath(imagePathGrid[x][y][z]);
                    
                    if(x >= 1 && ! imagePathGrid[x-1][y][z].isEmpty()){
                        //left
                        BufferedImage stitchToThis = ImageFileIO.readImageFromPath(imagePathGrid[x-1][y][z]);
                        StitchCoordinates s = findStitch(stitchToThis, toBeStitched);
                        if(s.coords[0] > 0){ //reject insane results
                            if(s.regression > stitches[x-1][y].regressionRight){
                                stitches[x-1][y].regressionRight = s.regression;
                                stitches[x-1][y].coordsRight = s.coords;                            
                            }
                            if(s.regression > stitches[x][y].regressionLeft){
                                stitches[x][y].regressionLeft = s.regression;
                                //left coordinates are the same as the right ones,
                                //just shifted left
                                stitches[x][y].coordsLeft[0] = -s.coords[0];
                                stitches[x][y].coordsLeft[1] = -s.coords[1];
                            }
                        }
                    }
                    if(y >= 1 && ! imagePathGrid[x][y-1][z].isEmpty()){
                        //up
                        BufferedImage stitchToThis = ImageFileIO.readImageFromPath(imagePathGrid[x][y-1][z]);
                        StitchCoordinates s = findStitch(stitchToThis, toBeStitched);
                        
                        if(s.coords[1] > 0){ //reject insane results
                            if(s.regression > stitches[x][y-1].regressionDown){
                                    stitches[x][y-1].regressionDown = s.regression;
                                    stitches[x][y-1].coordsDown = s.coords;
                            }
                            if(s.regression > stitches[x][y].regressionUp){
                                stitches[x][y].regressionUp = s.regression;
                                //up coordinates are the same as the down ones,
                                //just shifted up
                                stitches[x][y].coordsUp[0] = -s.coords[0];
                                stitches[x][y].coordsUp[1] = -s.coords[1];
                            }
                        }
                    }
                }
            }            
        }
    }
    
    public class ResliceStitchThread extends Thread{
        //reslices data to make images in the XZ or YZ plane
        //slides images along one axis to find overlap
         public void run(){
            //Go through the Z-series, find the stitch appropriate to
            //the image at nextIndex
            //do this forever until all valid indices are consumed
            //assumes all data structures are set up before this is called.
            while(true){
                int[] myJob = getNextIndex();
                if(myJob[0] >= gridXSize || myJob[1] >= gridYSize){
                    //no more stitching tasks are available. We're done!
                    incrementCompletedThreads();
                    break;
                }
                
                int x = myJob[0];
                int y = myJob[1];
                                
                //do stitching on this spot
                Log.write("Finding overlaps for x=" + x + " y=" + y, Color.BLACK, txtLog);
                if(imagePathGrid[x][y][0].isEmpty()){
                    //blank image, so skip it
                    Log.write("Skipping x=" + x + " y=" + y, Color.ORANGE, txtLog);
                    continue;
                }
                
                //perform stitching with top and left neighbors
                //(right / down neighbor calculations will be the same)
                int zSizeToUse = (int) Math.round(gridZSize * (zPercent/100));
                int numSlicesX = (int) Math.round(imageWidth * (maxOverlap/100));
                int numSlicesY = (int) Math.round(imageHeight * (maxOverlap/100));
                
                //cover for 0% overlap base
                if(numSlicesX == 0){
                    numSlicesX = 1;
                }
                if(numSlicesY == 0){
                    numSlicesY = 1;
                }
                
                //Make images of the edge slices as we go vertically through the stack.
                ArrayList<BufferedImage> myTopSlices = new ArrayList<BufferedImage>();
                ArrayList<BufferedImage> myLeftSlices = new ArrayList<BufferedImage>();
                ArrayList<BufferedImage> neighborBottomSlices = new ArrayList<BufferedImage>();
                ArrayList<BufferedImage> neighborRightSlices = new ArrayList<BufferedImage>();
                
                if(x >= 1 && ! imagePathGrid[x-1][y][0].isEmpty()){
                    //initialize slice images to black
                    for(int i = 0; i < numSlicesX; i++){
                        BufferedImage leftSlice = new BufferedImage(imageWidth,zSizeToUse,BufferedImage.TYPE_USHORT_GRAY);
                        myLeftSlices.add(leftSlice);
                        BufferedImage neighborRightSlice = new BufferedImage(imageWidth,zSizeToUse,BufferedImage.TYPE_USHORT_GRAY);
                        neighborRightSlices.add(neighborRightSlice);
                    }
                }
                if(y >= 1 && ! imagePathGrid[x][y-1][0].isEmpty()){
                    //initialize slice images to black
                    for(int i = 0; i < numSlicesY; i++){
                        BufferedImage topSlice = new BufferedImage(imageWidth,zSizeToUse,BufferedImage.TYPE_USHORT_GRAY);
                        myTopSlices.add(topSlice);
                        BufferedImage neighborBottomSlice = new BufferedImage(imageWidth,zSizeToUse,BufferedImage.TYPE_USHORT_GRAY);
                        neighborBottomSlices.add(neighborBottomSlice);
                    }
                }
                
                //generate images for the facing sides (XZ or YZ)         
                for(int z = 0; z < zSizeToUse; z++){
                    BufferedImage toBeStitched = ImageFileIO.readImageFromPath(imagePathGrid[x][y][z]);
                    
                    if(x >= 1 && ! imagePathGrid[x-1][y][z].isEmpty()){
                        //left
                        BufferedImage stitchToThis = ImageFileIO.readImageFromPath(imagePathGrid[x-1][y][z]);
                        
                        //update face images
                        for(int i = 0; i < numSlicesX; i++){
                            for(int h = 0; h < imageHeight; h++){
                                myLeftSlices.get(i).setRGB(h,z, toBeStitched.getRGB(0, h));
                                neighborRightSlices.get(i).setRGB(h,z, stitchToThis.getRGB(imageWidth-1-i, h));
                            }
                        }
                    }
                    if(y >= 1 && ! imagePathGrid[x][y-1][z].isEmpty()){
                        //up
                        BufferedImage stitchToThis = ImageFileIO.readImageFromPath(imagePathGrid[x][y-1][z]);
                        
                        //update face images
                        for(int i = 0; i < numSlicesY; i++){
                            for(int w = 0; w < imageWidth; w++){
                                myTopSlices.get(i).setRGB(w,z, toBeStitched.getRGB(w,0));
                                neighborBottomSlices.get(i).setRGB(w,z, stitchToThis.getRGB(w,imageHeight-1-i));
                            }
                        }
                    }
                }
                
                //do face-to-face stitch
                if(x >= 1 && ! imagePathGrid[x-1][y][0].isEmpty()){
                    //check all face pairs, find the offset with smallest difference
                    double bestDifference = Double.MAX_VALUE;
                    for(int i = 0; i < myLeftSlices.size(); i++){
                        for(int j = 0; j < neighborRightSlices.size(); j++){
                            
                            double[] offsetAndDifference = ImageOperations.faceImageOffset(myLeftSlices.get(i),neighborRightSlices.get(j));
                            int offset = (int) offsetAndDifference[0];
                            double difference = offsetAndDifference[1];
                            
                            if(difference < bestDifference){
                                stitches[x][y].coordsLeft[0] = -imageWidth+j+i;
                                stitches[x][y].coordsLeft[1] = -offset;
                                stitches[x][y].regressionRight = -difference;

                                stitches[x-1][y].coordsRight[0] = imageWidth-j-i;
                                stitches[x-1][y].coordsRight[1] = offset;
                                stitches[x-1][y].regressionRight = -difference;
                            }
                        }
                    }
                }
                if(y >= 1 && ! imagePathGrid[x][y-1][0].isEmpty()){
                    //check all face pairs, find the offset with smallest difference
                    double bestDifference = Double.MAX_VALUE;
                    for(int i = 0; i < myTopSlices.size(); i++){
                        for(int j = 0; j < neighborBottomSlices.size(); j++){
                            double[] offsetAndDifference = ImageOperations.faceImageOffset(myTopSlices.get(i),neighborBottomSlices.get(j));
                            int offset = (int) offsetAndDifference[0];
                            double difference = offsetAndDifference[1];
                            if(difference < bestDifference){
                                stitches[x][y].coordsUp[0] = -offset;
                                stitches[x][y].coordsUp[1] = -imageHeight+j+i;
                                stitches[x][y].regressionUp = -difference;

                                stitches[x][y-1].coordsDown[0] = offset;
                                stitches[x][y-1].coordsDown[1] = imageHeight-j-i;
                                stitches[x][y-1].regressionDown = -difference;
                            }
                        }
                    }
                }
            }            
        }
    }
    
    public void stitchDir(
            String mainDir, ArrayList<String> extraDirs, int gridXSize, int gridYSize, Integer[] skipBlocks, String scanType, //input params
            int numPeaks, double zPercent, double maxOverlap, int numThreads, //stitching calculation params
            int stitchAlgorithm, String coordsFile, //user-stitch params
            boolean applyFFT, int zProjectMethod, String overlapType, //output params
            JTextPane txtLog //logging
            ){
        
        this.gridXSize = gridXSize;
        this.gridYSize = gridYSize;
        this.numPeaks = numPeaks;
        this.applyFFT = applyFFT;
        this.numThreads = numThreads;
        this.zPercent = zPercent;
        this.maxOverlap = maxOverlap;
        this.txtLog = txtLog;
        
        int blocksPerZStep = (gridXSize * gridYSize) - skipBlocks.length;
        
        if(stitchAlgorithm == Constants.FOURIER_STITCH && zProjectMethod != -1){
            //Perform Z-projection if needed
            ZProjectTiles.zProjectTiles(mainDir, blocksPerZStep, zProjectMethod, txtLog);
            extraDirs.add(mainDir);
            mainDir += "/zProject/";
        }

        //fix the dir paths
        mainDir = Utility.fixDirPath(mainDir);
        for(int i = 0; i < extraDirs.size(); i++){
            extraDirs.set(i, Utility.fixDirPath(extraDirs.get(i)));
        }
        
        //get filenames of images
        ArrayList<String> imagePaths = ImageFileIO.getImageFilesFromDir(mainDir);
        Log.write("" + imagePaths.size() + " images found in " + mainDir, Color.WHITE, txtLog);
        
        
        if(imagePaths.size() % blocksPerZStep != 0){
            Log.write("Error: " + imagePaths.size() + " is not evenly divisible by your grid size of " + blocksPerZStep, Color.ORANGE, txtLog);
            return;
        }
        
        gridZSize = imagePaths.size() / blocksPerZStep;
        imagePathGrid = fixImageOrder(imagePaths, gridXSize, gridYSize, gridZSize, skipBlocks, scanType);
        
        //get X and Y size of image tiles
        BufferedImage temp = ImageFileIO.readImageFromPath(imagePaths.get(0));
        imageWidth = temp.getWidth();
        imageHeight = temp.getHeight();
        
        //we will go through the Z series and find the pair of images
        //that makes the best possible stitch. 
        //stitches data structure holds best result across z for each spot
        stitches = new StitchCoordinates[gridXSize][gridYSize];
        for(int x = 0; x < gridXSize; x++){
            for(int y = 0; y < gridYSize; y++){
                stitches[x][y] = new StitchCoordinates();
                stitches[x][y].regression = 0;
                stitches[x][y].coords[0] = x*imageWidth;
                stitches[x][y].coords[1] = y*imageWidth;
            }
        }
        
        if(stitchAlgorithm == Constants.RESLICE_STITCH || stitchAlgorithm == Constants.FOURIER_STITCH){
            //stitch automagically            
            //start threads to perform stitching
            
            if(stitchAlgorithm == Constants.FOURIER_STITCH){
                
                
                //start fourier stitch threads
                ArrayList<FourierStitchThread> threads = new ArrayList<FourierStitchThread>();
                for(int i = 0; i < numThreads; i++){
                    FourierStitchThread t = new FourierStitchThread();
                    t.start();
                    threads.add(t);
                }

                //wait for threads to finish
                while(numThreadsCompleted < numThreads){
                    try{
                        Thread.sleep(1000);
                    }
                    catch(Exception ex){
                        Log.write(ex);
                    }
                }
            }
            else if(stitchAlgorithm == Constants.RESLICE_STITCH){
                //start reslice stitch threads
                ArrayList<ResliceStitchThread> threads = new ArrayList<ResliceStitchThread>();
                for(int i = 0; i < numThreads; i++){
                    ResliceStitchThread t = new ResliceStitchThread();
                    t.start();
                    threads.add(t);
                }

                //wait for threads to finish
                while(numThreadsCompleted < numThreads){
                    try{
                        Thread.sleep(1000);
                    }
                    catch(Exception ex){
                        Log.write(ex);
                    }
                }
            }
            
            //now we need to reassemble the stitches by minimum spanning tree
            Log.write("Stitch calculation complete!", Color.YELLOW, txtLog);

            Log.write("Finding optimal application of pairwise stitches...", Color.YELLOW, txtLog);
            //calculate the maximum spanning tree for the stitches we have
            //by Prim's algorithm
            ArrayList<int[]> connectedNodes = new ArrayList<int[]>();

            //add a starting node
            for(int x = 0; x < gridXSize; x++){
                for(int y = 0; y < gridYSize; y++){ 
                    if(!imagePathGrid[x][y][0].isEmpty()){
                        int[] node = {x,y};
                        connectedNodes.add(node);
                        break;
                    }
                }       
                if(!connectedNodes.isEmpty()){
                    break;
                }
            }

            //connect the next node by finding max edge weight link to any
            //disconnected node from any connected node
            while(connectedNodes.size() < blocksPerZStep){
                ArrayList<StitchEdge> edges = getEdges(connectedNodes);
                Collections.sort(edges, Collections.reverseOrder());

                //edges are sorted in descending order, so pick the first
                //one that connects a new node
                for(StitchEdge edge : edges){
                    boolean alreadyFound = false;
                    for(int[] node : connectedNodes){
                        if(node[0] == edge.node2Index[0] && node[1] == edge.node2Index[1]){
                            alreadyFound = true;
                            break;
                        }
                    }
                    if(!alreadyFound){
                        //add this edge to the graph
                        Log.write("Added edge from (" + edge.node1Index[0] + "," + edge.node1Index[1] +
                               ") to (" + edge.node2Index[0] + "," + edge.node2Index[1] + ")", Color.BLACK, txtLog);
                        connectedNodes.add(edge.node2Index);

                        //set coordinates of node
                        int x = edge.node2Index[0];
                        int y = edge.node2Index[1];
                        if(edge.direction == Constants.EDGE_RIGHT){
                            stitches[x][y].coords[0] = stitches[x-1][y].coords[0] + stitches[x-1][y].coordsRight[0];
                            stitches[x][y].coords[1] = stitches[x-1][y].coords[1] + stitches[x-1][y].coordsRight[1];
                        }
                        if(edge.direction == Constants.EDGE_LEFT){
                            stitches[x][y].coords[0] = stitches[x+1][y].coords[0] + stitches[x+1][y].coordsLeft[0];
                            stitches[x][y].coords[1] = stitches[x+1][y].coords[1] + stitches[x+1][y].coordsLeft[1];
                        }
                        if(edge.direction == Constants.EDGE_UP){
                            stitches[x][y].coords[0] = stitches[x][y+1].coords[0] + stitches[x][y+1].coordsUp[0];
                            stitches[x][y].coords[1] = stitches[x][y+1].coords[1] + stitches[x][y+1].coordsUp[1];
                        }
                        if(edge.direction == Constants.EDGE_DOWN){
                            stitches[x][y].coords[0] = stitches[x][y-1].coords[0] + stitches[x][y-1].coordsDown[0];
                            stitches[x][y].coords[1] = stitches[x][y-1].coords[1] + stitches[x][y-1].coordsDown[1];
                        }
                        
                        break;
                    }
                } 
            }
        }
        else if(stitchAlgorithm == Constants.USER_STITCH){
            //user has selected the "user stitch" panel
            //means they want to use their own stitch
            if(! coordsFile.isEmpty()){
                //use the file they entered
                String str = FileAndDirOperations.readFileIntoString(coordsFile);
                String[] lines = str.split("\\n");
                for(int i = 0; i < lines.length; i++){
                    String line = lines[i];
                    //each line looks like this:
                    //block x=" + x + " y=" + y + " upper left at (" + stitches[x][y].coords[0] + "," + stitches[x][y].coords[1] + ")"
                    //just going to rely on that cause Java regex is such trash
                    
                    //find x and y coordinates of block
                    String[] toks = line.split("\\s+");
                    String xStr = toks[1].substring(toks[1].indexOf("=")+1, toks[1].length());
                    int x = Integer.parseInt(xStr);
                    
                    String yStr = toks[2].substring(toks[2].indexOf("=")+1, toks[2].length());
                    int y = Integer.parseInt(yStr);
                    
                    //find coordinates for this block
                    String coords0Str = (toks[toks.length-1].split(","))[0];
                    coords0Str = coords0Str.substring(1);
                    int coords0 = Integer.parseInt(coords0Str);
                    
                    String coords1Str = (toks[toks.length-1].split(","))[1];
                    coords1Str = coords1Str.substring(0, coords1Str.length()-1);
                    int coords1 = Integer.parseInt(coords1Str);
                    
                    print("Image coordinates: " + x + " " + y + " " + coords0 + " " + coords1);
                    stitches[x][y].coords[0] = coords0;
                    stitches[x][y].coords[1] = coords1;
                }
                Log.write("Applying stitch from coordinates text file", Color.YELLOW, txtLog);
            }
        }
        else{
        
        }
        
        Log.write(Log.getTimestamp() + "Generating output...", Color.YELLOW, txtLog);
        
         //we now have the best stitches! make a nice output image for each z-plane
         applyStitchToImagePaths(mainDir, imagePathGrid, gridZSize);
         
         //if there were additional dirs to stitch, process those now too
         for(int d = 0; d < extraDirs.size(); d++){
            ArrayList<String> dirImagePaths = ImageFileIO.getImageFilesFromDir(extraDirs.get(d));
            Log.write("" + dirImagePaths.size() + " images found in " + extraDirs.get(d));

            if(dirImagePaths.size() % blocksPerZStep != 0){
                Log.write("Warning: " + dirImagePaths.size() + " is not evenly divisible by your grid size of " + blocksPerZStep, Color.ORANGE, txtLog);
                return;
            }
            
            int dirZSize = dirImagePaths.size() / blocksPerZStep;
            String[][][] dirImagePathGrid = fixImageOrder(dirImagePaths, gridXSize, gridYSize, dirZSize, skipBlocks, scanType);
            applyStitchToImagePaths(extraDirs.get(d), dirImagePathGrid, dirZSize);
         }
        Log.write(Log.getTimestamp() + "Done stitching!", Color.YELLOW, txtLog);
    }
    
    private void applyStitchToImagePaths(String basedir, String[][][] imagePaths, int zSize){
        String dir = basedir + "/stitch/";
        File dirf = new File(dir);
        dirf.mkdirs();
        
        String txtFile = dir + "coords.txt";
        try{ 
            FileWriter out = new FileWriter(txtFile); 
            if(out != null){
            Log.write("Writing stitch coordinates into " + txtFile, Color.WHITE, txtLog);
                for(int x = 0; x < gridXSize; x++){
                    for(int y = 0; y < gridYSize; y++){
                        out.write("block x=" + x + " y=" + y + " upper left at (" + 
                            stitches[x][y].coords[0] + "," + stitches[x][y].coords[1] + ")" + 
                                System.getProperty("line.separator"));
                    }
                }
            }
            out.close();
        }
        catch(Exception ex){/* don't care if this fails really */}
        
        for(int z = 0; z < zSize; z++){
            Log.write("Writing stitched file z=" + z + " of " + zSize + " to " + dir, Color.WHITE, txtLog);
            ArrayList<String> planeImagePaths = new ArrayList<String>();
            ArrayList<StitchCoordinates> planeImageCoords = new ArrayList<StitchCoordinates>();
             for(int x = 0; x < gridXSize; x++){
                 for(int y = 0; y < gridYSize; y++){
                     if(!imagePaths[x][y][z].isEmpty()){
                         planeImagePaths.add(imagePaths[x][y][z]);
                         planeImageCoords.add(stitches[x][y]);
                     }
                 }
             }
             ImagePlus stitched = makeStitchedImage(planeImagePaths, planeImageCoords, imageWidth, imageHeight);
             FileSaver fs = new FileSaver(stitched);
             File outFile = new File(dir + "/z" + String.format("%04d", z) + ".tif");
             fs.saveAsTiff(outFile.getAbsolutePath());
         }
    }
    
    public ImagePlus makeStitchedImage(ArrayList<String> imagePaths, ArrayList<StitchCoordinates> stitches, int imageWidth, int imageHeight){
        //takes in a set of image paths and their coordinates
        //reads in the images and places them in the appropriate spots
        //assumes same indexing for imagePaths and stitches
        
        //find size of image to create
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;
        for(int i = 0; i < stitches.size(); i++){
            Log.write("image " + i + " x: " + stitches.get(i).coords[0] + " y: " + stitches.get(i).coords[1]);
            if(stitches.get(i).coords[0] < minX){
                minX = stitches.get(i).coords[0];
            }
            if(stitches.get(i).coords[1] < minY){
                minY = stitches.get(i).coords[1];
            }
            if(stitches.get(i).coords[0] > maxX){
                maxX = stitches.get(i).coords[0];
            }
            if(stitches.get(i).coords[1] > maxY){
                maxY = stitches.get(i).coords[1];
            }
        }
        
        //make image bounds big enough to fit an extra image in each direction
        maxX += imageWidth;
        maxY += imageHeight;
        
        
        //preprocessing -- determine source image type (8-bit or 16-bit)
        int imageBits = 16;
        for(int i = 0; i < imagePaths.size(); i++){
            if(imagePaths.get(i).isEmpty()){
                //we're on a skip block, so just leave that area black
                continue;
            }
            Opener o = new Opener();
            ImagePlus img = o.openImage(imagePaths.get(i));
            imageBits = img.getBitDepth();
        }
        
        //make the output file in the same type as the input image
        ImagePlus bi;
        if(imageBits == 8){
            bi = new ImagePlus("", new ByteProcessor(maxX - minX, maxY - minY));
        }
        else{
            bi = new ImagePlus("", new ShortProcessor(maxX - minX, maxY - minY));
        }
        
        //read images from files
        Opener o = new Opener();
        for(int i = 0; i < imagePaths.size(); i++){
            if(imagePaths.get(i).isEmpty()){
                //we're on a skip block, so just leave that area black
                continue;
            }
            
            
            //BufferedImage subImage = ImageFileIO.readImageFromPath(imagePaths.get(i));
            ImagePlus subImage = o.openImage(imagePaths.get(i));
                    
            if(applyFFT){
                //subImage = ImageOperations.fftBandPass(subImage, 3, 40, true, true);
            }
            
            int biX = stitches.get(i).coords[0] - minX;
            int biY = stitches.get(i).coords[1] - minY;

            for(int subX = 0; subX < subImage.getWidth(); subX++){
                for(int subY = 0; subY < subImage.getHeight(); subY++){
                    
                    //integrate pixels according to overlapType
                    if(overlapType.equalsIgnoreCase(Constants.OVERLAP_AVERAGE)){
                        //determine if we're in an overlap region
                        boolean inOverlap = false;
                        if(bi.getPixel(biX+subX, biY+subY)[0] > 0){
                            //probably overlap. Good enough, anyway.
                            inOverlap = true;
                        }
                        if(inOverlap){
                            int subRGB = subImage.getPixel(subX, subY)[0];
                            int biRGB = bi.getPixel(biX+subX, biY+subY)[0];
                            bi.getProcessor().putPixel(biX+subX, biY+subY, (subRGB+biRGB)/2);
                            //bi.setPixel(biX+subX, biY+subY, (subRGB+biRGB)/2);
                        }
                        else{
                            //not in overlap, so just take the subimage
                            int subRGB = subImage.getPixel(subX, subY)[0];
                            bi.getProcessor().putPixel(biX+subX, biY+subY, subRGB);
                            //bi.setRGB(biX+subX, biY+subY, subRGB);
                        }
                        
                    }
                    else if(overlapType.equalsIgnoreCase(Constants.OVERLAP_MAX)){
                        int biRGB = bi.getPixel(biX+subX, biY+subY)[0];
                        int subRGB = subImage.getPixel(subX, subY)[0];
                        if(biRGB > subRGB){
                            bi.getProcessor().putPixel(biX+subX, biY+subY, biRGB);
                            //bi.setRGB(biX+subX, biY+subY, biRGB);
                        }
                        else{
                            bi.getProcessor().putPixel(biX+subX, biY+subY, subRGB);
                            //bi.setRGB(biX+subX, biY+subY, subRGB);
                        }
                    }
                }
            }
        }
        return bi;
    }
    
    private ArrayList<StitchEdge> getEdges(ArrayList<int[]> nodes){
        
        ArrayList<StitchEdge> edges = new ArrayList<StitchEdge>();
        for(int i = 0; i < nodes.size(); i++){
            int x = nodes.get(i)[0];
            int y = nodes.get(i)[1];
            if(!imagePathGrid[x][y][0].isEmpty()){
                //edges are represented as being directed, so we use all 4

                if(x>0 && !imagePathGrid[x-1][y][0].isEmpty()){
                    StitchEdge edgeLeft = new StitchEdge();
                    edgeLeft.regression = stitches[x][y].regressionLeft;
                    edgeLeft.node1Index[0] = x;
                    edgeLeft.node1Index[1] = y;
                    edgeLeft.node2Index[0] = x-1;
                    edgeLeft.node2Index[1] = y;
                    edgeLeft.direction = Constants.EDGE_LEFT;
                    edges.add(edgeLeft);
                }                    

                if(y>0 && !imagePathGrid[x][y-1][0].isEmpty()){
                    StitchEdge edgeUp = new StitchEdge();
                    edgeUp.regression = stitches[x][y].regressionUp;
                    edgeUp.node1Index[0] = x;
                    edgeUp.node1Index[1] = y;
                    edgeUp.node2Index[0] = x;
                    edgeUp.node2Index[1] = y-1;
                    edgeUp.direction = Constants.EDGE_UP;
                    edges.add(edgeUp);
                }      

                if(x<gridXSize-1 && !imagePathGrid[x+1][y][0].isEmpty()){
                    StitchEdge edgeRight = new StitchEdge();
                    edgeRight.regression = stitches[x][y].regressionRight;
                    edgeRight.node1Index[0] = x;
                    edgeRight.node1Index[1] = y;
                    edgeRight.node2Index[0] = x+1;
                    edgeRight.node2Index[1] = y;
                    edgeRight.direction = Constants.EDGE_RIGHT;
                    edges.add(edgeRight);
                }      

                if(y<gridYSize-1 && !imagePathGrid[x][y+1][0].isEmpty()){
                    StitchEdge edgeDown = new StitchEdge();
                    edgeDown.regression = stitches[x][y].regressionDown;
                    edgeDown.node1Index[0] = x;
                    edgeDown.node1Index[1] = y;
                    edgeDown.node2Index[0] = x;
                    edgeDown.node2Index[1] = y+1;
                    edgeDown.direction = Constants.EDGE_DOWN;
                    edges.add(edgeDown);
                }      
            }
        }
        return edges;
    }
    
    public static String[][][] fixImageOrder(ArrayList<String> imagePaths, int gridXSize, int gridYSize, int gridZSize, Integer[] skipBlocks, String scanType){
        //Rearrange the order of the images to fit a regular coordinate system
        //so x=0 y=0 will be the upper left, x=1 y=0 will be to the right of it, 
        //and x=0 y=1 will be below it 
        //Inserts an empty string in the position of any skipBlocks
        String[][][] imagePathGrid = new String[gridXSize][gridYSize][gridZSize];
        
        if(scanType.equalsIgnoreCase(Constants.SCAN_BIDIRECTIONAL)){
            int index = 0;
            for(int z = 0; z < gridZSize; z++){
                for(int y = 0; y < gridYSize; y++){
                    for(int x = 0; x < gridXSize; x++){
                        
                        //check if we're at a skip block
                        boolean skipThisBlock = false;
                        int blockIndex = y*gridXSize + x;
                        for(int s = 0; s < skipBlocks.length; s++){
                            if(blockIndex == skipBlocks[s]){
                                skipThisBlock = true;
                            }
                        }
                        if(skipThisBlock){
                            //x is backwards for every other Y-row
                            if(y % 2 == 1){
                                imagePathGrid[gridXSize-x-1][y][z] = "";
                            }
                            else{
                                imagePathGrid[x][y][z] = "";
                            }
                        }
                        else{
                            //This is a good block. Use it!
                            //x is backwards for every other Y-row
                            if(y % 2 == 1){
                                imagePathGrid[gridXSize-x-1][y][z] = imagePaths.get(index);
                            }
                            else{
                                imagePathGrid[x][y][z] = imagePaths.get(index);
                            }
                            index++;
                        }
                    }
                }
            }
        }
        else if(scanType.equals(Constants.SCAN_LEFTTORIGHT)){
            int index = 0;
            for(int z = 0; z < gridZSize; z++){
                for(int y = 0; y < gridYSize; y++){
                    for(int x = 0; x < gridXSize; x++){
                        //check if we're at a skip block
                        boolean skipThisBlock = false;
                        int blockIndex = y*gridXSize + x;
                        for(int s = 0; s < skipBlocks.length; s++){
                            if(blockIndex == skipBlocks[s]){
                                skipThisBlock = true;
                            }
                        }
                        if(skipThisBlock){
                            imagePathGrid[x][y][z] = "";
                        }
                        else{
                            //This is a good block. Use it!
                            imagePathGrid[x][y][z] = imagePaths.get(index);
                            index++;
                        }
                    }
                }
            }
        }
        return imagePathGrid;
    }

    public StitchCoordinates findStitch(BufferedImage image1, BufferedImage image2){
        //returns coordinates and regression of a stitch
        StitchCoordinates stitchCoords = new StitchCoordinates();
        stitchCoords.regression = 0.0;
        
        ImagePlus imp1 = new ImagePlus("imp1", image1);
        ImagePlus imp2 = new ImagePlus("imp2", image2);
        
        final Stitching_2D stitch = new Stitching_2D();
        stitch.checkPeaks = numPeaks;
        stitch.fusedImageName = "Fused " + imp1.getTitle() + " " + imp2.getTitle();
        stitch.fuseImages = false;
        stitch.handleRGB1 = null; //single channel only
        stitch.handleRGB2 = null;
        stitch.image1 = imp1.getTitle();
        stitch.image2 = imp2.getTitle();

        stitch.windowing = false;
        
        //final ImagePlus imp1b =  new ImagePlus("Imp1 B", imp1.getProcessor().duplicate());
        final ImageProcessor ip1 = imp1.getProcessor().duplicate();
        stitch.imp1 = imp1;

        //final ImagePlus imp2b = new ImagePlus("Imp2 B", imp2.getProcessor().duplicate());

        final ImageProcessor ip2 = imp2.getProcessor().duplicate();
        stitch.imp2 = imp2;
        
        stitch.doLogging = false;
        stitch.computeOverlap = true;

        try
        {
                stitch.work();

                stitchCoords.regression = stitch.getCrossCorrelationResult().R;
                Point2D p = stitch.getTranslation();
                stitchCoords.coords[0] = p.x;
                stitchCoords.coords[1] = p.y;
                Log.write("translation: " + p.toString());
                Log.write("regression: " + stitchCoords.regression);
                Log.write("--------------");
        }
        catch (Exception ex)
        {
            Log.write("Stitching error!");
            Log.write(ex);
        }

        imp1.setProcessor(imp1.getTitle(), ip1);
        imp2.setProcessor(imp2.getTitle(), ip2);

        return stitchCoords;
    }
    
    private void print(String str){
        System.out.println(str);
    }
}
