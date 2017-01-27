/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools.imageTools;

import confocaltools.Constants;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author walkert
 */
public class ImageScaling {
    //contains static methods for performing global scaling on images
    //works for color images, grayscale, 3D, etc
    
    
    public static BufferedImage rangeScale(BufferedImage src){
        //this only works for USHORT grayscale
        //right now, we just assume everything else is color
        if(src.getType() != BufferedImage.TYPE_USHORT_GRAY){
            return rangeScaleColors(src);
        }
        
        double[] minMax = findMinMax(src);
        return rangeScaleGivenMinMax(src, minMax);
    }
    
    public static BufferedImage rangeScaleColors(BufferedImage src){
        double[] minMax = findMinMaxColors(src);
        return rangeScaleColorsGivenMinMax(src, minMax);
    }
    
    public static double[] findMinMax(BufferedImage src){
        double[] minMax = new double[2]; //[0] = min, [1] = max.
        int height = src.getHeight();
        int width = src.getWidth();
        
        
        int[] pixelValues = new int[width*height];
        src.getRaster().getPixels(0, 0, width, height, pixelValues);
        
        int maxValue = 0;
        int minValue = Constants.SHORT_WHITE;
        
        for(int i = 0; i < height; i++){
            for(int j = i*width; j < width*(i+1); j++){
                if(pixelValues[j] < minValue && minValue != 0){
                    minValue = pixelValues[j];
                }
                if(pixelValues[j] > maxValue){
                    maxValue = pixelValues[j];
                }
            }
        }
        
        minMax[0] = minValue;
        minMax[1] = maxValue;
        
        return minMax;
    }
    
    public static double[] findMinMax(ArrayList<BufferedImage> srcImages){
        double[] minMax = new double[]{Constants.SHORT_WHITE, 0};
        
        for(int i = 0; i < srcImages.size(); i++){
            double[] imgMinMax = findMinMax(srcImages.get(i));
            if(imgMinMax[0] < minMax[0]){
                minMax[0] = imgMinMax[0];
            }
            if(imgMinMax[1] > minMax[1]){
                minMax[1] = imgMinMax[1];
            }
        }
        return minMax;
    }
    
    public static double[] findMinMaxColors(BufferedImage src){
        double[] minMax = new double[6]; 
        //[0] = minRed, [1] = maxRed, 
        //[2] = minGreen, [3] = maxGreen, 
        //[4] = minBlue, [5] = maxBlue
        
        int height = src.getHeight();
        int width = src.getWidth();
        
        int minRed = 99999;
        int minGreen = 99999;
        int minBlue = 99999;
        
        int maxRed = 0;
        int maxGreen = 0;
        int maxBlue = 0;
        
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Color c = new Color(src.getRGB(x, y));
                if(c.getRed() > maxRed){
                    maxRed = c.getRed();
                }
                if(c.getGreen() > maxGreen){
                    maxGreen = c.getGreen();
                }
                if(c.getBlue() > maxBlue){
                    maxBlue = c.getBlue();
                }
                
                if(c.getRed() < minRed){
                    minRed = c.getRed();
                }
                if(c.getGreen() < minGreen){
                    minGreen = c.getGreen();
                }
                if(c.getBlue() < minBlue){
                    minBlue = c.getBlue();
                }
            }
        }
        minMax[0] = minRed;
        minMax[1] = maxRed;
        minMax[2] = minGreen;
        minMax[3] = maxGreen;
        minMax[4] = minBlue;
        minMax[5] = maxBlue;
        
        return minMax;
    }
    
    public static double[] findMinMaxColors(ArrayList<BufferedImage> srcImages){
        double[] minMax = new double[]{Constants.BYTE_WHITE, 0, Constants.BYTE_WHITE, 0, Constants.BYTE_WHITE, 0};
        
        for(int i = 0; i < srcImages.size(); i++){
            double[] imgMinMax = findMinMaxColors(srcImages.get(i));
            if(imgMinMax[0] < minMax[0]){
                minMax[0] = imgMinMax[0];
            }
            if(imgMinMax[2] < minMax[2]){
                minMax[2] = imgMinMax[2];
            }
            if(imgMinMax[4] < minMax[4]){
                minMax[4] = imgMinMax[4];
            }
            
            if(imgMinMax[1] > minMax[1]){
                minMax[1] = imgMinMax[1];
            }
            if(imgMinMax[3] > minMax[3]){
                minMax[3] = imgMinMax[3];
            }
            if(imgMinMax[5] > minMax[5]){
                minMax[5] = imgMinMax[5];
            }
        }
        return minMax;
    }
    
    public static BufferedImage rangeScaleColorsGivenMinMax(BufferedImage src, double[] minMax){
        //for range-scaling a colored image.
        //Each color is scaled independently. 
        //If a color only occurs at one intensity, that intensity is unchanged.
        int height = src.getHeight();
        int width = src.getWidth();
        
        BufferedImage rangeScaledImage = new BufferedImage(width, height, src.getType());
        
        int minRed = (int) minMax[0];
        int maxRed = (int) minMax[1];
        int minGreen = (int) minMax[2];
        int maxGreen = (int) minMax[3];
        int minBlue = (int) minMax[4];
        int maxBlue = (int) minMax[5];
        
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Color c = new Color(src.getRGB(x, y));
                
                int rScaled = 0, gScaled = 0, bScaled = 0;
                if(maxRed <= minRed){
                    rScaled = c.getRed();
                }
                else{
                    double newR = new Double((c.getRed() - minRed)) / (maxRed - minRed);
                    rScaled = (int) Math.round(newR * Constants.BYTE_WHITE);
                }
                
                if(maxGreen <= minGreen){
                    gScaled = c.getGreen();
                }
                else{
                    double newG = new Double((c.getGreen() - minGreen)) / (maxGreen - minGreen);
                    gScaled = (int) Math.round(newG * Constants.BYTE_WHITE);
                }
                
                if(maxBlue <= minBlue){
                    bScaled = c.getBlue();
                }
                else{
                    double newB = new Double((c.getBlue() - minBlue)) / (maxBlue - minBlue);
                    bScaled = (int) Math.round(newB * Constants.BYTE_WHITE);
                }
                Color cScaled = new Color(rScaled, gScaled, bScaled);
                rangeScaledImage.setRGB(x, y, cScaled.getRGB());
            }
        }
        
        return rangeScaledImage;
    }
        
    public static BufferedImage rangeScaleGivenMinMax(BufferedImage src, double[] minMax){
        //makes the darkest pixel black and the lightest pixel white, and scales
        //all pixels in between.
        //Assumes the input image is a TYPE_USHORT_GRAY.
        
        int height = src.getHeight();
        int width = src.getWidth();
        
        int[] pixelValues = new int[width*height];
        src.getRaster().getPixels(0, 0, width, height, pixelValues);
        
        int minValue = (int) minMax[0];
        int maxValue = (int) minMax[1];
        
        for(int i = 0; i < height; i++){
            for(int j = i*width; j < width*(i+1); j++){
                double scaledColor = new Double(pixelValues[j] - minValue) / (maxValue - minValue) * Constants.SHORT_WHITE;
                pixelValues[j] = (int) Math.round(scaledColor);
            }
        }
        
        BufferedImage rangeScaledImage = new BufferedImage(width, height, src.getType());
        rangeScaledImage.getRaster().setPixels(0, 0, width, height, pixelValues);
        
        return rangeScaledImage;
    }
    
}
