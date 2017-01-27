/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools;

import confocaltools.imageTools.ImageFileIO;
import confocaltools.imageTools.ImageOperations;
import confocaltools.imageTools.ImageStitcher;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class CommandLine {
    
    public static void print(String s){
        System.out.println(s);
    }
    
    public static void printHelp(){
        print("To run the GUI: " + Constants.PROGRAM_NAME);	
        print("");
        print("Command line modes...");
        print("----");
        print("To fix contrast in images: " + Constants.PROGRAM_NAME + " fft-bandpass srcImage.tif outputImage.tif [params]");	
        print("");
        print("The optional [params] settings for contrast fixing...");
        print("-filterSmall: Features smaller than this will be removed. Default 3.");
        print("-filterLarge: Features larger than this will be removed. Default 40.");
        print("-autoScale: Perform histogram scaling. Default true.");
        print("-autoSaturate: Perform automatic saturation. Default true.");
        print("");
        print("Example: " + Constants.PROGRAM_NAME + " fft-bandpass srcImage.tif targetImage.tif outputImage.tif -filterSmall 5 -filterLarge 20 -autoScale false -autoSaturate false");
        print("");
        print("----");
        print("To stitch a directory containing images: " + Constants.PROGRAM_NAME + " stitch imageDirPath outputDir gridSizeX gridSizeY [params]");	
        print("\tIt is assumed that the input image filenames are in a bidirectional (snake-like) order.");
        print("\tSo for 3x3 grid, the files should go in this order, repeated for each Z-position:");
        print("\t 123");
        print("\t 654");
        print("\t 789");
        print("\t Export your data as single-channel tifs from Zen to get files laid out this way.");
        print("");
        print("The optional [params] settings for stitching...");
        print("-skipBlocks: If your confocal scan was over a non-square area, enter in the missing blocks.");
        print("\t For example, if your scan looks like:");
        print("\t xxxx");
        print("\t  xxx");
        print("\t  xxx");
        print("\t xxx ");
        print("\t You would run: " + Constants.PROGRAM_NAME + " stitch 4 4 -skipBlocks \"8 9 13\"");
        print("-fixContrast: Will run FFT bandpass on input images before stitching. Improves stitching, especially on bad data. Default true.");
        print("-threshold: Will use Otsu's thresholding to remove background before stitching. Default true.");
        print("Example: " + Constants.PROGRAM_NAME + " stitch myImageDir/images/ 3 3 -skipBlocks \"7\" -threshold false");
        
        return;
    }
    
    public static void handleArgs(String[] args){
        if(args[0].equalsIgnoreCase("fft-bandpass")){
            String srcPath = args[1];
            String outPath = args[2];
            Opener o = new Opener();
            ImagePlus src = o.openImage(srcPath);
            
            if(src == null){
                String curDir = System.getProperty("user.dir");
                print("Looking for images in directory " + curDir);
                //try specifying local path (maybe in same dir)
                srcPath = curDir + "/" + srcPath;
                outPath = curDir + "/" + outPath;
                src = o.openImage(srcPath);
            }
            
            int filterSmall = 3;
            int filterLarge = 40;
            boolean doScalingDia = true;
            boolean saturateDia = true;
            
            for(int i = 3; i < args.length; i++){
                if(args[i].equalsIgnoreCase("-filterSmall")){
                    filterSmall = Integer.parseInt(args[i+1]);
                }
                if(args[i].equalsIgnoreCase("-filterLarge")){
                    filterLarge = Integer.parseInt(args[i+1]);
                }
                if(args[i].equalsIgnoreCase("-autoScale")){
                    if(! args[i+1].equalsIgnoreCase("true")){
                        doScalingDia = false;
                    }
                }
                if(args[i].equalsIgnoreCase("-autoSaturate")){
                    if(! args[i+1].equalsIgnoreCase("true")){
                        saturateDia = false;
                    }
                }
            }
            BufferedImage out = ImageOperations.fftBandPass(ImageFileIO.getBufferedFromImagePlus(src), filterSmall, filterLarge, doScalingDia, saturateDia);
            FileSaver fs = new FileSaver(new ImagePlus("outImage",out));
            fs.saveAsTiff(outPath);
        }
        else if(args[0].equalsIgnoreCase("stitch")){
            String srcDir = args[1];
            String outDir = args[2];
            int gridXSize = Integer.parseInt(args[3]);
            int gridYSize = Integer.parseInt(args[4]);
            
            Integer[] skipBlocks = new Integer[0];
            boolean fixContrast = true;
            boolean threshold = true;
            
            for(int i = 5; i < args.length; i++){
                if(args[i].equalsIgnoreCase("-skipBlocks")){
                    ArrayList<Integer> skipBlocksList = new ArrayList<Integer>();
                    while(! args[i].endsWith("\"")){
                        skipBlocksList.add(Integer.parseInt(args[i+1].replaceAll("\"", "")));
                        i++;
                    }
                    skipBlocks = skipBlocksList.toArray(skipBlocks);
                }
                if(args[i].equalsIgnoreCase("-fixContrast")){
                    if(! args[i+1].equalsIgnoreCase("true")){
                        fixContrast = false;
                    }
                }
                if(args[i].equalsIgnoreCase("-threshold")){
                    if(! args[i+1].equalsIgnoreCase("true")){
                        threshold = false;
                    }
                }
            }
            
            ImageStitcher stitcher = new ImageStitcher();
            //stitcher.stitchDir(srcDir, outDir, gridXSize, gridYSize, skipBlocks, fixContrast, threshold, "snake");
        }
        else{
            printHelp();
        }
    }
}
