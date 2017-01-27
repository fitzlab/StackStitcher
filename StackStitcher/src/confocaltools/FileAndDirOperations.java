/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author walkert
 */

public class FileAndDirOperations {
    
    public static boolean isDirEmpty(String dirpath){
        File file = new File(dirpath);
	if(file.isDirectory()){
            if(file.list().length>0){
                return false;
            }
            else{
                return true;
            }
	}
        else{
            return false;
	}
    }
    
    public static ArrayList<String> getDirContents(String path, boolean recurse){
        ArrayList<String> contents = new ArrayList<String>();
        try{
            if(!path.endsWith("/")){
                path += "/";
            }
            File dir = new File(path);
            String files[] = dir.list();
            if(files == null){
                    Log.write("Error reading directory: " + path);
            }
            int x = 0;
            while(files != null && x<files.length){
                File f = new File(path + files[x]);
                if(! f.isDirectory()){
                    contents.add(path + files[x]);
                }
                else if(recurse){
                    File newDir = new File(path + files[x]);
                    newDir.mkdir();
                    contents.addAll(getDirContents(path + files[x], true));
                }
                x++;
            }
        }
        catch(Exception ex){
            Log.write(ex);
        }
        
        return contents;
    }


    public static String readFileIntoString(String filePath){
        StringBuffer fileContents = new StringBuffer();
        try{
            File fromFile = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(fromFile));
            char[] buf = new char[1024];
            int numRead=0;
            while((numRead=br.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                fileContents.append(readData);
                buf = new char[1024];
            }
            br.close();
        }
        catch(Exception ex){
                //uhm
        }
        return fileContents.toString();
    }
}
