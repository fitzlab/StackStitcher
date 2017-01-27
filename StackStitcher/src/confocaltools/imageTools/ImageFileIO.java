/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools.imageTools;

import confocaltools.FileAndDirOperations;
import confocaltools.Log;
import confocaltools.Utility;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;

/** contains static functions for reading / writing images
 */
public class ImageFileIO {
    
    public static String showSelectDirDialog(){
        String dir = "";
        
        JFileChooser chooser = new JFileChooser();
        if(new File("G:/netbeans-projects/Image Processing Datasets").exists()){
            chooser.setCurrentDirectory(new java.io.File("G:/netbeans-projects/Image Processing Datasets"));
        }
        else{
            chooser.setCurrentDirectory(new java.io.File("."));
        }
        chooser.setDialogTitle("Open Image Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            dir = chooser.getSelectedFile().getAbsolutePath();
        }
        
        dir = dir.replaceAll("\\\\", "/") + "/";
        return dir;
    }
    
    public static String showSelectFileDialog(){
        String file = "";
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Open File");
        chooser.setSelectedFile(new File("coords.txt"));
        
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile().getAbsolutePath();
        }
        
        file = file.replaceAll("\\\\", "/");
        return file; 
    }
    
    
    public static ArrayList<String> getImageFilesFromDir(String path){
        ArrayList<String> imageFileList = new ArrayList<String>(); // for random access use
        try{
            if(!path.endsWith("/")){
                path += "/";
            }
            System.out.println("loading image files list");
            imageFileList = FileAndDirOperations.getDirContents(path, false);
            System.out.println("done loading image files list");
        }
        catch(Exception ex){
            Log.write(ex);
        }
        for(int i = imageFileList.size() - 1; i >= 0; i--){
            if(! imageFileList.get(i).endsWith(".tif")){
                imageFileList.remove(i);
            }
        }
        
        Collections.sort(imageFileList, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return Utility.naturalSortCompare(o1, o2);
            }});
        
        return imageFileList;
    }
    
    public static void saveImageDialog(BufferedImage img, String defaultFileName){
        //saves a single image as a TIFF
        JFileChooser fileChooser = new JFileChooser();  
        fileChooser.setCurrentDirectory(new java.io.File("."));
        File f = new File(defaultFileName);
        fileChooser.setSelectedFile(f);
        fileChooser.setDialogTitle("Save Max TIFF As...");

        int returnVal = fileChooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File outFile = fileChooser.getSelectedFile(); 
            ImagePlus ip = new ImagePlus(defaultFileName,img);
            FileSaver fs = new FileSaver(ip);
            fs.saveAsTiff(outFile.getAbsolutePath());
        }                  
    }
    
    public static void saveImageToFile(BufferedImage img, String filePath){
            File outFile = new File(filePath);
            ImagePlus ip = new ImagePlus(filePath,img);
            FileSaver fs = new FileSaver(ip);
            fs.saveAsTiff(outFile.getAbsolutePath());
    }
    
    public static String saveImageSet(ArrayList<BufferedImage> images, ArrayList<String> imageNames, JTextPane txtLog){
        //saves a set of TIFFs
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Open Image Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if(imageNames == null){
            //just generate a series of numbers
            imageNames = new ArrayList<String>();
            for(int i = 0; i < images.size(); i++){
                imageNames.add(String.format("%05d", i) + ".tif");
            }
        }
        
        String dir = "";
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            dir = chooser.getSelectedFile().getAbsolutePath();
            if(!(new File(dir).exists())){
                new File(dir).mkdirs();
            }
            if(! FileAndDirOperations.isDirEmpty(dir)){
                Log.write("Could not write files! Output dir " + dir + 
                        " is not empty! Please use an empty directory.", Color.RED, txtLog);
                return "";
            }
            for(int i = 0; i < images.size(); i++){
                
                File outFile = new File(dir + "/" + imageNames.get(i)); 
                ImagePlus ip = new ImagePlus(imageNames.get(i), images.get(i));
                
                FileSaver fs = new FileSaver(ip);
                fs.saveAsTiff(outFile.getAbsolutePath());                
            }
        }  
        
        return dir;
    }
    
    
    public static BufferedImage readImageFromPath(String imagePath){
        //reads a BufferedImage from a given spot
        //uses JAI so that it can read TIFFs as well
        BufferedImage im = null;
        try{
            /*
            SeekableStream s = new FileSeekableStream(new File(imagePath));
            TIFFDecodeParam param = null;
            ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
            System.out.println("Number of images in this TIFF: " + dec.getNumPages());
            RenderedImage r =
            new NullOpImage(dec.decodeAsRenderedImage(0),
                    null,
                    OpImage.OP_IO_BOUND,
                    null);

            im = convertRenderedImage(r);*/
            
            Opener o = new Opener();
            ImagePlus src = o.openImage(imagePath);
            im = getBufferedFromImagePlus(src);
        }
        catch(Exception ex){
            Log.write("Couldn't read image: " + imagePath); 
        }
        return im;
    }

    public static BufferedImage getBufferedFromImagePlus(ImagePlus ip){
        //imageplus.getBufferedImage() screws up the pixel depth! Use this instead.
        if(ip.getProcessor().getClass().equals(ShortProcessor.class)){
            ShortProcessor sp = (ShortProcessor) ip.getProcessor();
            return sp.get16BitBufferedImage();
        }
        else if(ip.getProcessor().getClass().equals(FloatProcessor.class)){
            FloatProcessor fp = (FloatProcessor) ip.getProcessor();
            ShortProcessor sp = (ShortProcessor) fp.convertToShort(false);
            return sp.get16BitBufferedImage();
        }
        else if(ip.getProcessor().getClass().equals(ByteProcessor.class)){
            ByteProcessor bp = (ByteProcessor) ip.getProcessor();
            ShortProcessor sp = (ShortProcessor) bp.convertToShort(false);
            return sp.getBufferedImage();
        }
        return null;
    }
    
    
}
