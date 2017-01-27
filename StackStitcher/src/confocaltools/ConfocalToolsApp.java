/*
 * ConfocalToolsApp.java
 */

package confocaltools;

import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import java.util.Properties;
import javax.swing.UIManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class ConfocalToolsApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        try{
            // set your theme
            Properties props = new Properties();
            props.put("backgroundPattern", "");
            HiFiLookAndFeel.setCurrentTheme(props);
            UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        }
        catch(Exception ex){
            //whatever
        }
        show(new ConfocalToolsView(this));
    }


    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of ConfocalToolsApp
     */
    public static ConfocalToolsApp getApplication() {
        return Application.getInstance(ConfocalToolsApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        if(args.length == 0){
            launch(ConfocalToolsApp.class, args);
        }
        else{
            CommandLine.handleArgs(args);
        }
    }
}
