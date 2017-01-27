/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools.imageTools;

import confocaltools.Constants;
import confocaltools.Log;
import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JTextPane;

/**
 * A set of static methods for processing individual images and sets of images
 * @author walkert
 */
public class ImageOperations {
    //all the stuff in here only works on 12-bit images right now
    //and it assumes all the images have the same dimensions
    //fix that someday, maybe
    
    public static final int imageType = BufferedImage.TYPE_USHORT_GRAY;
    
    public static BufferedImage subtract(BufferedImage src, BufferedImage toSubtract){
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
        int[] srcPixel = new int[1];
        int[] toSubtractPixel = new int[1];
        int[] destPixel = new int[1];
        for(int i = 0; i < src.getHeight(); i++){    
            for(int j = 0; j < src.getWidth(); j++){
                src.getRaster().getPixel(j, i, srcPixel);
                toSubtract.getRaster().getPixel(j,i,toSubtractPixel);
                destPixel[0] = srcPixel[0] - toSubtractPixel[0];
                dest.getRaster().setPixel(j, i, destPixel);
            }          
        }
        return dest;
    }
    
    public static void greyOut(int x0, int x1, int y0, int y1, int z0, int z1, ArrayList<BufferedImage> images){ 
        //greys out a given volume in the buffered image stack.
        //Only works for 12-bit grayscale images.
        int grey = Constants.GREY_12BIT;
        
        for(int z = z0; z <= z1; z++){
            if(z < images.size() && z >= 0){
                for(int x = x0; x <= x1; x++){
                    for(int y = y0; y <= y1; y++)
                    images.get(z).setRGB(x, y, grey);
                }
            }
        }
    }
    
    public static BufferedImage max(ArrayList<BufferedImage> images){
        //Takes the maximum value of each pixel across all bufferedimages
        //Assumes all images have the same dimensions
        
        int width = images.get(0).getWidth();
        int height = images.get(0).getHeight();
        
        double[] runningAverage = null;
        
        int[] maxImage = null;
        int index = 1;
        for(BufferedImage im: images){
            if(maxImage == null && im != null){
                //if this is the first image, create maxImage array from it
                maxImage = new int[width * height];
                im.getRaster().getPixels(0, 0, width, height, maxImage);
                runningAverage = new double[width*height];
                im.getRaster().getPixels(0, 0, width, height, runningAverage);
            }
            else{
                //update max-image
                int[] newImage = new int[width * height];
                if(maxImage.length != newImage.length){
                    Log.write("Error: Directory contains images of different sizes.");
                }
                im.getRaster().getPixels(0, 0, width, height, newImage);

                for(int k = 0; k < newImage.length; k++){
                    if(newImage[k] > maxImage[k]){
                        maxImage[k] = newImage[k];
                    }
                    runningAverage[k] = ((runningAverage[k]*index) + newImage[k]) / (index+1);
                }
            }
            index++;
        }        
        
        
        BufferedImage max = new BufferedImage(width, height, imageType);
        max.getRaster().setPixels(0, 0, width, height, maxImage);
        
        return max;
    }
    
    public static ArrayList<BufferedImage> fftBandPass(ArrayList<BufferedImage> srcImages, JTextPane txtLog, double filterSmall, double filterLarge){
        ArrayList<BufferedImage> bandPassed = new ArrayList<BufferedImage>();
        for(int i = 0; i < srcImages.size(); i++){
            bandPassed.add(fftBandPass(srcImages.get(i), filterSmall, filterLarge, true, true));
        }
        return bandPassed;
    }
    
    public static BufferedImage fftBandPass(BufferedImage src, double filterSmall, double filterLarge, boolean doScalingDia, boolean saturateDia){
        //runs the imageJ 
        FFT_Filter filter = new FFT_Filter();
        
        filter.setFilterSmallDia(filterSmall); //3.0 will blur out the high-freq noise
        filter.setFilterLargeDia(filterLarge); //40.0 will remove low-freq intensity variances
        
        filter.setDoScalingDia(true); //scale brightness afterwards
        filter.setSaturateDia(true); //saturate image afterwards
        ImagePlus imp = new ImagePlus("arg", src);
        filter.setup("ARG ARG ARG!", imp);
        filter.filter(imp.getProcessor());
        return ImageFileIO.getBufferedFromImagePlus(imp);
    }
    
    public static BufferedImage gaussianBlur(BufferedImage src, double radius){
        ImageProcessor iproc = new ShortProcessor(src);
        GaussianBlur blur = new GaussianBlur();
        blur.blur(iproc,radius);
        return iproc.getBufferedImage();
    }
    
    public static BufferedImage highPass(BufferedImage src, int threshold){
        //threshold is in 0..4095
        //colors below threshold become black
        
        int height = src.getHeight();
        int width = src.getWidth();
        
        int[] pixelValues = new int[width*height];
        src.getRaster().getPixels(0, 0, width, height, pixelValues);
        
        for(int i = 0; i < height; i++){
            for(int j = i*width; j < width*(i+1); j++){
                if(pixelValues[j] < threshold){
                    pixelValues[j] = 0;
                }
            }
        }
        BufferedImage dst = new BufferedImage(width, height, imageType);
        dst.getRaster().setPixels(0, 0, width, height, pixelValues);
        return dst;
    }
    
    public static BufferedImage lowPass(BufferedImage src, int threshold){
        //threshold is in 0..4095
        //colors below threshold become black
        
        int height = src.getHeight();
        int width = src.getWidth();
        
        int[] pixelValues = new int[width*height];
        src.getRaster().getPixels(0, 0, width, height, pixelValues);
        
        for(int i = 0; i < height; i++){
            for(int j = i*width; j < width*(i+1); j++){
                if(pixelValues[j] > threshold){
                    pixelValues[j] = 0;
                }
            }
        }
        BufferedImage dst = new BufferedImage(width, height, imageType);
        dst.getRaster().setPixels(0, 0, width, height, pixelValues);
        return dst;
    }
    
    public static ImageProcessor proc(BufferedImage image){
         ImageProcessor ipFreely = new ShortProcessor(image);
         return ipFreely;
    }
    
    public static BufferedImage avgOfBufferedImages(ArrayList<BufferedImage> images){
        int height = images.get(0).getHeight();
        int width = images.get(0).getWidth();
        
        double[] runningAvgPixelValues = new double[width*height];
        
        for(int k = 0; k < images.size(); k++){
            int[] pixelValues = new int[width*height];
            images.get(k).getRaster().getPixels(0, 0, width, height, pixelValues);
            
            for(int i = 0; i < height; i++){
                for(int j = i*width; j < width*(i+1); j++){
                    runningAvgPixelValues[j] = (new Double(pixelValues[j]) + runningAvgPixelValues[j] * k) / new Double(k+1);
                }
            }
        }
        
        int[] avgPixelValues = new int[width*height];
        for(int i = 0; i < width*height; i++){
            avgPixelValues[i] = (int)Math.round(runningAvgPixelValues[i]);
        }    
        
        BufferedImage dst = new BufferedImage(width, height, imageType);
        dst.getRaster().setPixels(0, 0, width, height, avgPixelValues);
        return dst;
    }
    
    public static int[] getPixelCube(ArrayList<BufferedImage> imageStack, int ctrX, int ctrY, int ctrZ, int size){
        int[] pixelValues = new int[size*size*size];
        int index = 0;
        for(int z = ctrZ - size/2; z <= ctrZ + size/2; z++){
            BufferedImage slice = imageStack.get(z);
            for(int x = ctrX - size/2; x <= ctrX + size/2; x++){
                for(int y = ctrY - size/2; y <= ctrY + size/2; y++){
                    pixelValues[index] = slice.getRaster().getSample(x, y, 0);
                    index++;
                }
            }
        }
        return pixelValues;
    }
    
    public static BufferedImage convertRenderedImage(RenderedImage img) {
        if (img instanceof BufferedImage) {
                return (BufferedImage)img;	
        }	
        ColorModel cm = img.getColorModel();
        int width = img.getWidth();
        int height = img.getHeight();
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        Hashtable properties = new Hashtable();
        String[] keys = img.getPropertyNames();
        if (keys!=null){
            for (int i = 0; i < keys.length; i++) {
                properties.put(keys[i], img.getProperty(keys[i]));
            }
        }
        BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
        img.copyData(raster);
        return result;
    }
   
    public static double[] faceImageOffset(BufferedImage src, BufferedImage target){
        //slides src image to the left and right
        //determines where best overlap is (minimum difference) 
        //used in aligning non-overlapping slices by their faces
        
        //requires at least a 50% overlap between the two images 
        //(extremely generous for all known applications.)
        int imageWidth = src.getWidth();
        int imageHeight = src.getHeight();
        
        int bestOffset = 0;
        double bestOffsetDifference = Double.MAX_VALUE;
        
        //take the src image and slide it along target (positive offset)
        for(int w = 0; w < imageWidth/2; w++){
            double meanDifference = 0;
            for(int x = 0; x+w < imageWidth; x++){
                for(int y = 0; y < imageHeight; y++){
                    meanDifference = meanDifference + Math.abs(src.getRGB(x,y) - target.getRGB(x+w,y));
                }
            }
            meanDifference = meanDifference / ((imageWidth-w)*imageHeight);
            if(meanDifference < bestOffsetDifference){
                bestOffset = w;
                bestOffsetDifference = meanDifference;
            }
        }
        
        //take the target image and slide it along src (negative offset)
        for(int w = 0; w < imageWidth/2; w++){
            double meanDifference = 0;
            for(int x = 0; x+w < imageWidth; x++){
                for(int y = 0; y < imageHeight; y++){
                    meanDifference = meanDifference + Math.abs(src.getRGB(x+w,y) - target.getRGB(x,y));
                }
            }
            meanDifference = meanDifference / ((imageWidth-w)*imageHeight);
            if(meanDifference < bestOffsetDifference){
                bestOffset = -w;
                bestOffsetDifference = meanDifference;
            }
        }
        
        double[] offsetAndDifference = new double[2];
        offsetAndDifference[0] = bestOffset;
        offsetAndDifference[1] = bestOffsetDifference;
        return offsetAndDifference;
    }
}
