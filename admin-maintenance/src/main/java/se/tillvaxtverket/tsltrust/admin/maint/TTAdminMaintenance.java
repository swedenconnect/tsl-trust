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
package se.tillvaxtverket.tsltrust.admin.maint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iaik.security.provider.IAIK;
import iaik.x509.extensions.qualified.structures.QCStatement;
import iaik.x509.ocsp.net.OCSPContentHandlerFactory;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Security;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileFilter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tillvaxtverket.tsltrust.admin.maint.config.ExtFileFilter;
import se.tillvaxtverket.tsltrust.admin.maint.config.Preferences;
import se.tillvaxtverket.tsltrust.common.config.ConfigFactory;
import se.tillvaxtverket.tsltrust.common.iaik.AuthContextQCStatement;
import se.tillvaxtverket.tsltrust.common.iaik.PdsQCStatement;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.JsonConfigFactory;
import se.tillvaxtverket.tsltrust.common.utils.general.ObservableFrameCloser;
import se.tillvaxtverket.tsltrust.common.utils.general.ObserverConstants;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HibernateConfigFactory;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HibernateUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.LotlSigCert;
import se.tillvaxtverket.ttsigvalws.ttwsconsole.SignatureVerifyIF;
import se.tillvaxtverket.ttsigvalws.ttwsconsole.TrustCache;
import se.tillvaxtverket.ttsigvalws.ttwsconsole.config.SigValConfig;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.ConfigData;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;

/**
 * The main class of the TSL Trust Maintenance and configuration console. This
 * application provides functions for configuration and test of the TSL Trust
 * policy administration webservice.
 */
public class TTAdminMaintenance extends javax.swing.JFrame implements Observer, ObserverConstants {

    private String localDir, currentContextConfFileName = null;
    private PolicyCaIF cacheDeamon;
    File contextConfLocationFile;
    File contextConfFileBack;
    File backupDir;
    TslTrustConfig contextConfData;
    private static final Logger LOG = Logger.getLogger(TTAdminMaintenance.class.getName());
    private ResourceBundle uiText = ResourceBundle.getBundle("adUiText");
    boolean initialized;
    TslTrustModel ttModel = null;
    private static final List<String> parameterExceptions = new LinkedList<String>();
    private String rootlistUrl = "";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        parameterExceptions.add("DiscoFeedUrl");
        parameterExceptions.add("LogDbUserName");
        parameterExceptions.add("LogDbPassword");
        parameterExceptions.add("PolicyDbUserName");
        parameterExceptions.add("PolicyDbPassword");
        parameterExceptions.add("DbAutoCreateTables");
        parameterExceptions.add("DbVerboseLogging");
    }

    /**
     * Creates new form TTAdminMaintenance
     */
    public TTAdminMaintenance() {
        super("TSL Trust - Policy Admin Maintenance");
        setMinimumSize(new Dimension(800, 600));
        localDir = System.getProperty("user.dir");
        String backupDirName = FileOps.getfileNameString(localDir, "conf/backup");
        backupDir = new File(backupDirName);
        contextConfFileBack = new File(backupDirName, "WebXmlBackup.xml");
        contextConfLocationFile = new File(FileOps.getfileNameString(localDir, "conf/webXmlLoc.txt"));
        initComponents();
        LOG.info("Logger initialized");
        jMenuItemBackup.setEnabled(false);
        getCurrentContextConfig();

        jMenuItemSigValConsole.setVisible(false);
        jMenuItemSigValConsole.setVisible(true);

        //Register private QCStatements
        QCStatement.register(PdsQCStatement.statementID, PdsQCStatement.class);
        QCStatement.register(AuthContextQCStatement.statementID, AuthContextQCStatement.class);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        desktop = new javax.swing.JDesktopPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemRestart = new javax.swing.JMenuItem();
        jMenuItemLocateWebXml = new javax.swing.JMenuItem();
        jMenuItemBackup = new javax.swing.JMenuItem();
        jMenuItemRestore = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItemPreferences = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItemSigValConsole = new javax.swing.JMenuItem();
        jMenuWindows = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        desktop.setBackground(new java.awt.Color(51, 51, 0));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("adUiText"); // NOI18N
        jMenu1.setText(bundle.getString("MenuFile")); // NOI18N

        jMenuItemRestart.setText(bundle.getString("MenuItemRestart")); // NOI18N
        jMenuItemRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRestartActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemRestart);

        jMenuItemLocateWebXml.setText("Locate tslTrustConfig.json");
        jMenuItemLocateWebXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLocateWebXmlActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemLocateWebXml);

        jMenuItemBackup.setText("Backup tslTrustConfig.json");
        jMenuItemBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBackupActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemBackup);

        jMenuItemRestore.setText("Restore tslTrustConfig.json");
        jMenuItemRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRestoreActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemRestore);
        jMenu1.add(jSeparator1);

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText(bundle.getString("MenuTools")); // NOI18N

        jMenuItemPreferences.setText(bundle.getString("MenuItemPreferences")); // NOI18N
        jMenuItemPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPreferencesActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemPreferences);

        jMenuItem1.setText("Policy CAs");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItemSigValConsole.setText("Signature validation test console");
        jMenuItemSigValConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSigValConsoleActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemSigValConsole);

        jMenuBar1.add(jMenu2);

        jMenuWindows.setText(bundle.getString("MenuWindows")); // NOI18N
        jMenuBar1.add(jMenuWindows);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(desktop)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(desktop)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPreferencesActionPerformed
        openPreferences();
    }//GEN-LAST:event_jMenuItemPreferencesActionPerformed

    private void jMenuItemRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRestartActionPerformed
        restartConsole();
    }//GEN-LAST:event_jMenuItemRestartActionPerformed

    private void jMenuItemLocateWebXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLocateWebXmlActionPerformed
        selectWebXmlLocation();
    }//GEN-LAST:event_jMenuItemLocateWebXmlActionPerformed

    private void jMenuItemBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBackupActionPerformed
        backupWebXml();
    }//GEN-LAST:event_jMenuItemBackupActionPerformed

    private void jMenuItemRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRestoreActionPerformed
        restoreWebXml();
    }//GEN-LAST:event_jMenuItemRestoreActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        dispose();
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemSigValConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSigValConsoleActionPerformed
        openSigValConsole();
    }//GEN-LAST:event_jMenuItemSigValConsoleActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        openCacheDeamon();
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemBackup;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemLocateWebXml;
    private javax.swing.JMenuItem jMenuItemPreferences;
    private javax.swing.JMenuItem jMenuItemRestart;
    private javax.swing.JMenuItem jMenuItemRestore;
    private javax.swing.JMenuItem jMenuItemSigValConsole;
    private javax.swing.JMenu jMenuWindows;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    // End of variables declaration//GEN-END:variables

    private void openPreferences() {
        if (!contextConfLocationFile.canRead()) {
            selectWebXmlLocation();
            return;
        };

        getCurrentContextConfig();

        Preferences prefIF = new Preferences(contextConfData,currentContextConfFileName, this);
        desktop.add(prefIF);
        addListener(prefIF);
        try {
            prefIF.setSelected(true);
            prefIF.setMaximum(true);
        } catch (PropertyVetoException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        setWindowsMenu();
    }

    private void openSigValConsole() {
        if (!contextConfLocationFile.canRead()) {
            selectWebXmlLocation();
            return;
        };

        // inits for sigValConsole
        JsonConfigFactory<SigValConfig> confFact = new JsonConfigFactory<SigValConfig>(localDir, new SigValConfig());
        SigValConfig jsonConf = confFact.getConfData();
        String dataDirName = FileOps.getfileNameString(localDir, "conf/sigval");
        ConfigData conf = new ConfigData(dataDirName);
        TrustCache tCache = new TrustCache(conf, rootlistUrl);
        tCache.refreshTrustCache();
        SigValidationBaseModel sigValBaseModel = new SigValidationBaseModel(conf);
        Locale.setDefault(new Locale(sigValBaseModel.getConf().getLanguageCode()));
        SignatureVerifyIF svIF = new SignatureVerifyIF(sigValBaseModel, tCache);
        desktop.add(svIF);
        addListener(svIF);
        try {
            svIF.setMaximum(true);
        } catch (PropertyVetoException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        setWindowsMenu();

    }

    /**
     * Main method of the TSL Trust admin daemon
     *
     * @param args arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Security.addProvider(new BouncyCastleProvider());
                Security.insertProviderAt(new IAIK(), 2);
                Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

                try {
                    // Get System Look and Feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    //Use Nimbus Look and feel if installed
                    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                        String lafName = info.getName();
                        if ("XNimbus".equals(info.getName())) {
                            javax.swing.UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }


                // register content handler factory for OCSP
                HttpURLConnection.setContentHandlerFactory(new OCSPContentHandlerFactory());
                new TTAdminMaintenance().setVisible(true);
            }
        });
    }

    private void openCacheDeamon() {
        if (!contextConfLocationFile.canRead()) {
            selectWebXmlLocation();
            return;
        };

        getTSLTrustModel();
        cacheDeamon = new PolicyCaIF(ttModel, contextConfData, this);
        desktop.add(cacheDeamon);
        addListener(cacheDeamon);
        try {
            cacheDeamon.setMaximum(true);
        } catch (PropertyVetoException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        setWindowsMenu();
//        LOG.info("Starting Cache Deamon");
    }

    private void addListener(JInternalFrame iFrame) {
        final JInternalFrame thisFrame = iFrame;
        InternalFrameListener ifl = new InternalFrameListener() {
            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                //setWindowsMenu();
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (thisFrame.getClass().equals(Preferences.class)) {
                    if (((Preferences) thisFrame).save) {
                        JInternalFrame[] allFrames = desktop.getAllFrames();
                        for (JInternalFrame iframe : allFrames) {
                            if (iframe.getClass().equals(PolicyCaIF.class)) {
                                iframe.doDefaultCloseAction();
                            }
                        }
                    }
                }
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                setWindowsMenu();
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
            }
        };

        iFrame.addInternalFrameListener(ifl);
    }

    private void restartConsole() {
        if (cacheDeamon != null) {
            cacheDeamon.stopDeamon();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TTAdminMaintenance().setVisible(true);
            }
        });
        dispose();
    }

    private void setWindowsMenu() {
        jMenuWindows.removeAll();
        JInternalFrame[] allFrames = desktop.getAllFrames();
        for (int i = 0; i < allFrames.length; i++) {
            JMenuItem mItem = new JMenuItem(allFrames[i].getTitle());
            mItem.addActionListener(new SelectInternalFrames(allFrames[i]));
            jMenuWindows.add(mItem);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ObservableFrameCloser) {
            if (arg.equals(PREFERENCES_SAVE)) {
                restartConsole();
            }
        }
    }

    private void getCurrentContextConfig() {
        if (contextConfLocationFile.canRead()) {
            List<String> fileLines = FileOps.readTextLineFile(contextConfLocationFile);
            if (!fileLines.isEmpty()) {
                currentContextConfFileName = fileLines.get(0);
                try {
                    ConfigFactory<TslTrustConfig> confFact = new ConfigFactory<TslTrustConfig>(new File(currentContextConfFileName), new TslTrustConfig());
                    contextConfData = confFact.getConfData();
                } catch (Exception ex) {
                    jMenuItemBackup.setEnabled(false);
                    return;
                }
            }
        } else {
            jMenuItemBackup.setEnabled(false);
            return;
        }
        if (testContextConfData()) {
            jMenuItemBackup.setEnabled(true);
        } else {
            jMenuItemBackup.setEnabled(false);
        }
    }

    private void storeCurrentWebXml(String webXmlLocation) {
        if (!contextConfLocationFile.canRead()) {
            try {
                (new File(contextConfLocationFile.getParent())).mkdirs();
            } catch (Exception ex) {
            }
        }
        try {
            FileOps.saveTxtFile(contextConfLocationFile, webXmlLocation);
            getCurrentContextConfig();
            restartConsole();
        } catch (Exception ex) {
        }
    }

    private void selectWebXmlLocation() {
        String currentWebXmlLocation = (currentContextConfFileName != null) ? currentContextConfFileName : localDir;
        JFileChooser fc = new JFileChooser(new File(currentWebXmlLocation));
        fc.setDialogTitle("TSL Trust Admin Service web.xml file");
        fc.setApproveButtonText("Select");
        fc.setAcceptAllFileFilterUsed(false);
        FileFilter filt1 = new ExtFileFilter("JSON Files", new String[]{"JSON"});
        fc.addChoosableFileFilter(filt1);

        int returnVal = fc.showOpenDialog(fc);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.exists()) {
                try {
                    String fileName = file.getCanonicalPath();
                    storeCurrentWebXml(fileName);
                } catch (IOException ex) {
                }
            }
        }
    }

    private void setDevMode(boolean selected) {
        if (initialized) {
            if (selected) {
                contextConfData.setMode("devmode");
            } else {
                contextConfData.setMode("production");
            }
        }
    }

    private boolean testContextConfData() {
        getTSLTrustModel(false);
        return initialized;
    }

    private void getTSLTrustModel() {
        getTSLTrustModel(true);
    }

    private void getTSLTrustModel(boolean createModel) {
        initialized = true;
        String lotlUrl = contextConfData.getLotlURL();
        String discoFeedUrl = contextConfData.getDiscoFeedUrl();
        String mode = contextConfData.getMode();
        String dataLocation = FileOps.getfileNameString(localDir, "conf/sigval");
        String maxDetailedLogSize = contextConfData.getMaxConsoleLogSize();
        String maxSumLogAge = contextConfData.getMaxMajorLogAge();
//        int tslRefreshDelay;
//        try {
//            tslRefreshDelay = Integer.valueOf(contextConfData.getTSLrecacheTime()) * 1000 * 60 * 60;
//        } catch (Exception ex) {
//            tslRefreshDelay = 1000 * 60 * 60 * 24;
//        }
//        String caCountry = contextConfData.getCaCountry();
//        String caOrgName = contextConfData.getCaOrganizationName();
//        String caOrgUnit = contextConfData.getCaOrgUnitName();
//        String caSerial = contextConfData.getCaSerialNumber();
        String caCommonName = contextConfData.getCaCommonName();
//        String caFileStorageLocation = contextConfData.getCaFileStorageLocation();
        String caDistributionURL = contextConfData.getCaDistributionURL();

        //Init Hibernate configuration
        HibernateConfigFactory.setLogConnectionUrl(contextConfData.getLogDbConnectionUrl());
        HibernateConfigFactory.setLogUserName(contextConfData.getLogDbUserName());
        HibernateConfigFactory.setLogUserPassword(contextConfData.getLogDbPassword());
        HibernateConfigFactory.setPolicyConnectionUrl(contextConfData.getPolicyDbConnectionUrl());
        HibernateConfigFactory.setPolicyUserName(contextConfData.getPolicyDbUserName());
        HibernateConfigFactory.setPolicyUserPassword(contextConfData.getPolicyDbPassword());
        HibernateConfigFactory.setAutoCreate(contextConfData.getDbAutoCreateTables());
        HibernateConfigFactory.setVerboseLogging(contextConfData.getDbVerboseLogging());
        HibernateUtil.initSessionFactories();

        rootlistUrl = FileOps.getfileNameString(caDistributionURL, "rootlist.xml");

        if (caCommonName.indexOf("####") == -1) {
            initialized = false;
        }

        if (initialized && createModel) {
            ConfigFactory<TslTrustConfig> confFact = new ConfigFactory<TslTrustConfig>(dataLocation, new TslTrustConfig());
            TslTrustConfig conf = confFact.getConfData();

            ttModel = new TslTrustModel(contextConfData, dataLocation, mode, contextConfData.getTSLrecacheTime(), maxDetailedLogSize, maxSumLogAge, lotlUrl, discoFeedUrl);

            ttModel.setLotlSigCerts(LotlSigCert.getCertificates(dataLocation));
        }
    }

//    private String getContextConfValue(String parameter) {
//        String value = contextConfData.getValue(parameter);
//
//        if (!parameterExceptions.contains(parameter)) {
//            if (value.length() == 0) {
//                initialized = false;
//            }
//        }
//        return value;
//    }

    private void backupWebXml() {
        File webXmlFile = new File(currentContextConfFileName);
        if (!webXmlFile.canRead()) {
            return;
        }
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                return;
            }
        }

        File saveFile = contextConfFileBack;

        JFileChooser fc = new JFileChooser(backupDir);
        FileFilter xmlFileFilter = new ExtFileFilter("XML Files", new String[]{"XML"});
        fc.addChoosableFileFilter(xmlFileFilter);
        fc.setSelectedFile(contextConfFileBack);
        int returnVal = fc.showSaveDialog(fc);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            saveFile = fc.getSelectedFile();
            if (saveFile.canRead()) {
                if (JOptionPane.showConfirmDialog(this,
                        "File exists - Overwrite?",
                        "Backup web.xml",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
        }
        FileOps.saveByteFile(FileOps.readBinaryFile(webXmlFile), saveFile);
    }

    private void restoreWebXml() {
        File webXmlFile = new File(currentContextConfFileName);
        if (webXmlFile.getParent() == null || !(new File(webXmlFile.getParent())).exists()) {
            return;
        }

        JFileChooser fc = new JFileChooser(backupDir);

        FileFilter xmlFileFileter = new ExtFileFilter("XML Files", new String[]{"XML"});
        fc.addChoosableFileFilter(xmlFileFileter);
        int returnVal = fc.showOpenDialog(fc);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File backupFile = fc.getSelectedFile();
            if (backupFile.canRead()) {
                FileOps.saveByteFile(FileOps.readBinaryFile(backupFile), webXmlFile);
                restartConsole();
            }
        }
    }

    class SelectInternalFrames extends AbstractAction {

        private JInternalFrame iFrame;

        public SelectInternalFrames(JInternalFrame iFrame) {
            super(iFrame.getTitle());
            this.iFrame = iFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                iFrame.setSelected(true);
            } catch (Exception ex) {
            }
        }
    }
}
