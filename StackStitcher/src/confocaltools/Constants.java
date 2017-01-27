/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools;


/**
 *
 * @author walkert
 */
public class Constants {
    public static final String PROGRAM_NAME = "ConfocalTools.jar"; //ha ha, this is so gonna change
    
    public static final String OVERLAP_MAX = "Max";
    public static final String OVERLAP_AVERAGE = "Average";

    public static final String SCAN_BIDIRECTIONAL = "Bidirectional";
    public static final String SCAN_LEFTTORIGHT = "Left to Right";

    public static final int SHORT_WHITE = 65535; //used in range scaling USHORT type images
    public static final int BYTE_WHITE = 255; //used in range scaling color & byte images
    public static final int GREY_12BIT = 2048;
    
    public static final int EDGE_RIGHT = 0;
    public static final int EDGE_LEFT = 1;
    public static final int EDGE_UP = 2;
    public static final int EDGE_DOWN = 3;
    
    public static final int FOURIER_STITCH = 0;
    public static final int RESLICE_STITCH = 1;
    public static final int USER_STITCH = 2;
}
