package net.java.xpath.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component for XPath evaluation.
 * @author Michael Bien
 */
public final class XPathTopComponent extends TopComponent {

    private static XPathTopComponent instance;

    /** path to the icon used by the component and its open action */
    public static final String ICON_PATH = "net/java/xpath/ui/utilities-terminal.png";

    private static final String PREFERRED_ID = "XPathTopComponent";

    private XPathEvaluatorThread evaluator;
    public JTextComponent lastFocusedEditor;

    private XPathTopComponent() {

//        org.netbeans.spi.editor.mimelookup.MimeDataProvider mdp = Lookup.getDefault().lookup(MimeDataProvider.class);
//        Lookup mime = mdp.getLookup(MimePath.get("text/x-xpath"));
//        java.util.prefs.Preferences pref = mime.lookup(Preferences.class);
//        pref.putInt("SimpleValueNames.TEXT_LIMIT_WIDTH"/*org.netbeans.modules.editor.lib.EditorPreferencesKeys.TEXT_LIMIT_WIDTH*/, 1);
        
        initComponents();

        setName(NbBundle.getMessage(XPathTopComponent.class, "CTL_XPathTopComponent"));
        setToolTipText(NbBundle.getMessage(XPathTopComponent.class, "HINT_XPathTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        outputPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/xml"));
        xpathTextField.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-xpath"));

        xpathTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                xpathTextFieldChanged(e);
            }

            public void removeUpdate(DocumentEvent e) {
                xpathTextFieldChanged(e);
            }

            public void changedUpdate(DocumentEvent e) {
                xpathTextFieldChanged(e);
            }
        });

        // hack: consume the return key to prevent new lines
        xpathTextField.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                    e.consume();
            }

            public void keyTyped(KeyEvent e) {  }
            public void keyReleased(KeyEvent e) {  }
        });

        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                    if(xpathTextField != evt.getOldValue() && evt.getOldValue() instanceof JTextComponent) {
                        lastFocusedEditor = (JTextComponent)evt.getOldValue();
                    }
                }
            }
        });


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JLabel expressionLabel = new JLabel();
        JScrollPane scrollPane = new JScrollPane();
        outputPane = new JEditorPane();
        JButton saveButton = new JButton();
        xpathTextField = new JEditorPane();

        Mnemonics.setLocalizedText(expressionLabel, NbBundle.getMessage(XPathTopComponent.class, "XPathTopComponent.expressionLabel.text")); // NOI18N
        outputPane.setEditable(false);
        outputPane.setFocusable(false);
        scrollPane.setViewportView(outputPane);
        Mnemonics.setLocalizedText(saveButton, NbBundle.getMessage(XPathTopComponent.class, "XPathTopComponent.saveButton.text"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        xpathTextField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        xpathTextField.setFont(new JTextField().getFont());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(scrollPane, Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(expressionLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(xpathTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(saveButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(expressionLabel)
                        .addComponent(saveButton))
                    .addComponent(xpathTextField, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed

        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);

        chooser.setDialogTitle("Export As...");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter("XML files", "xml", "xsd", "xls", "html"));

        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {

            File output = chooser.getSelectedFile();

            if(output != null) {
                String content = outputPane.getText();

                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(output);
                    fileWriter.write(content);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if(fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                try{
                    DataObject dao = DataObject.find(FileUtil.toFileObject(output));
                    OpenCookie oc = dao.getLookup().lookup(OpenCookie.class);
                    if(oc != null) {
                        oc.open();
                    }else{
                        EditCookie ec = dao.getLookup().lookup(EditCookie.class);
                        if(ec != null) {
                            ec.edit();
                        }
                    }
                }catch(Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(
                        Level.WARNING, "could not open exported xml file in editor", ex);
                }
            }

        }

    }//GEN-LAST:event_saveButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JEditorPane outputPane;
    private JEditorPane xpathTextField;
    // End of variables declaration//GEN-END:variables


    private final void xpathTextFieldChanged(DocumentEvent e) {
        try {
            String text = e.getDocument().getText(0, e.getDocument().getLength());

            JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();

            if(lastFocusedComponent != xpathTextField)
                lastFocusedEditor = lastFocusedComponent;

            if(lastFocusedEditor != null) {

                String editorContent = lastFocusedEditor.getText();

                if(editorContent != null) {
                    evaluator.asyncEval(text, editorContent);
                }else {
                    outputPane.setText("<please focus an edior containing a xml file first>");
                }
            }
        } catch (BadLocationException ex) {
            // we can ignore this;
        }
    }


    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized XPathTopComponent getDefault() {
        if (instance == null) {
            instance = new XPathTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the XPathTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized XPathTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(XPathTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof XPathTopComponent) {
            return (XPathTopComponent) win;
        }
        Logger.getLogger(XPathTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        if(evaluator == null) {
            evaluator = new XPathEvaluatorThread(outputPane);
        }
    }

    @Override
    public void componentClosed() {
        if(evaluator != null) {
            evaluator.interrupt();
            evaluator = null;
        }
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return XPathTopComponent.getDefault();
        }
    }
}
