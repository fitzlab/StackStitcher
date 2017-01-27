/*
 * ConfocalToolsView.java
 */

package confocaltools;

import confocaltools.imageTools.ImageFileIO;
import confocaltools.imageTools.ImageStitcher;
import ij.plugin.ZProjector;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Integer;
import java.util.ArrayList;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The application's main frame.
 */
public class ConfocalToolsView extends FrameView {

    //init layout table
    StitchTableModel stitchTableModel = new StitchTableModel();

    class StitchDirThread extends Thread{
        
        public void run(){
            
            ImageStitcher stitcher = new ImageStitcher();

            //input params
            String mainDir = txtMainDir.getText() + "/";

            ArrayList<String> extraDirs = new ArrayList<String>();
            if(txtDir1.getText() != null && !txtDir1.getText().isEmpty()){
                extraDirs.add(txtDir1.getText());
            }
            if(txtDir2.getText() != null && !txtDir2.getText().isEmpty()){
                extraDirs.add(txtDir2.getText());
            }
            if(txtDir3.getText() != null && !txtDir3.getText().isEmpty()){
                extraDirs.add(txtDir3.getText());
            }
            if(txtDir4.getText() != null && !txtDir4.getText().isEmpty()){
                extraDirs.add(txtDir4.getText());
            }

            int gridXSize = Integer.parseInt(txtGridWidth.getText());
            int gridYSize = Integer.parseInt(txtGridHeight.getText());

            String scanType = Constants.SCAN_BIDIRECTIONAL;
            if(radioLeftToRight.isSelected()){
                scanType = Constants.SCAN_LEFTTORIGHT;
            }
            
            ArrayList<Integer> missingBlocks = stitchTableModel.getMissingTiles();
            Integer[] skipBlocks = missingBlocks.toArray(new Integer[missingBlocks.size()]);

            //stitching params
            int numPeaks = Integer.parseInt(txtPeaks.getText());
            double zPercent = 100;//Double.parseDouble(txtZPercent.getText());
            double maxOverlap = Double.parseDouble(txtMaxOverlap.getText());
            int numThreads = Integer.parseInt(txtThreads.getText());

            //output params
            boolean applyFFT = false;//checkFFT.isSelected();
            String overlapType = Constants.OVERLAP_AVERAGE;
            if(radioOverlapMax.isSelected()){
                overlapType = Constants.OVERLAP_MAX;
            }
            
            //determine Z-projection method from radio button
            int zProjectMethod = -1;
            if(radioMaxProject.isSelected()){
                zProjectMethod = ZProjector.MAX_METHOD;
            }
            else if(radioMeanProject.isSelected()){
                zProjectMethod = ZProjector.AVG_METHOD;
            }
            else if(radioStdProject.isSelected()){
                zProjectMethod = ZProjector.SD_METHOD;
            }

            //perform stitching
            stitcher.stitchDir(
                    mainDir, extraDirs, gridXSize, gridYSize, skipBlocks, scanType, //input
                    numPeaks, zPercent, maxOverlap, numThreads, //stitching
                    tabStitchMethods.getSelectedIndex(), txtCoordsFile.getText(), //user-stitch
                    applyFFT, zProjectMethod, overlapType, //output
                    txtLog //logging
                    );   
        
            //done!
            btnRun.setEnabled(true);
        }
    }
    
    public ConfocalToolsView(SingleFrameApplication app) {
        super(app);

        initComponents();
        layoutTable.setTableHeader(null);
        
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                }
            }
        });
    }

    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = ConfocalToolsApp.getApplication().getMainFrame();
            aboutBox = new ConfocalToolsAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ConfocalToolsApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtMainDir = new javax.swing.JTextField();
        btnChooseMainDir = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtDir1 = new javax.swing.JTextField();
        btnChooseDir1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtDir2 = new javax.swing.JTextField();
        btnChooseDir2 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtDir3 = new javax.swing.JTextField();
        btnChooseDir3 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txtDir4 = new javax.swing.JTextField();
        btnChooseDir4 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtGridWidth = new javax.swing.JTextField();
        txtGridHeight = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        radioLeftToRight = new javax.swing.JRadioButton();
        radioBidirectional = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        layoutTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        radioOverlapMax = new javax.swing.JRadioButton();
        radioOverlapAverage = new javax.swing.JRadioButton();
        btnRun = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextPane();
        tabStitchMethods = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtThreads = new javax.swing.JTextField();
        txtPeaks = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        radioStdProject = new javax.swing.JRadioButton();
        radioMaxProject = new javax.swing.JRadioButton();
        radioMeanProject = new javax.swing.JRadioButton();
        radioUseEachSlice = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        txtMaxOverlap = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        txtCoordsFile = new javax.swing.JTextField();
        btnChooseDir5 = new javax.swing.JButton();
        btnChooseCoordsFile = new javax.swing.JButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        zInfoButtonGroup = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(confocaltools.ConfocalToolsApp.class).getContext().getResourceMap(ConfocalToolsView.class);
        jPanel2.setBackground(resourceMap.getColor("jPanel2.background")); // NOI18N
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(-16777216,true)), resourceMap.getString("jPanel2.border.title"), 0, 0, null, resourceMap.getColor("jPanel2.border.titleColor"))); // NOI18N
        jPanel2.setForeground(resourceMap.getColor("jPanel2.foreground")); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 160, 20));

        txtMainDir.setText(resourceMap.getString("txtMainDir.text")); // NOI18N
        txtMainDir.setName("txtMainDir"); // NOI18N
        jPanel2.add(txtMainDir, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 30, 160, -1));

        btnChooseMainDir.setText(resourceMap.getString("btnChooseMainDir.text")); // NOI18N
        btnChooseMainDir.setName("btnChooseMainDir"); // NOI18N
        btnChooseMainDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseMainDirActionPerformed(evt);
            }
        });
        jPanel2.add(btnChooseMainDir, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 30, -1, -1));

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 100, 20));

        txtDir1.setName("txtDir1"); // NOI18N
        jPanel2.add(txtDir1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 160, -1));

        btnChooseDir1.setText(resourceMap.getString("btnChooseDir1.text")); // NOI18N
        btnChooseDir1.setName("btnChooseDir1"); // NOI18N
        btnChooseDir1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseDir1ActionPerformed(evt);
            }
        });
        jPanel2.add(btnChooseDir1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 120, -1, -1));

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 100, 20));

        txtDir2.setName("txtDir2"); // NOI18N
        jPanel2.add(txtDir2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 150, 160, -1));

        btnChooseDir2.setText(resourceMap.getString("btnChooseDir2.text")); // NOI18N
        btnChooseDir2.setName("btnChooseDir2"); // NOI18N
        btnChooseDir2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseDir2ActionPerformed(evt);
            }
        });
        jPanel2.add(btnChooseDir2, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 150, -1, -1));

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 100, 20));

        txtDir3.setName("txtDir3"); // NOI18N
        jPanel2.add(txtDir3, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 180, 160, -1));

        btnChooseDir3.setText(resourceMap.getString("btnChooseDir3.text")); // NOI18N
        btnChooseDir3.setName("btnChooseDir3"); // NOI18N
        btnChooseDir3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseDir3ActionPerformed(evt);
            }
        });
        jPanel2.add(btnChooseDir3, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 180, -1, -1));

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 100, 20));

        txtDir4.setName("txtDir4"); // NOI18N
        jPanel2.add(txtDir4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 210, 160, -1));

        btnChooseDir4.setText(resourceMap.getString("btnChooseDir4.text")); // NOI18N
        btnChooseDir4.setName("btnChooseDir4"); // NOI18N
        btnChooseDir4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseDir4ActionPerformed(evt);
            }
        });
        jPanel2.add(btnChooseDir4, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 210, -1, -1));

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 340, 30));

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 90, 100, 20));

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 30, 100, 20));

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N
        jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 60, 100, 20));

        txtGridWidth.setText(resourceMap.getString("txtGridWidth.text")); // NOI18N
        txtGridWidth.setName("txtGridWidth"); // NOI18N
        txtGridWidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGridWidthActionPerformed(evt);
            }
        });
        txtGridWidth.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtGridWidthFocusGained(evt);
            }
        });
        txtGridWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtGridWidthKeyReleased(evt);
            }
        });
        jPanel2.add(txtGridWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 30, 40, -1));

        txtGridHeight.setText(resourceMap.getString("txtGridHeight.text")); // NOI18N
        txtGridHeight.setName("txtGridHeight"); // NOI18N
        txtGridHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGridHeightActionPerformed(evt);
            }
        });
        txtGridHeight.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtGridHeightFocusGained(evt);
            }
        });
        txtGridHeight.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtGridHeightKeyReleased(evt);
            }
        });
        jPanel2.add(txtGridHeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 60, 40, -1));

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N
        jPanel2.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 120, 160, 60));

        buttonGroup2.add(radioLeftToRight);
        radioLeftToRight.setSelected(true);
        radioLeftToRight.setText(resourceMap.getString("radioLeftToRight.text")); // NOI18N
        radioLeftToRight.setName("radioLeftToRight"); // NOI18N
        radioLeftToRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioLeftToRightActionPerformed(evt);
            }
        });
        jPanel2.add(radioLeftToRight, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 90, 110, -1));

        buttonGroup2.add(radioBidirectional);
        radioBidirectional.setText(resourceMap.getString("radioBidirectional.text")); // NOI18N
        radioBidirectional.setName("radioBidirectional"); // NOI18N
        radioBidirectional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioBidirectionalActionPerformed(evt);
            }
        });
        jPanel2.add(radioBidirectional, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 90, 130, -1));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setAutoscrolls(true);
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        layoutTable.setModel(stitchTableModel);
        layoutTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        layoutTable.setName("layoutTable"); // NOI18N
        layoutTable.setRowSelectionAllowed(false);
        layoutTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        layoutTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                layoutTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(layoutTable);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 130, 300, 130));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 910, 270));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(-16777216,true)), resourceMap.getString("jPanel4.border.title"), 0, 0, null, resourceMap.getColor("jPanel4.border.titleColor"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N
        jPanel4.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 110, 20));

        buttonGroup1.add(radioOverlapMax);
        radioOverlapMax.setText(resourceMap.getString("radioOverlapMax.text")); // NOI18N
        radioOverlapMax.setName("radioOverlapMax"); // NOI18N
        jPanel4.add(radioOverlapMax, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 30, -1, -1));

        buttonGroup1.add(radioOverlapAverage);
        radioOverlapAverage.setSelected(true);
        radioOverlapAverage.setText(resourceMap.getString("radioOverlapAverage.text")); // NOI18N
        radioOverlapAverage.setName("radioOverlapAverage"); // NOI18N
        jPanel4.add(radioOverlapAverage, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 30, -1, -1));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, 350, 80));

        btnRun.setText(resourceMap.getString("btnRun.text")); // NOI18N
        btnRun.setName("btnRun"); // NOI18N
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });
        jPanel1.add(btnRun, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 590, 170, 50));

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 290, -1, -1));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtLog.setName("txtLog"); // NOI18N
        jScrollPane1.setViewportView(txtLog);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 310, 340, 340));

        tabStitchMethods.setName("tabStitchMethods"); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        txtThreads.setText(resourceMap.getString("txtThreads.text")); // NOI18N
        txtThreads.setName("txtThreads"); // NOI18N

        txtPeaks.setText(resourceMap.getString("txtPeaks.text")); // NOI18N
        txtPeaks.setName("txtPeaks"); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        zInfoButtonGroup.add(radioStdProject);
        radioStdProject.setSelected(true);
        radioStdProject.setText(resourceMap.getString("radioStdProject.text")); // NOI18N
        radioStdProject.setName("radioStdProject"); // NOI18N

        zInfoButtonGroup.add(radioMaxProject);
        radioMaxProject.setText(resourceMap.getString("radioMaxProject.text")); // NOI18N
        radioMaxProject.setName("radioMaxProject"); // NOI18N

        zInfoButtonGroup.add(radioMeanProject);
        radioMeanProject.setText(resourceMap.getString("radioMeanProject.text")); // NOI18N
        radioMeanProject.setName("radioMeanProject"); // NOI18N

        zInfoButtonGroup.add(radioUseEachSlice);
        radioUseEachSlice.setText(resourceMap.getString("radioUseEachSlice.text")); // NOI18N
        radioUseEachSlice.setName("radioUseEachSlice"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(txtThreads, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtPeaks, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(radioMeanProject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(radioMaxProject, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(radioStdProject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(radioUseEachSlice, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                .addGap(1, 1, 1)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPeaks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioMeanProject)
                            .addComponent(radioStdProject))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioMaxProject)
                            .addComponent(radioUseEachSlice)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtThreads, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );

        tabStitchMethods.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        txtMaxOverlap.setText(resourceMap.getString("txtMaxOverlap.text")); // NOI18N
        txtMaxOverlap.setName("txtMaxOverlap"); // NOI18N
        txtMaxOverlap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaxOverlapActionPerformed(evt);
            }
        });
        txtMaxOverlap.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtMaxOverlapKeyReleased(evt);
            }
        });

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtMaxOverlap, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(51, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMaxOverlap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(58, 58, 58))
        );

        tabStitchMethods.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        txtCoordsFile.setName("txtCoordsFile"); // NOI18N

        btnChooseDir5.setText(resourceMap.getString("btnChooseDir5.text")); // NOI18N
        btnChooseDir5.setName("btnChooseDir5"); // NOI18N
        btnChooseDir5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseDir5ActionPerformed(evt);
            }
        });

        btnChooseCoordsFile.setText(resourceMap.getString("btnChooseCoordsFile.text")); // NOI18N
        btnChooseCoordsFile.setName("btnChooseCoordsFile"); // NOI18N
        btnChooseCoordsFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseCoordsFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCoordsFile, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnChooseCoordsFile)
                .addGap(171, 171, 171)
                .addComponent(btnChooseDir5)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChooseDir5)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCoordsFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChooseCoordsFile))
                .addContainerGap(200, Short.MAX_VALUE))
        );

        tabStitchMethods.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        jPanel1.add(tabStitchMethods, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, 530, 270));

        mainPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 1005, 670));

        setComponent(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
    //parse form data and perform stitching
     StitchDirThread t = new StitchDirThread();
     t.start();
     btnRun.setEnabled(false);
}//GEN-LAST:event_btnRunActionPerformed

private void btnChooseMainDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseMainDirActionPerformed
    String dir = ImageFileIO.showSelectDirDialog();    
    txtMainDir.setText(dir);
}//GEN-LAST:event_btnChooseMainDirActionPerformed

private void btnChooseDir1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseDir1ActionPerformed
    String dir = ImageFileIO.showSelectDirDialog();    
    txtDir1.setText(dir);
}//GEN-LAST:event_btnChooseDir1ActionPerformed

private void btnChooseDir2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseDir2ActionPerformed
    String dir = ImageFileIO.showSelectDirDialog();    
    txtDir2.setText(dir);
}//GEN-LAST:event_btnChooseDir2ActionPerformed

private void btnChooseDir3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseDir3ActionPerformed
    String dir = ImageFileIO.showSelectDirDialog();    
    txtDir3.setText(dir);
}//GEN-LAST:event_btnChooseDir3ActionPerformed

private void btnChooseDir4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseDir4ActionPerformed
    String dir = ImageFileIO.showSelectDirDialog();    
    txtDir4.setText(dir);
}//GEN-LAST:event_btnChooseDir4ActionPerformed

private void txtGridHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGridHeightActionPerformed
    stitchTableModel.updateHeight(Integer.parseInt(txtGridHeight.getText()));
}//GEN-LAST:event_txtGridHeightActionPerformed

private void txtGridWidthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGridWidthActionPerformed
    stitchTableModel.updateWidth(Integer.parseInt(txtGridWidth.getText()));
}//GEN-LAST:event_txtGridWidthActionPerformed

private void radioBidirectionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioBidirectionalActionPerformed
    if(radioBidirectional.isSelected()){
        stitchTableModel.updateScanDirection(Constants.SCAN_BIDIRECTIONAL);
    }
    else if(radioLeftToRight.isSelected()){
        stitchTableModel.updateScanDirection(Constants.SCAN_LEFTTORIGHT);
    }
}//GEN-LAST:event_radioBidirectionalActionPerformed

private void radioLeftToRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioLeftToRightActionPerformed
    if(radioBidirectional.isSelected()){
        stitchTableModel.updateScanDirection(Constants.SCAN_BIDIRECTIONAL);
    }
    else if(radioLeftToRight.isSelected()){
        stitchTableModel.updateScanDirection(Constants.SCAN_LEFTTORIGHT);
    }
}//GEN-LAST:event_radioLeftToRightActionPerformed

private void txtGridWidthKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGridWidthKeyReleased
    if(txtGridWidth.getText() != ""){
        stitchTableModel.updateWidth(Integer.parseInt(txtGridWidth.getText()));
    }
}//GEN-LAST:event_txtGridWidthKeyReleased

private void txtGridHeightKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGridHeightKeyReleased
    if(txtGridHeight.getText() != ""){
        stitchTableModel.updateHeight(Integer.parseInt(txtGridHeight.getText()));
    }
}//GEN-LAST:event_txtGridHeightKeyReleased

private void layoutTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_layoutTableMouseClicked
    int row = layoutTable.getSelectedRow();
    int col = layoutTable.getSelectedColumn();
    stitchTableModel.updateMissingTiles(row, col);
}//GEN-LAST:event_layoutTableMouseClicked

private void btnChooseDir5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseDir5ActionPerformed
    String filepath = ImageFileIO.showSelectDirDialog();    
    txtCoordsFile.setText(filepath);
}//GEN-LAST:event_btnChooseDir5ActionPerformed

private void txtMaxOverlapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaxOverlapActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtMaxOverlapActionPerformed

private void txtMaxOverlapKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMaxOverlapKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_txtMaxOverlapKeyReleased

private void btnChooseCoordsFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseCoordsFileActionPerformed
    String file = ImageFileIO.showSelectFileDialog();    
    txtCoordsFile.setText(file); 
}//GEN-LAST:event_btnChooseCoordsFileActionPerformed

private void txtGridWidthFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGridWidthFocusGained
    txtGridWidth.selectAll();
}//GEN-LAST:event_txtGridWidthFocusGained

private void txtGridHeightFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGridHeightFocusGained
    txtGridHeight.selectAll();
}//GEN-LAST:event_txtGridHeightFocusGained

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseCoordsFile;
    private javax.swing.JButton btnChooseDir1;
    private javax.swing.JButton btnChooseDir2;
    private javax.swing.JButton btnChooseDir3;
    private javax.swing.JButton btnChooseDir4;
    private javax.swing.JButton btnChooseDir5;
    private javax.swing.JButton btnChooseMainDir;
    private javax.swing.JButton btnRun;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable layoutTable;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JRadioButton radioBidirectional;
    private javax.swing.JRadioButton radioLeftToRight;
    private javax.swing.JRadioButton radioMaxProject;
    private javax.swing.JRadioButton radioMeanProject;
    private javax.swing.JRadioButton radioOverlapAverage;
    private javax.swing.JRadioButton radioOverlapMax;
    private javax.swing.JRadioButton radioStdProject;
    private javax.swing.JRadioButton radioUseEachSlice;
    private javax.swing.JTabbedPane tabStitchMethods;
    private javax.swing.JTextField txtCoordsFile;
    private javax.swing.JTextField txtDir1;
    private javax.swing.JTextField txtDir2;
    private javax.swing.JTextField txtDir3;
    private javax.swing.JTextField txtDir4;
    private javax.swing.JTextField txtGridHeight;
    private javax.swing.JTextField txtGridWidth;
    private javax.swing.JTextPane txtLog;
    private javax.swing.JTextField txtMainDir;
    private javax.swing.JTextField txtMaxOverlap;
    private javax.swing.JTextField txtPeaks;
    private javax.swing.JTextField txtThreads;
    private javax.swing.ButtonGroup zInfoButtonGroup;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
