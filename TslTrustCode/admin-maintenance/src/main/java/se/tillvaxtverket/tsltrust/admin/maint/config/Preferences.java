/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
 */
package se.tillvaxtverket.tsltrust.admin.maint.config;

import java.util.Enumeration;
import se.tillvaxtverket.tsltrust.common.utils.general.ColorPane;
import se.tillvaxtverket.tsltrust.common.utils.general.ConfigConstants;
import se.tillvaxtverket.tsltrust.common.utils.general.ObservableFrameCloser;
import se.tillvaxtverket.tsltrust.common.utils.general.ObserverConstants;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;

/**
 * UI class for configuration of TSL Trust Administration service
 */
public class Preferences extends javax.swing.JInternalFrame implements ConfigConstants, Observer, ObserverConstants {

    public boolean save = false;
    private static final Logger LOG = Logger.getLogger(Preferences.class.getName());
    private ObservableFrameCloser frameCloser;
    private ResourceBundle uiText = ResourceBundle.getBundle("adUiText");
    TslTrustConfig tslTrustConfig;
    Map<String, String> paramChangeMap;
    String[] paramTableHeader = new String[]{"Parameter", "Value", "Description"};

    /** Creates new form Preferences */
    public Preferences(TslTrustConfig tslTrustConfig, String location, Observer closeObserver) {
        super("Service Configuration - " + location,
                false, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable
        initComponents();
        this.setVisible(true);
        this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setLocation(100, 100);
        this.tslTrustConfig = tslTrustConfig;
        //set oservable frame closer
        frameCloser = new ObservableFrameCloser(this, closeObserver);
        paramChangeMap = new HashMap<String, String>();
        

        setInitialValues();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        crlOptionsGroup = new javax.swing.ButtonGroup();
        jButtonSave = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableParameters = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jComboBoxParameters = new javax.swing.JComboBox();
        jTextFieldParameterValue = new javax.swing.JTextField();
        jButtonUpdate = new javax.swing.JButton();
        jLabelDescription = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("adUiText"); // NOI18N
        jButtonSave.setText(bundle.getString("ButtonSave")); // NOI18N
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonCancel.setText(bundle.getString("ButtonCancel")); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTableParameters.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTableParameters.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableParameters.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableParametersMouseClicked(evt);
            }
        });
        jTableParameters.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTableParametersKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jTableParameters);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jScrollPane2.setViewportView(jTextPane1);

        jSplitPane1.setRightComponent(jScrollPane2);

        jComboBoxParameters.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxParameters.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxParametersItemStateChanged(evt);
            }
        });

        jTextFieldParameterValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldParameterValueKeyTyped(evt);
            }
        });

        jButtonUpdate.setText("Update");
        jButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateActionPerformed(evt);
            }
        });

        jLabelDescription.setText("           ");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jComboBoxParameters, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabelDescription)
                        .addContainerGap())
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jTextFieldParameterValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonUpdate))))
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxParameters, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldParameterValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonUpdate)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jButtonCancel)
                        .add(18, 18, 18)
                        .add(jButtonSave))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonSave)
                    .add(jButtonCancel)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        saveData();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jComboBoxParametersItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxParametersItemStateChanged
//        String param = (String) jComboBoxParameters.getSelectedItem();
//        if (paramChangeMap.containsKey(param)) {
//            jTextFieldParameterValue.setText(paramChangeMap.get(param));
//        } else {
//            jTextFieldParameterValue.setText(tslTrustConfig.getValue(param));
//        }
//        jLabelDescription.setText(" " + tslTrustConfig.getDescription(param));
//        jTextFieldParameterValue.requestFocusInWindow();

    }//GEN-LAST:event_jComboBoxParametersItemStateChanged

    private void jTableParametersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableParametersMouseClicked
        jComboBoxParameters.setSelectedIndex(jTableParameters.getSelectedRow());
        jTableParameters.requestFocusInWindow();
    }//GEN-LAST:event_jTableParametersMouseClicked

    private void jTableParametersKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTableParametersKeyReleased
        jComboBoxParameters.setSelectedIndex(jTableParameters.getSelectedRow());
        jTableParameters.requestFocusInWindow();
    }//GEN-LAST:event_jTableParametersKeyReleased

    private void jButtonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateActionPerformed
        captureDataChange();
    }//GEN-LAST:event_jButtonUpdateActionPerformed

    private void jTextFieldParameterValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldParameterValueKeyTyped
        char keyChar = evt.getKeyChar();
        if (keyChar == (char) 10 || keyChar == (char) 13) {
            captureDataChange();
        }
    }//GEN-LAST:event_jTextFieldParameterValueKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup crlOptionsGroup;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonUpdate;
    private javax.swing.JComboBox jComboBoxParameters;
    private javax.swing.JLabel jLabelDescription;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableParameters;
    private javax.swing.JTextField jTextFieldParameterValue;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    private void setInitialValues() {
//        displayParmTable();
//        List<String> initParamList = tslTrustConfig.getInitParamList();
//        jComboBoxParameters.removeAllItems();
//        for (String parameter : initParamList) {
//            jComboBoxParameters.addItem(parameter);
//        }
//        updateParams();
    }

    private long getLong(String value) {
        try {
            long val = Long.decode(value);
            return val;
        } catch (Exception ex) {
            return (long) 0;
        }
    }

    private String getTimeString(String text) {
        try {
            long val = Long.decode(text);
            return getTimeString(val);
        } catch (Exception ex) {
            return getTimeString((long) 0);
        }
    }

    private String getTimeString(long sec) {
        StringBuilder b = new StringBuilder();
        int seconds = (int) sec;
        int minutes = (seconds > 0) ? seconds / 60 : 0;
        int hours = (minutes > 0) ? minutes / 60 : 0;
        int days = (hours > 0) ? hours / 24 : 0;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        b.append(days).append(" ").append(uiText.getString("ShortDays")).append(" ").append(hours).append(uiText.getString("ShortHours")).append(" ").append(minutes).append(uiText.getString("ShortMinutes")).append(" ").append(seconds).append(uiText.getString("ShortSeconds"));
        return b.toString();

    }

    private void saveData() {
//        tslTrustConfig.updateParameters(paramChangeMap);
//        save = true;
//        frameCloser.close(PREFERENCES_SAVE);
    }

    public void selectHomeDir(String homedir) {
        JFileChooser fc = new JFileChooser(new File(homedir));
        fc.setDialogTitle("Signature validation data home directory");
        fc.setApproveButtonText("Select");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        FileFilter filt1 = new ExtFileFilter("Directory", new String[]{""});
        fc.addChoosableFileFilter(filt1);

        int returnVal = fc.showOpenDialog(fc);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.exists()) {
                try {
                    String dir = file.getCanonicalPath();
                } catch (IOException ex) {
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ObservableFrameCloser) {
            if (arg instanceof String) {
                String param = (String) arg;
                int seconds;
                try {
                    seconds = Integer.valueOf(param.substring(1));
                } catch (Exception ex) {
                    seconds = 0;
                }
                if (param.startsWith("T")) {
                }
                if (param.startsWith("C")) {
                }
            }
        }
    }

    //Time records Utility
    private void displayParmTable() {
//        DefaultTableModel tableData = new MyTableModel(paramTableHeader);
//        
//        if (!tslTrustConfig.isInitialized()) {
//            renderParmTable(tableData);
//            return;
//        }
//        List<String> initParamList = tslTrustConfig.getInitParamList();
//        Map<String, List<String>> initParamValues = tslTrustConfig.getInitParamValues();
//        for (String parm : initParamList) {
//            String parmValue = "";
//            List<String> values = initParamValues.get(parm);
//            if (paramChangeMap.containsKey(parm)) {
//                parmValue = (paramChangeMap.get(parm));
//            } else {
//                parmValue = (values.get(0));
//            }
//            tableData.addRow(new String[]{
//                parm,
//                parmValue,
//                values.get(1)
//            });
//        }
//
//        renderParmTable(tableData);
    }

    private void renderParmTable(TableModel tm) {
        jTableParameters.setModel(tm);
        Enumeration<TableColumn> columns = jTableParameters.getColumnModel().getColumns();
        int[] colWidth = new int[]{200,600,600};
        int colIndex = 0;
        while (columns.hasMoreElements()){
            columns.nextElement().setPreferredWidth(colWidth[colIndex++]);
        }
        jTableParameters.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void captureDataChange() {
//        String newValue = jTextFieldParameterValue.getText();
//        String parameter = (String) jComboBoxParameters.getSelectedItem();
//        Map<String, List<String>> initParamValues = tslTrustConfig.getInitParamValues();
//        if (initParamValues.containsKey(parameter)) {
//            // if paremeter was previously changed
//            if (paramChangeMap.containsKey(parameter)) {
//                // If new value matches original value, remove change record
//                if (newValue.equals(initParamValues.get(parameter).get(0))) {
//                    paramChangeMap.remove(parameter);
//                    updateParams();
//                    return;
//                }
//            }
//            paramChangeMap.put(parameter, newValue);
//            updateParams();
//        } else {
//            // If not on parameter list, remove
//            paramChangeMap.remove(parameter);
//            updateParams();
//            return;
//        }
//        jTextFieldParameterValue.requestFocusInWindow();
    }

    private void updateParams() {
//        jTextPane1.setText("");
//        ColorPane cp = new ColorPane(jTextPane1);
//
//        if (paramChangeMap.isEmpty()) {
//            cp.addStyledTextLine("No changes will be applied on save", ColorPane.GREEN_BOLD);
//            displayParmTable();
//            cp.renderText();
//            return;
//        }
//        Map<String, List<String>> initParamValues = tslTrustConfig.getInitParamValues();
//        cp.addStyledTextLine("The following changes will be applied on save", ColorPane.GREEN_BOLD);
//        cp.addLF();
//        List<String> initParamList = tslTrustConfig.getInitParamList();
//        for (String param : initParamList) {
//            if (paramChangeMap.containsKey(param)) {
//                cp.addStyledText(param + ": ", ColorPane.ATTRIBUTE);
//                cp.addStyledText(initParamValues.get(param).get(0), ColorPane.GRAY);
//                cp.addStyledText(" -> ", ColorPane.BOLD);
//                cp.addStyledTextLine(paramChangeMap.get(param), ColorPane.GRAY);
//            }
//        }
//        cp.renderText();
//        displayParmTable();
    }

    class MyTableModel extends DefaultTableModel {

        public MyTableModel(Object[] os) {
            super(os, 0x0);
        }
        
        @Override
        public boolean isCellEditable(int i, int i1) {
            return false;
        }
    }
}
