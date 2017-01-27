/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools;

import java.awt.Color;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author walkert
 */
public class Log {
   
    public static String getTimestamp(){
        Date dateNow = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuilder s = new StringBuilder( df.format( dateNow ) );
        return "[" + s.toString() + "] ";
   }
   
   public static void write(String s, Color c, JTextPane txtLog){
        try{
            StyleContext sc = StyleContext.getDefaultStyleContext();
            AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
            StyledDocument doc = txtLog.getStyledDocument();
            doc.insertString(doc.getLength(), getTimestamp() + s + "\n", aset);
            //scroll the text as it gets more and more
            txtLog.scrollRectToVisible(
            new Rectangle(0,txtLog.getHeight()-1,1,1));
            txtLog.repaint();
        }
        catch(Exception ex){
            //what? It messed up writing a LOG MESSAGE. What do you want me to do about that exactly?!
        }
   }
   
   public static void write(String s){
       System.out.println(s);
   }
   
   public static void write(Exception ex){
       ex.printStackTrace();
   }
}
