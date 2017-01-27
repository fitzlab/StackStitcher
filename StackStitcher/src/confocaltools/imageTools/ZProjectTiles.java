/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools.imageTools;

import confocaltools.Log;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.ZProjector;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JTextPane;
/**
 *
 * @author theo
 */

//Applies a Z-projection to a set of images before stitching.
//Create a directory, writes the Z-projected images into it, and returns the path.

public class ZProjectTiles {
    public static final int Z_PROJECT_MAX = ZProjector.MAX_METHOD;
    public static final int Z_PROJECT_MEAN = ZProjector.AVG_METHOD;
    public static final int Z_PROJECT_STD = ZProjector.SD_METHOD;
    public static final int Z_PROJECT_NONE = -1;

    public static String zProjectTiles(String mainDir, int numTiles, int zProjectMethod, JTextPane txtLog){
        
        String outDirPath = mainDir + "/zProject";
        File outDir = new File(outDirPath);
        outDir.mkdirs();
        
        
        Opener o = new Opener();
        
        //Get all images from baseDir
        ArrayList<String> imagePaths = ImageFileIO.getImageFilesFromDir(mainDir);
        Log.write("Calculating Z-projection on " + imagePaths.size() + " images found in " + mainDir, Color.WHITE, txtLog);
        
        //Make a Z-series ImagePlus for each tile
        ImageStack tileZStack = null;
        int zSize = imagePaths.size() / numTiles;
        if(zSize < 3 && zProjectMethod == Z_PROJECT_STD){
            Log.write("Warning: Standard deviation Z-projection requires a z-stack of depth 3 or more.");
        }
        for(int t=0; t < numTiles; t++){
            Log.write("Z-projecting tile (" + (t+1) + " / " + numTiles + ")", Color.RED, txtLog);
            for(int z = 0; z < zSize; z++){
                int imgPathPos = z*numTiles+t;
                ImagePlus imp = o.openImage(imagePaths.get(imgPathPos));
                if(z==0){
                    //initialize stack
                    tileZStack = new ImageStack(imp.getWidth(), imp.getHeight());
                }
                //Add tile to stack
                tileZStack.addSlice("z_"+z, imp.getProcessor());
            }
            
            //Z-project stack
            ZProjector z = new ZProjector();
            z.setImage(new ImagePlus("tile_"+t, tileZStack));
            z.setMethod(zProjectMethod);
            z.doProjection();
            
            //Write projected results to outDir
            FileSaver fs = new FileSaver(z.getProjection());
            fs.saveAsTiff(outDirPath + "/tile_" + String.format("%05d", t) + ".tif");
            
        }
        Log.write("Wrote Z-projected files to " + outDirPath, Color.WHITE, txtLog);
        
        return outDirPath;
    }
    
}
