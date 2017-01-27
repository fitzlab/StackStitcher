/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package confocaltools;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import stitching.Point3D;

/**
 *
 * @author walkert
 */
public class StitchTableModel extends AbstractTableModel {
    ArrayList<String> columnNames = new ArrayList<String>();
    ArrayList<Class> columnClasses = new ArrayList<Class>();
    String scanType = Constants.SCAN_BIDIRECTIONAL;
    Integer width = new Integer(3);
    Integer height = new Integer(3);    
    ArrayList<Integer> missingTiles = new ArrayList<Integer>();
    
    public StitchTableModel(){
        //column headers aren't actually displayed
        //but they define the width
        for(int i = 0; i < width; i++){
            columnNames.add("");
            columnClasses.add(String.class);
        }
    }
    
    public Class getColumnClass(int c) {
        return String.class;
    }
    
    public String getColumnName(int column){
        return columnNames.get(column);
    }
    
    public int getRowCount() {
        return height;
    }

    public int getColumnCount() {
        return width;
    }
    
    private int getPos(int row, int col){
        int pos;
        if(scanType.equals(Constants.SCAN_LEFTTORIGHT)){
            pos = row*getColumnCount() + col;
        }
        else if(scanType.equals(Constants.SCAN_BIDIRECTIONAL)){
            //if it's an even numbered row, we'll have left to right
            if(row % 2 == 0){
                pos = row*getColumnCount() + col;
            }        
            else{
                pos = (row+1)*getColumnCount() - col - 1;
            }
        }
        else{
            return -1;
        }
        return pos;
    }
    
    public Object getValueAt(int row, int col) {
        int pos = getPos(row,col);
        
        for(int i = 0; i < missingTiles.size(); i++){
            if(missingTiles.get(i) == pos){
                return "X";
            }
        }
        
        //show pos+1 for the silly hoo-mans so they don't get their
        //tiny brains all muddled by numbers starting at 0
        return Integer.toString(pos+1); 
    }
    
    public boolean isCellEditable(int row, int col){ 
        return false; 
    }
    
    private void updateData(){
        this.fireTableStructureChanged();
        this.fireTableDataChanged();
    }
    
    public void updateWidth(int newWidth){
        width = newWidth;
        columnNames.clear();
        columnClasses.clear();
        for(int i = 0; i < width; i++){
            columnNames.add("");
            columnClasses.add(String.class);
        }
        updateData();
    }

    public void updateHeight(int newHeight){
        height = newHeight;
        updateData();
    }
    
    public void updateMissingTiles(int rowAt, int colAt){
        int pos = getPos(rowAt, colAt);
        for(int i = 0; i < missingTiles.size(); i++){
            if(missingTiles.get(i) == pos){
                missingTiles.remove(i);
                updateData();
                return;
            }
        }
        missingTiles.add(pos);
        updateData();
    }
    
    public void updateScanDirection(String newScanType){
        scanType = newScanType;
        updateData();
    }
    
    public ArrayList<Integer> getMissingTiles(){
        return missingTiles;
    }
}
