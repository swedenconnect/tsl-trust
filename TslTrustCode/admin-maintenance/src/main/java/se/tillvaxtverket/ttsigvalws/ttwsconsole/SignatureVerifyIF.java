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
package se.tillvaxtverket.ttsigvalws.ttwsconsole;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import iaik.asn1.ObjectID;
import iaik.x509.X509Certificate;
import iaik.x509.ocsp.OCSPRequest;
import iaik.x509.ocsp.OCSPResponse;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultTreeModel;
import org.bounce.text.LineNumberMargin;
import org.bounce.text.ScrollableEditorPanel;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLFoldingMargin;
import org.bounce.text.xml.XMLStyleConstants;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.jdesktop.layout.GroupLayout;
import se.tillvaxtverket.tsltrust.common.utils.core.CorePEM;
import se.tillvaxtverket.tsltrust.common.utils.general.ColorPane;
import se.tillvaxtverket.tsltrust.common.utils.general.ContextLogger;
import se.tillvaxtverket.tsltrust.common.utils.general.FilenameFilterImpl;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.tsltrust.common.utils.general.ObjectTree;
import se.tillvaxtverket.tsltrust.common.utils.general.ObserverConstants;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller.SignatureValidationReport;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.PdfSigVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifierFactory;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.TreeUtil;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.XmlSigVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.CertVerifyContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.OCSPVerifyContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.SignatureValidationContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.TimeStampContext;

/**
 * Internal frame providing test and logging tools to test signature validation
 * based on the cached signature validation data and the signature validation library
 */
public class SignatureVerifyIF extends javax.swing.JInternalFrame implements Observer, ObserverConstants {

    private static final String SERVER_DOC_FOLDER = "serverdocs";
    private String LF = System.getProperty("line.separator");
    private Map<String, String> listedSigFiles;
    private ObjectTree objectTree;
    private List<String> rootNames;
    private List<X509Certificate> rootCerts;
    private SigValidationBaseModel baseModel;
    private SigValidationModel model;
    private ContextLogger LOG;
    private X509Certificate root;
    private Thread verifierTask;
    private final ResourceBundle uiText;
    private JEditorPane xmlPane;
    private String selectedSigFileName;

    /** Creates new form SignatureVerifyIF */
    public SignatureVerifyIF(SigValidationBaseModel inhBaseModel, final TrustCache tCache) {
        super("Signature Validation Console",
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable
        initComponents();
        this.setVisible(true);
        this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.baseModel = inhBaseModel;
        this.LOG = baseModel.getLOG();
        jCheckBoxPrefFastest.setSelected(true);
        jTreeCertChain.setModel(null);
        getAvailablePolicyRoots();
        getAvailableSignedDocuments();
        model = new ConsoleSigValidationModel();
        model.setBaseModel(baseModel);
        uiText = ResourceBundle.getBundle("uitext");

        setupXMLPanel();
        
        // Timersetting for converting Nroff to Text
        int delay = 1000*60*30;
        ActionListener taskPerformer = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                tCache.refreshTrustCache();
                getAvailablePolicyRoots();
            }
        };
        // Start timer
        new Timer(delay, taskPerformer).start();
    }

    private void getAvailablePolicyRoots() {
        JComboBox jc = jComboBoxPolicyRoots;
        if (baseModel.getTrustStore().isInitialized()) {
            List<String> nrList = baseModel.getTrustStore().getRootNames();
            if (rootNames != null && rootNames.hashCode() == nrList.hashCode()) {
                return;
            }
            rootNames = nrList;
            jc.removeAllItems();
            for (String rootName : rootNames) {
                jc.addItem(rootName);
            }
        }
    }

    private void getAvailableSignedDocuments() {
        listedSigFiles = new HashMap<String, String>();
        jComboBoxDocs.removeAllItems();
        File caDir = new File(baseModel.getConf().getDataDirectory(), SERVER_DOC_FOLDER);
        if (caDir.canRead()) {
            File[] fileList = caDir.listFiles(new FilenameFilterImpl("."));
            if (fileList.length > 0) {
                for (File listedFile : fileList) {
                    String lfName = listedFile.getName().toLowerCase();
                    if (lfName.endsWith(".pdf")) {
                        jComboBoxDocs.addItem(listedFile.getName());
                        listedSigFiles.put(listedFile.getName(), listedFile.getAbsolutePath());
                    }
                    if (lfName.endsWith(".xml")) {
                        jComboBoxDocs.addItem(listedFile.getName());
                        listedSigFiles.put(listedFile.getName(), listedFile.getAbsolutePath());
                    }
                    if (lfName.endsWith(".xsig")) {
                        jComboBoxDocs.addItem(listedFile.getName());
                        listedSigFiles.put(listedFile.getName(), listedFile.getAbsolutePath());
                    }
                    if (lfName.endsWith(".xades")) {
                        jComboBoxDocs.addItem(listedFile.getName());
                        listedSigFiles.put(listedFile.getName(), listedFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupRevChecking = new javax.swing.ButtonGroup();
        jComboBoxPolicyRoots = new javax.swing.JComboBox();
        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPaneSigInfo = new javax.swing.JTextPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jCheckBoxCertDetails = new javax.swing.JCheckBox();
        jSplitPane3 = new javax.swing.JSplitPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextPaneChainInfo = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTreeCertChain = new javax.swing.JTree();
        jCheckBoxProvSigPath = new javax.swing.JCheckBox();
        jCheckBoxCrlDetails = new javax.swing.JCheckBox();
        jCheckBoxIncludePem = new javax.swing.JCheckBox();
        jCheckBoxJustCerts = new javax.swing.JCheckBox();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPaneOcspLog = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextPaneCrlLog = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanelXMLData = new javax.swing.JPanel();
        jButtonVerifySig = new javax.swing.JButton();
        jComboBoxDocs = new javax.swing.JComboBox();
        jCheckBoxBothOcspAndCrl = new javax.swing.JCheckBox();
        jCheckBoxPrefFastest = new javax.swing.JCheckBox();
        jCheckBoxPrefCRL = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jButtonFileChoose = new javax.swing.JButton();
        jTextSelectedFile = new javax.swing.JTextField();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);

        jComboBoxPolicyRoots.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxPolicyRoots.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxPolicyRootsItemStateChanged(evt);
            }
        });

        jSplitPane2.setDividerLocation(550);

        jSplitPane1.setDividerLocation(500);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTextPaneSigInfo.setEditable(false);
        jScrollPane1.setViewportView(jTextPaneSigInfo);

        jSplitPane1.setLeftComponent(jScrollPane1);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("uitext"); // NOI18N
        jCheckBoxCertDetails.setText(bundle.getString("LabelCertificateDetails")); // NOI18N
        jCheckBoxCertDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCertDetailsActionPerformed(evt);
            }
        });

        jSplitPane3.setDividerLocation(250);

        jTextPaneChainInfo.setEditable(false);
        jScrollPane5.setViewportView(jTextPaneChainInfo);

        jSplitPane3.setRightComponent(jScrollPane5);

        jTreeCertChain.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTreeCertChainMouseClicked(evt);
            }
        });
        jTreeCertChain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTreeCertChainKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(jTreeCertChain);

        jCheckBoxProvSigPath.setText(bundle.getString("LabelProvidedPath")); // NOI18N
        jCheckBoxProvSigPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxProvSigPathActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jCheckBoxProvSigPath)
                .addContainerGap())
            .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxProvSigPath))
        );

        jSplitPane3.setLeftComponent(jPanel3);

        jCheckBoxCrlDetails.setText(bundle.getString("LabelCrlDetails")); // NOI18N
        jCheckBoxCrlDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCrlDetailsActionPerformed(evt);
            }
        });

        jCheckBoxIncludePem.setText(bundle.getString("LabelIncludePem")); // NOI18N
        jCheckBoxIncludePem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxIncludePemActionPerformed(evt);
            }
        });

        jCheckBoxJustCerts.setText(bundle.getString("LabelJustCerts")); // NOI18N
        jCheckBoxJustCerts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxJustCertsActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBoxCertDetails)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxIncludePem)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxCrlDetails)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxJustCerts)
                .addContainerGap(43, Short.MAX_VALUE))
            .add(jSplitPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxCertDetails)
                    .add(jCheckBoxIncludePem)
                    .add(jCheckBoxCrlDetails)
                    .add(jCheckBoxJustCerts))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("FrameStatusInfo"), jPanel2); // NOI18N

        jScrollPane3.setViewportView(jTextPaneOcspLog);

        jTabbedPane2.addTab(bundle.getString("FrameOcspLogs"), jScrollPane3); // NOI18N

        jScrollPane6.setViewportView(jTextPaneCrlLog);

        jTabbedPane2.addTab(bundle.getString("FrameCrlLogs"), jScrollPane6); // NOI18N

        jTabbedPane1.addTab(bundle.getString("FrameLogs"), jTabbedPane2); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane1);
        jTabbedPane1.getAccessibleContext().setAccessibleName(bundle.getString("FrameStatusInfo")); // NOI18N

        jSplitPane2.setTopComponent(jSplitPane1);

        jLabel1.setText(bundle.getString("LabelXMLReport")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelXMLDataLayout = new org.jdesktop.layout.GroupLayout(jPanelXMLData);
        jPanelXMLData.setLayout(jPanelXMLDataLayout);
        jPanelXMLDataLayout.setHorizontalGroup(
            jPanelXMLDataLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 699, Short.MAX_VALUE)
        );
        jPanelXMLDataLayout.setVerticalGroup(
            jPanelXMLDataLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 744, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1)
                .addContainerGap(628, Short.MAX_VALUE))
            .add(jPanelXMLData, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelXMLData, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setRightComponent(jPanel1);

        jButtonVerifySig.setText(bundle.getString("ButtonCheckSignature")); // NOI18N
        jButtonVerifySig.setActionCommand(bundle.getString("ButtonCheckSignature")); // NOI18N
        jButtonVerifySig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVerifySigActionPerformed(evt);
            }
        });

        jComboBoxDocs.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxDocs.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxDocsItemStateChanged(evt);
            }
        });

        buttonGroupRevChecking.add(jCheckBoxBothOcspAndCrl);
        jCheckBoxBothOcspAndCrl.setText(bundle.getString("LabelBothOcspCrl")); // NOI18N

        buttonGroupRevChecking.add(jCheckBoxPrefFastest);
        jCheckBoxPrefFastest.setText(bundle.getString("LabelFastest")); // NOI18N

        buttonGroupRevChecking.add(jCheckBoxPrefCRL);
        jCheckBoxPrefCRL.setText(bundle.getString("LabelCrl")); // NOI18N

        jLabel4.setText(bundle.getString("LabelPrefer")); // NOI18N

        jButtonFileChoose.setText(bundle.getString("BrowseButton")); // NOI18N
        jButtonFileChoose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFileChooseActionPerformed(evt);
            }
        });

        jTextSelectedFile.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSplitPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1260, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jComboBoxPolicyRoots, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButtonVerifySig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(jComboBoxDocs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 232, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jButtonFileChoose))
                            .add(jTextSelectedFile))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 554, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jLabel4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jCheckBoxPrefFastest)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jCheckBoxPrefCRL))
                            .add(jCheckBoxBothOcspAndCrl))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxPrefCRL)
                    .add(jCheckBoxPrefFastest)
                    .add(jLabel4)
                    .add(jComboBoxDocs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonFileChoose)
                    .add(jComboBoxPolicyRoots, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxBothOcspAndCrl)
                    .add(jTextSelectedFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonVerifySig))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxPolicyRootsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxPolicyRootsItemStateChanged
}//GEN-LAST:event_jComboBoxPolicyRootsItemStateChanged

    private void jCheckBoxCertDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCertDetailsActionPerformed
        displaySelectedTreeItem();
}//GEN-LAST:event_jCheckBoxCertDetailsActionPerformed

    private void jTreeCertChainMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTreeCertChainMouseClicked

        displaySelectedTreeItem();
}//GEN-LAST:event_jTreeCertChainMouseClicked

    private void jTreeCertChainKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTreeCertChainKeyReleased
        displaySelectedTreeItem();
}//GEN-LAST:event_jTreeCertChainKeyReleased

    private void jCheckBoxProvSigPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxProvSigPathActionPerformed
        objectTree = new ObjectTree(TreeUtil.getTreeName(root), root.getSubjectDN().getName(), root);
        displayTree();
}//GEN-LAST:event_jCheckBoxProvSigPathActionPerformed

    private void jCheckBoxCrlDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCrlDetailsActionPerformed
        displaySelectedTreeItem();
}//GEN-LAST:event_jCheckBoxCrlDetailsActionPerformed

    private void jCheckBoxIncludePemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxIncludePemActionPerformed
        displaySelectedTreeItem();
}//GEN-LAST:event_jCheckBoxIncludePemActionPerformed

    private void jCheckBoxJustCertsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxJustCertsActionPerformed
        objectTree = new ObjectTree(TreeUtil.getTreeName(root), root.getSubjectDN().getName(), root);
        displayTree();
}//GEN-LAST:event_jCheckBoxJustCertsActionPerformed

    private void jButtonVerifySigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVerifySigActionPerformed
        if (jButtonVerifySig.getText().equals(uiText.getString("ButtonAbortSignature"))) {
            model.setAbort(true);
            try {
                verifierTask.interrupt();
                simpleNotification("Signature verification aborted");
                verifierTask = null;
                jButtonVerifySig.setText(uiText.getString("ButtonCheckSignature"));
            } catch (Exception ex) {
                simpleNotification("Abort failed");
            }
        } else {
            verifySignature();
        }

}//GEN-LAST:event_jButtonVerifySigActionPerformed

    private void jButtonFileChooseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFileChooseActionPerformed
        openSigFile();
    }//GEN-LAST:event_jButtonFileChooseActionPerformed

    private void jComboBoxDocsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxDocsItemStateChanged
        selectedSigFileName = listedSigFiles.get((String) jComboBoxDocs.getSelectedItem());
        if (selectedSigFileName != null) {
            jTextSelectedFile.setText(new File(selectedSigFileName).getName());
        }
    }//GEN-LAST:event_jComboBoxDocsItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupRevChecking;
    private javax.swing.JButton jButtonFileChoose;
    private javax.swing.JButton jButtonVerifySig;
    private javax.swing.JCheckBox jCheckBoxBothOcspAndCrl;
    private javax.swing.JCheckBox jCheckBoxCertDetails;
    private javax.swing.JCheckBox jCheckBoxCrlDetails;
    private javax.swing.JCheckBox jCheckBoxIncludePem;
    private javax.swing.JCheckBox jCheckBoxJustCerts;
    private javax.swing.JCheckBox jCheckBoxPrefCRL;
    private javax.swing.JCheckBox jCheckBoxPrefFastest;
    private javax.swing.JCheckBox jCheckBoxProvSigPath;
    private javax.swing.JComboBox jComboBoxDocs;
    private javax.swing.JComboBox jComboBoxPolicyRoots;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelXMLData;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextPane jTextPaneChainInfo;
    private javax.swing.JTextPane jTextPaneCrlLog;
    private javax.swing.JTextPane jTextPaneOcspLog;
    private javax.swing.JTextPane jTextPaneSigInfo;
    private javax.swing.JTextField jTextSelectedFile;
    private javax.swing.JTree jTreeCertChain;
    // End of variables declaration//GEN-END:variables

    /**
     * Verifies the signatures of the selected PDF document
     */
    public void verifySignature() {
        if (verifierTask != null && verifierTask.isAlive()) {
            simpleNotification("Waiting for current Signature validatioin to complete");
            return;
        }
        model = new ConsoleSigValidationModel(jTextPaneOcspLog, jTextPaneOcspLog);
        model.setBaseModel(baseModel);
        if (jTextSelectedFile.getText().length() > 0) {
            model.setSigFileName(selectedSigFileName);
        } else {
            model.setSigFileName(listedSigFiles.get((String) jComboBoxDocs.getSelectedItem()));
        }
        String pName = (String) jComboBoxPolicyRoots.getSelectedItem();
        model.setPolicyName(pName);
        String policyDesc;
        try {
            policyDesc = baseModel.getTrustStore().getPolicyDescMap().get(pName);
            model.setPolicyDescription(policyDesc);
        } catch (Exception ex) {
        }
        model.setCheckOcspAndCrl(jCheckBoxBothOcspAndCrl.isSelected());
        model.setPrefSpeed(jCheckBoxPrefFastest.isSelected());

        KeyStore keyStore = baseModel.getTrustStore().getKeyStore(pName);
        root = baseModel.getTrustStore().getRoot(pName);

        if (keyStore != null) {
            objectTree = new ObjectTree(TreeUtil.getTreeName(root), root.getSubjectDN().getName(), root);
            jTextPaneSigInfo.setText("");
            SigVerifier verifier = SigVerifierFactory.getSigVerifier(model);
            if (verifier != null) {
//            if (loadpdfFile()) {
                //Verify Signature
//                PdfSigVerifier verifier = new PdfSigVerifier(model);
                verifier.addObserver(this);
                verifierTask = new Thread(verifier);
                verifierTask.start();
                jButtonVerifySig.setText(uiText.getString("ButtonAbortSignature"));
            }
        }
    }

    /**
     * The update method of the Observer interface. Catches events triggered by
     * the PdfSigVerifier task
     * @param o
     * @param arg 
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof PdfSigVerifier || o instanceof XmlSigVerifier) {
            if (arg.equals(COMPLETE) && !model.isAbort()) {
                displayResults();
                jButtonVerifySig.setText(uiText.getString("ButtonCheckSignature"));
                return;
            }
            if (arg.equals(ABORTED)) {
                simpleNotification("Signature verification aborted");
                verifierTask = null;
                jButtonVerifySig.setText(uiText.getString("ButtonCheckSignature"));
                return;
            }
            if (arg.equals(RETURN_FROM_ABORT)) {
                return;
            }
            return;
        }
    }

    public void displayResults() {
        displaySignatureResult();
        displayLogData();
        displayTree();
        displaySelectedTreeItem();
        getXMLReport();
    }

    private boolean loadpdfFile() {
        try {
            PdfReader reader = new PdfReader(model.getSigFileName());
            model.setReader(reader);
            return true;
        } catch (IOException ex) {
            LOG.info("Failed reading PDF");
            return false;
        }
    }

//    private void checkTimestamp(SignatureValidationContext svc, CertChainVerifier certChainVerifier, PdfPKCS7 pkcs7) {
//        TimeStampContext tsCont = svc.getTstContext();
//        TimeStampToken tst = tsCont.getTimeStampToken();
////        PdfPKCS7 pkcs7 = svc.getPkcs7();
//
//        tsCont.setTimeStampDate((svc.isTimestamped()) ? pkcs7.getTimeStampDate() : null);
//
////        ObjectID tsHashOID = new ObjectID(tst.getTimeStampInfo().getMessageImprintAlgOID());
////        byte[] tsMessageImprit = tst.getTimeStampInfo().getMessageImprintDigest();
////        System.out.println(tsHashOID.getNameAndID());
////        System.out.println(tsMessageImprit);
////        
////        try {
////            PKCS7 p7 = new PKCS7(pkcs7.getEncodedPKCS7());
////            p7.getContentInfo().getContentBytes();
////        } catch (Exception ex) {
////            Logger.getLogger(SigVerifyConsole.class.getName()).log(Level.SEVERE, null, ex);
////        }
//
//        CollectionStore certStore = (CollectionStore) tst.getCertificates();
//        ArrayList<X509CertificateHolder> tsCertList = (ArrayList<X509CertificateHolder>) certStore.getMatches(null);
//        java.security.cert.X509Certificate tsSigCert;
//        try {
//            tsSigCert = KsCertFactory.getX509Cert(tsCertList.get(0).getEncoded());
//            SignerInformationVerifier siv = new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(tsSigCert);
//            tst.validate(siv);
//            tsCont.setTsSignValidated(true);
//            tsCont.setMessageImprintValidated(true);
//            tsCont.setCertVerifyContext(certChainVerifier.verifyChain(tsCertList));
//        } catch (Exception ex) {
//            model.ELOG.info("Validation of Time Stamp Signature failed");
//            tsCont.setTsSignValidated(false);
//        }
//        // Verify CRL
//    }
    public void displayLogData() {
        List<SignatureValidationContext> pdfSvcList = model.getSignatureContexts();
        StringBuilder b = new StringBuilder();
        for (SignatureValidationContext pdfSvContext : pdfSvcList) {
            //OCSP Log
            try {
                for (String str : pdfSvContext.getSignCertValidation().getOcspVerifyContext().getLog()) {
                    b.append(str).append(LF);
                }
            } catch (Exception ex) {
            }

            try {
                for (String str : pdfSvContext.getTstContext().getCertVerifyContext().getOcspVerifyContext().getLog()) {
                    b.append(str).append(LF);
                }
            } catch (Exception ex) {
            }
        }

        jTextPaneOcspLog.setText(b.toString());
        jTextPaneOcspLog.setCaretPosition(0);


        // CRL Log
        b = new StringBuilder();
        for (String str : baseModel.getCrlCache().getLog()) {
            b.append(str).append(LF);
        }
        jTextPaneCrlLog.setText(b.toString());
        jTextPaneCrlLog.setCaretPosition(0);

        //ExceptionLogs
        b = new StringBuilder();
        for (SignatureValidationContext pdfSvContext : pdfSvcList) {
            try {
                for (String str : pdfSvContext.getSignCertValidation().getOcspVerifyContext().getExceptionLog()) {
                    b.append(str).append(LF);
                }
            } catch (Exception ex) {
            }

            try {
                for (String str : pdfSvContext.getTstContext().getCertVerifyContext().getOcspVerifyContext().getExceptionLog()) {
                    b.append(str).append(LF);
                }
            } catch (Exception ex) {
            }

        }

        for (String str : baseModel.getCrlCache().getExceptionLog()) {
            b.append(str).append(LF);
        }
        xmlPane.setText(b.toString());
        xmlPane.setCaretPosition(0);

    }

    public void displayTree() {
        List<SignatureValidationContext> pdfSvcList = model.getSignatureContexts();
        if (pdfSvcList == null) {
            return;
        }

        objectTree = TreeUtil.getTreeNodes(pdfSvcList, objectTree, jCheckBoxProvSigPath.isSelected(), jCheckBoxJustCerts.isSelected());
        DefaultTreeModel treeModel = new DefaultTreeModel(objectTree.getNameTree());
        jTreeCertChain.setModel(treeModel);
        jTreeCertChain.setRootVisible(!jCheckBoxProvSigPath.isSelected());

        //Expand all rows
        int row = 0;
        while (row < jTreeCertChain.getRowCount()) {
            jTreeCertChain.expandRow(row++);
        }
    }

    private void displaySelectedTreeItem() {
        SimpleDateFormat sigTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Object o = objectTree.getObjectFromJTreeSelection(jTreeCertChain);
        StringBuilder b = new StringBuilder();

        try {
            if (o != null) {
                if (o instanceof X509Certificate) {
                    X509Certificate troot = (X509Certificate) o;
                    b.append("Certificate subject: ").append(LF);
                    Iterator<Entry<ObjectID, String>> rdns = TreeUtil.getCertNameAttributeSet(troot).iterator();
                    while (rdns.hasNext()) {
                        Entry<ObjectID, String> entry = rdns.next();
                        b.append(entry.getKey().getShortName());
                        b.append(" = ");
                        b.append(entry.getValue()).append(LF);
                    }

                    b.append(LF);
                    b.append(((X509Certificate) o).toString(jCheckBoxCertDetails.isSelected())).append(LF);

                    if (jCheckBoxIncludePem.isSelected()) {
                        b.append(LF);
                        try {
                            b.append(CorePEM.getPemCert(troot.getEncoded()));
                        } catch (CertificateEncodingException ex) {
                        }
                    }

                }

                if (o instanceof CertVerifyContext) {
                    CertVerifyContext cont = (CertVerifyContext) o;
                    X509Certificate cert = cont.getChain().get(0);
                    b.append("Certificate issued to: ").append(LF);
                    Iterator<Entry<ObjectID, String>> rdns = TreeUtil.getCertNameAttributeSet(cert).iterator();
                    while (rdns.hasNext()) {
                        Entry<ObjectID, String> entry = rdns.next();
                        b.append(entry.getKey().getShortName());
                        b.append(" = ");
                        b.append(entry.getValue()).append(LF);
                    }
                    b.append(LF);
                    b.append("CRL check: ");
                    b.append(cont.isNoCheck() ? "No revocation check - Certificate has OCSP responder EKU and No-Check extension"
                            : !cont.isCrlStatusDetermined() ? "Revocation status is not determined"
                            : cont.isRevoked() ? "Certificate is Revoked" : "OK (not revoked)").append(LF);
                    OCSPVerifyContext ocspCont = cont.getOcspVerifyContext();
                    if (ocspCont != null) {
                        b.append("OCSP check: ");
                        b.append(ocspCont.isOcspCheckOK() ? "Successfull - returned valid \"good\" response"
                                : ocspCont.isOcspRevoked() ? "Certificate revoked"
                                : "Certificate status could not be determined").append(LF).append(LF);
                    } else {
                        b.append("No OCSP check.").append(LF).append(LF);
                    }
                    //b.append((cont.getIssuingCertContext() != null) ? "Issuer Cert has Context" : "Issuer cert has no Context").append(LF).append(LF);
                    b.append(cert.toString(jCheckBoxCertDetails.isSelected()));

                    if (jCheckBoxIncludePem.isSelected()) {
                        b.append(LF);
                        try {
                            b.append(CorePEM.getPemCert(cert.getEncoded()));
                        } catch (CertificateEncodingException ex) {
                        }
                    }

                }

                if (o instanceof OCSPVerifyContext) {
                    OCSPVerifyContext ocspCont = (OCSPVerifyContext) o;
                    OCSPResponse response = ocspCont.getResponse();
                    OCSPRequest request = ocspCont.getRequest();
                    b.append("OCSP check for subject:").append(LF);
                    X509Certificate cert = ocspCont.getTargetCert();
                    Iterator<Entry<ObjectID, String>> rdns = TreeUtil.getCertNameAttributeSet(cert).iterator();
                    while (rdns.hasNext()) {
                        Entry<ObjectID, String> entry = rdns.next();
                        b.append(entry.getKey().getShortName());
                        b.append(" = ");
                        b.append(entry.getValue()).append(LF);
                    }
                    b.append(LF);

                    b.append("Request:").append(LF);
                    b.append(request.toString()).append(LF).append(LF);

                    b.append("Response:").append(LF);
                    b.append(response.toString()).append(LF).append(LF);
                    b.append(ocspCont.isOcspCheckOK() ? "OCSP check successfull - status = \"good\""
                            : ocspCont.isOcspRevoked() ? "OCSP check successful - status \"revoked\""
                            : "OCSP check failed - Certificate status could not be determined").append(LF).append(LF);

                }

                if (o instanceof String) {
                    String crlKey = (String) o;
                    b.append(baseModel.getCrlCache().getIaikCRLfromKey(crlKey).toString(jCheckBoxCrlDetails.isSelected()));
                }

                if (o instanceof SignatureValidationContext) {
                    SignatureValidationContext pdfSigContext = (SignatureValidationContext) o;
                    getSignatureResultLog(pdfSigContext, b);
                }

                if (o instanceof TimeStampContext) {
                    TimeStampContext tsCont = (TimeStampContext) o;
                    if (tsCont != null) {
                        TimeStampToken tst = tsCont.getTimeStampToken();
                        TimeStampTokenInfo tstInfo = tst.getTimeStampInfo();
                        if (tstInfo.getTsa() != null) {
                            b.append("Timestamp Authority: ").append(tstInfo.getTsa().toString()).append(LF);
                        }
                        b.append("Serial Number: ").append(tstInfo.getSerialNumber().toString()).append(LF);
                        b.append("Timestamp Time: ").append(sigTime.format(tstInfo.getGenTime())).append(LF);
                        BigInteger nonce = tstInfo.getNonce();
                        b.append("Nonce: ").append(nonce != null ? tstInfo.getNonce().toString() : "No nonce").append(LF);
                        b.append("Hash algorithm: ").append(new ObjectID(tstInfo.getHashAlgorithm().getAlgorithm().toString()).getNameAndID()).append(LF);
                        String policy = tstInfo.getPolicy().toString();
                        b.append("Policy: ").append(policy != null ? policy : "Absent").append(LF).append(LF);


                        //Validity check
                        boolean sig = tsCont.isTsSignValidated();
                        boolean doc = tsCont.isMessageImprintValidated();
                        CertVerifyContext cvCont = tsCont.getCertVerifyContext();
                        boolean crl = (cvCont.isCrlStatusDetermined() && !cvCont.isRevoked());
                        OCSPVerifyContext ocspCont = cvCont.getOcspVerifyContext();
                        boolean ocsp = false;
                        if (ocspCont != null) {
                            ocsp = ocspCont.isOcspCheckOK();
                        }
                        b.append("Validity check:").append(LF);
                        b.append(sig ? "Timestamp signature check OK" : "Timestamp fails signature check").append(LF);
                        b.append(doc ? "Timestap validated against document" : "Timestamp does not match document").append(LF);
                        b.append((crl || ocsp) ? "Timestap cert validated" : "Timestamp cert fails validation").append(LF).append(LF);
                        b.append((sig && doc && (crl || ocsp)) ? "Timstamp is valid" : "Timestamp is NOT valid");
                    }
                }

            }
        } catch (NullPointerException ex) {
            LOG.warning(ex.getMessage() + " Exception in TreeData contruction");
        }

        jTextPaneChainInfo.setText(b.toString());
        jTextPaneChainInfo.setCaretPosition(0);
    }

    private void getSignatureResultLog(SignatureValidationContext pdfSvContext, StringBuilder b) {
        TimeStampContext tsCont = pdfSvContext.getTstContext();
        SimpleDateFormat sigTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        b.append("Signature name: ").append(pdfSvContext.getSignatureName()).append(LF);
        b.append("Covers document: ").append(pdfSvContext.isCoversDoc()).append(LF);
        b.append("Document revision: ").append(pdfSvContext.getRevision());
        b.append(" of ").append(pdfSvContext.getRevisions()).append(LF);
        b.append("Signing time: ").append(sigTime.format(pdfSvContext.getSignDate().getTime())).append(LF);
        b.append((pdfSvContext.isTimestamped()) ? "Document is timestamped: "
                + sigTime.format(tsCont.getTimeStampDate().getTime()) + LF : "");
        b.append("Subject: ").append(pdfSvContext.getSignCert().getSubjectX500Principal().toString()).append(LF);
        b.append("Document not modified: ").append(pdfSvContext.isDigestValid()).append(LF);

        Object[] errors = pdfSvContext.getSigValidationError();
        if (errors != null) {
            b.append("--> ").append((String) errors[1]).append(LF);
            if (errors[0] != null && errors[0] instanceof X509Certificate) {
                X509Certificate errorCert = (X509Certificate) errors[0];
                b.append("--> Error cert:").append(errorCert.getSubjectDN().getName()).append(LF);
            }
        }

        if (pdfSvContext.isDigestValid()) {
            b.append("PDF Siganture valid: ").append(pdfSvContext.isSigValid()).append(LF);

            // Algorithm identifiers
            b.append("Signature PK Alg: ").append(pdfSvContext.getSignaturePkAlgOID().getNameAndID()).append(LF);
            b.append("Signature Hash Alg: ").append(pdfSvContext.getSignatureHashAlgOID().getNameAndID()).append(LF);

            // Signing cert qualifications
            b.append("Signed by Qualified Certificate: ").append(pdfSvContext.isQualifiedCertificate()).append(LF);
            b.append("Signed using SSCD: ").append(pdfSvContext.isSscd()).append(LF);

            CertVerifyContext cvc = pdfSvContext.getSignCertValidation();
            b.append((cvc.isSigChainVerified() ? "Signature certificate chain verified"
                    : cvc.isChainVerifyError() ? "Path error:" + cvc.getChainVerifyErrorMessage()
                    : "Failed certificate path validation")).append(LF);
            boolean crlOK = (cvc.isCrlStatusDetermined() && !cvc.isRevoked());
            b.append(crlOK ? "Signature cert CRL validation OK" : cvc.isCrlStatusDetermined() ? "Signature cert revoked by CRL" : "Sig cert not CRL checked").append(LF);

            OCSPVerifyContext ocspVC = pdfSvContext.getSignCertValidation().getOcspVerifyContext();
            if (ocspVC != null) {
                b.append((ocspVC.isOcspCheckOK()) ? "Signature cert OCSP Validation OK" : "Sign cert failed OCSP Validation").append(LF);
            }
        }
        if (pdfSvContext.isTimestamped()) {
            CertVerifyContext cvc = tsCont.getCertVerifyContext();
            b.append("Time Stamp Siganture valid: ").append(tsCont.isTsSignValidated()).append(LF);
            b.append((cvc.isSigChainVerified() ? "TimeStamp certificate chain verified"
                    : "Signature certificate chain fails validation")).append(LF);

            boolean crlOK = (cvc.isCrlStatusDetermined() && !cvc.isRevoked());
            b.append(crlOK ? "Timestamp cert CRL validation OK" : cvc.isCrlStatusDetermined() ? "Timestamp cert revoked by CRL" : "TS cert not CRL checked").append(LF);

            OCSPVerifyContext ocspVC = cvc.getOcspVerifyContext();
            if (ocspVC != null) {
                b.append((ocspVC.isOcspCheckOK()) ? "Timestamp Cert OCSP Validation OK" : "Timestamp Cert failed OCSP Validation").append(LF);
            }
        }
        b.append(LF);
    }

    private void displaySignatureResult() {
        List<SignatureValidationContext> pdfSvcList = model.getSignatureContexts();
        jTextPaneSigInfo.setText("");
        ColorPane cp = new ColorPane(jTextPaneSigInfo);
        SimpleDateFormat tFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String info = ColorPane.NORMAL;
        String value = ColorPane.ORANGE;
        String mainTitle = ColorPane.MAGENTA;
        String subTitle = ColorPane.BOLD;
        String boldUnderline = ColorPane.BOLD_UNDERLINE;
        String comment = ColorPane.GRAY;
        String attribute = ColorPane.ATTRIBUTE;
        String heading = ColorPane.UNDERLINE;
        String plain = ColorPane.NORMAL;
        String good = ColorPane.GREEN;
        String error = ColorPane.RED;



        if (pdfSvcList.isEmpty()) {
            cp.addStyledTextLine("This document is not signed", mainTitle);
            cp.renderText();
            return;
        }

        cp.addStyledText("PDF Signature validation result ", mainTitle);
        cp.addStyledTextLine((pdfSvcList.size() == 1) ? "(1 Signature)" : "(" + String.valueOf(pdfSvcList.size()) + " Signatures)", comment);
        cp.addLF();


        for (SignatureValidationContext sc : pdfSvcList) {
            boolean valid = true;
            CertVerifyContext cc = sc.getSignCertValidation();
            TimeStampContext tc = (sc.isTimestamped()) ? sc.getTstContext() : null;
            OCSPVerifyContext oc = cc.getOcspVerifyContext();

            cp.addStyledText("Signature:", boldUnderline);
            cp.addStyledTextLine(" " + sc.getSignatureName(), value);

            boolean signatureCheck = sc.isSigChainVerified() && sc.isDigestValid() && sc.isSigValid();
            cp.addStyledText("Signature Check: ", info);
            if (signatureCheck) {
                cp.addStyledTextLine("OK - Document has not been modified", good);
            } else {
                cp.addStyledTextLine("Signature fails validation", error);
                //Basic signature check
//                if (!sc.isSigValid()) {
//                    cp.addStyledText("---> Reason: ", subTitle);
//                    cp.addStyledTextLine("Basic signture verification failed", comment);
//                }
                if (!sc.isDigestValid()) {
                    cp.addStyledText("---> Reason: ", subTitle);
                    cp.addStyledTextLine("The document has ben modified since it was signed", comment);
                }
                if (!sc.isSigChainVerified()) {
                    String errorMess;
                    if (cc.isChainVerifyError()) {
                        String pvem = cc.getChainVerifyErrorMessage();
                        if (pvem != null && pvem.length() > 0 && !pvem.equalsIgnoreCase("null")) {
                            errorMess = "Path validation error: " + pvem;
                        } else {
                            errorMess = "Path validation error";
                        }
                    } else {
                        errorMess = "Signer certificate can't be verified";
                    }
                    cp.addStyledText("---> Reason: ", subTitle);
                    cp.addStyledTextLine(errorMess, comment);
                }
                cp.addLF();
                valid = false;
            }
            cp.addStyledText("Signature coverage:", info);
            if (sc.isCoversDoc()) {
                cp.addStyledTextLine("Whole document", good);
            } else {
                cp.addStyledTextLine("Part of the document", value);
            }

            boolean crlValid = cc.isCrlStatusDetermined() && !cc.isRevoked()
                    && !cc.isExpired() && !cc.isNotValidYet();
            boolean crlRevoked = cc.isCrlStatusDetermined() && cc.isRevoked();
            boolean ocspValid = oc == null ? false : oc.isOcspCheckOK();
            boolean ocspRevoked = oc == null ? false : oc.isOcspRevoked();

            cp.addStyledText("Signer certificate status: ", info);
            if (crlValid || ocspValid) {
                cp.addStyledTextLine("Valid", good);
            } else {
                cp.addStyledTextLine("Status check failed", error);
                if (cc.isExpired() || cc.isNotValidYet()) {
                    cp.addStyledText("---> Reason: ", subTitle);
                    cp.addStyledTextLine(cc.isExpired() ? "Certificate has expired"
                            : "Certificate is not valid yet", comment);
                } else {
                    cp.addStyledText("---> Reason: ", subTitle);
                    cp.addStyledTextLine((crlRevoked || ocspRevoked) ? "Signer certificate is revoked"
                            : "Certificate revocation status could not be verified", comment);
                }
                cp.addLF();
                valid = false;
            }
            if (sc.getSignDate() != null) {
                cp.addStyledText("Claimed signing time: ", info);
                cp.addStyledTextLine(tFormat.format(sc.getSignDate().getTime()), value);
            }
            cp.addStyledText("TimeStamp: ", info);

            if (sc.isTimestamped()) {
                try {
                    cp.addStyledTextLine(tFormat.format(tc.getTimeStampDate().getTime()), good);
                    //Validity check
                    boolean tsSig = tc.isTsSignValidated();
                    boolean tsdoc = tc.isMessageImprintValidated();
                    CertVerifyContext tscc = tc.getCertVerifyContext();
                    boolean tsCrl = (tscc.isCrlStatusDetermined() && !tscc.isRevoked());
                    OCSPVerifyContext ocspCont = tscc.getOcspVerifyContext();
                    boolean tsOcsp = false;
                    if (ocspCont != null) {
                        tsOcsp = ocspCont.isOcspCheckOK();
                    }
                    cp.addStyledText("Timestamp validation: ", info);
                    if ((tsSig && tsdoc && (tsCrl || tsOcsp))) {
                        cp.addStyledTextLine("Timestamp is valid", good);
                    } else {
                        cp.addStyledTextLine("failed", error);
                        cp.addStyledText("---> Reason: ", subTitle);
                        cp.addStyledTextLine(!tsSig ? "Invalid signature on timestamp"
                                : !(tsCrl || tsOcsp) ? "Timestamp Authority not trusted"
                                : "Timestamp does not match the PDF document", comment);
                    }
                } catch (NullPointerException ex) {
                    cp.addStyledTextLine("failed", error);
                    cp.addStyledText("---> Reason: ", subTitle);
                    cp.addStyledTextLine("Insufficient information to validate timestamp", comment);
                }
            } else {
                cp.addStyledTextLine("Document is not timestamped", value);

            }
            //QC and SSCD properties
            cp.addStyledText("Qualifying Properties: ", info);
            if (sc.isQualifiedCertificate() && sc.isSscd()) {
                cp.addStyledTextLine("Qualified Electronic Signature (QES)", good);
            } else {
                if (sc.isQualifiedCertificate()) {
                    cp.addStyledTextLine("Signed by a Qualified Certificate (AdES/QC)", value);
                } else {
                    cp.addStyledTextLine("None", comment);
                }
            }

            cp.addLF();
            cp.addStyledTextLine("Signer:", boldUnderline);

            X509Certificate sigCert;
            try {
                sigCert = KsCertFactory.getIaikCert(sc.getSignCert().getEncoded());
                Iterator<Entry<ObjectID, String>> rdns = TreeUtil.getCertNameAttributeSet(sigCert).iterator();
                while (rdns.hasNext()) {
                    Entry<ObjectID, String> entry = rdns.next();
                    cp.addStyledText(entry.getKey().getName(), plain);
                    cp.addStyledText(" = \"", value);
                    cp.addStyledText(entry.getValue(), attribute);
                    cp.addStyledTextLine("\"", value);
                }
            } catch (CertificateEncodingException ex) {
                cp.addStyledText("Signer Certificate is not a valid certificate", info);
            }

            cp.addLF();
            cp.addStyledText("Signature validation result: ", ColorPane.BOLD);
            if (valid) {
                cp.addStyledTextLine((sc.isQualifiedCertificate() && sc.isSscd())
                        ? "Qualified Electronic Signature is valid"
                        : sc.isQualifiedCertificate() ? "Signature is valid and signed by QC"
                        : "Signature is valid", ColorPane.GREEN_BOLD);
            } else {
                cp.addStyledTextLine("Signature is NOT valid", ColorPane.RED_BOLD);
            }
            cp.addLF();
            cp.addLF();



            cp.renderText();
            jTextPaneSigInfo.setCaretPosition(0);
        }
    }

    private void simpleNotification(String string) {
        jTextPaneSigInfo.setText("");
        ColorPane cp = new ColorPane(jTextPaneSigInfo);
        cp.addStyledTextLine("Process notification:", ColorPane.BOLD_UNDERLINE);
        cp.addStyledTextLine(string, ColorPane.ATTRIBUTE);
        cp.renderText();
    }

    //SLASK
    private void testLogOCSPStatus(SignatureValidationContext sc) {
        try {
            CertVerifyContext cvc = sc.getSignCertValidation();
            LOG.info(sc.getSignatureName());

            OCSPVerifyContext oc = cvc.getOcspVerifyContext();
            LOG.info("Has OCSP Validation context: " + String.valueOf(oc != null));
            LOG.info("Is status OK: " + String.valueOf(oc.isOcspCheckOK()));
        } catch (Exception ex) {
        }
    }

    private void getXMLReport() {
        SignatureValidationReport report = new SignatureValidationReport(model);
        xmlPane.setText(report.generateReport());
    }

    private void setupXMLPanel() {
        try {
            xmlPane = new JEditorPane();
            xmlPane.setEditable(false);

            // Instantiate a XMLEditorKit
            XMLEditorKit kit = new XMLEditorKit();

            xmlPane.setEditorKit(kit);

            // Set the font style.
            xmlPane.setFont(new Font("Courier", Font.PLAIN, 12));

            // Set the tab size
            xmlPane.getDocument().putProperty(PlainDocument.tabSizeAttribute,
                    new Integer(4));

            // Enable auto indentation.
            kit.setAutoIndentation(true);

            // Enable tag completion.
            kit.setTagCompletion(true);

            // Enable error highlighting.
            xmlPane.getDocument().putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, true);

            // Set a style
            kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, new Color(51, 96, 0), Font.ITALIC);
            kit.setStyle(XMLStyleConstants.ELEMENT_NAME, new Color(87, 0, 51), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.ELEMENT_PREFIX, new Color(87, 0, 51), Font.PLAIN);

            // Put the editor in a panel that will force it to resize, when a different 
            // view is choosen.
            ScrollableEditorPanel editorPanel = new ScrollableEditorPanel(xmlPane);
            JScrollPane scroller = new JScrollPane(editorPanel);

            // Add the number margin and folding margin as a Row Header View
            JPanel rowHeader = new JPanel(new BorderLayout());
            rowHeader.add(new XMLFoldingMargin(xmlPane), BorderLayout.EAST);
            rowHeader.add(new LineNumberMargin(xmlPane), BorderLayout.WEST);
            scroller.setRowHeaderView(rowHeader);

            GroupLayout jPanelXMLDataLayout = new GroupLayout(jPanelXMLData);
            jPanelXMLData.setLayout(jPanelXMLDataLayout);
            jPanelXMLDataLayout.setHorizontalGroup(
                    jPanelXMLDataLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(scroller, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 781, Short.MAX_VALUE));
            jPanelXMLDataLayout.setVerticalGroup(
                    jPanelXMLDataLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(scroller, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 756, Short.MAX_VALUE));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void openSigFile() {
        File targetDir = new File(baseModel.getConf().getDataDirectory(), SERVER_DOC_FOLDER);
        if (!targetDir.exists()) {
            targetDir = new File(System.getProperty("user.dir"));
        }
        if (selectedSigFileName != null && selectedSigFileName.length() > 0) {
            targetDir = new File(selectedSigFileName).getParentFile();
        }
        JFileChooser fc = new JFileChooser(targetDir);

        FileFilter filt1 = new ExtFileFilter("Signed documents", new String[]{"PDF", "XML"});
        fc.addChoosableFileFilter(filt1);
        int returnVal = fc.showOpenDialog(fc);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.canRead()) {
                selectedSigFileName = file.getAbsolutePath();
                jTextSelectedFile.setText(file.getName());
            }
        }
    }

    // Adding file extension filtering to JFileChooser
    class ExtFileFilter extends FileFilter {

        String description;
        String extensions[];

        public ExtFileFilter(String description, String extension) {
            this(description, new String[]{extension});
        }

        public ExtFileFilter(String description, String extensions[]) {
            if (description == null) {
                this.description = extensions[0];
            } else {
                this.description = description;
            }
            this.extensions = (String[]) extensions.clone();
            toLower(this.extensions);
        }

        private void toLower(String array[]) {
            for (int i = 0, n = array.length; i < n; i++) {
                array[i] = array[i].toLowerCase();
            }
        }

        public String getDescription() {
            return description;
        }

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            } else {
                String path = file.getAbsolutePath().toLowerCase();
                for (int i = 0, n = extensions.length; i < n; i++) {
                    String extension = extensions[i];
                    if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
