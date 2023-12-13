package dev.mbien.xpathutil.ui;

import dev.mbien.xpathutil.XPathDataObject;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component for XPath expression evaluation.
 * @author Michael Bien
 */
@TopComponent.Description(
        preferredID = XPathTopComponent.PREFERRED_ID,
        iconBase="dev/mbien/xpathutil/ui/utilities-terminal.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        position = 999,
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "dev.mbien.xpathutil.XPathTopComponent"
)
@ActionReference(
        path = "Menu/Window",
        position = 850
)
@TopComponent.OpenActionRegistration(
        displayName = "XPath",
        preferredID = XPathTopComponent.PREFERRED_ID
)
@NbBundle.Messages({
    "CTL_XPathTopComponent=XPath",
    "HINT_XPathTopComponent=XPath evaluator",
    "XPathTopComponent.expressionLabel.text=Expression:",
    "XPathTopComponent.saveButton.text=Save..."
})
public final class XPathTopComponent extends TopComponent {

    private static final long serialVersionUID = 1L;

    private static XPathTopComponent instance;

    /** path to the icon used by the component and its open action */
    public static final String ICON_PATH = "dev/mbien/xpathutil/ui/utilities-terminal.png";
    public static final String PREFERRED_ID = "XPathTopComponent";

    private transient XPathEvaluatorThread evaluator;
    private transient final DocumentListener docListener;

    private transient DataObject activeDao;
    private transient String lastFilename;

    public XPathTopComponent() {
        
        initComponents();

        setName(NbBundle.getMessage(XPathTopComponent.class, "CTL_XPathTopComponent"));
        setToolTipText(NbBundle.getMessage(XPathTopComponent.class, "HINT_XPathTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        outputPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/xml"));

        docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { xpathTextFieldChanged(e.getDocument()); }
            @Override public void removeUpdate(DocumentEvent e)  { xpathTextFieldChanged(e.getDocument()); }
            @Override public void changedUpdate(DocumentEvent e) { xpathTextFieldChanged(e.getDocument()); }
        };
        xpathTextField.getDocument().addDocumentListener(docListener);

        xpathTextField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume(); // TODO without this, completion items are inserted twice
                    xpathTextFieldChanged(((JTextComponent)e.getComponent()).getDocument());
                }
            }
        });

        EditorRegistry.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName()) && evt.getNewValue() instanceof JTextComponent) {
                editorFocusChanged((JTextComponent) evt.getNewValue());
            } else if (EditorRegistry.COMPONENT_REMOVED_PROPERTY.equals(evt.getPropertyName())) {
                DataObject removed = NbEditorUtilities.getDataObject(((JTextComponent) evt.getOldValue()).getDocument());
                if (activeDao == removed) {
                    activeDao = null;
                }
            }
        });

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JLabel expressionLabel = new JLabel();
        JScrollPane scrollPane = new JScrollPane();
        outputPane = new JEditorPane();
        JButton saveButton = new JButton();
        JScrollPane editorScrollPane = createXPathEditor();

        Mnemonics.setLocalizedText(expressionLabel, NbBundle.getMessage(XPathTopComponent.class, "XPathTopComponent.expressionLabel.text")); // NOI18N

        outputPane.setEditable(false);
        outputPane.setFocusable(false);
        scrollPane.setViewportView(outputPane);

        Mnemonics.setLocalizedText(saveButton, NbBundle.getMessage(XPathTopComponent.class, "XPathTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(this::saveButtonActionPerformed);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(scrollPane, Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(expressionLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(editorScrollPane, GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(saveButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(expressionLabel)
                        .addComponent(saveButton))
                    .addComponent(editorScrollPane))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Export As...");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter("XML files", "xml", "xsd", "xls", "html"));

        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {

            File file = chooser.getSelectedFile();

            if(file != null) {

                try {
                    try {
                        Files.writeString(file.toPath(), outputPane.getText(), StandardOpenOption.CREATE_NEW);
                    } catch (FileAlreadyExistsException ex) {
                        if (JOptionPane.showConfirmDialog(null, "Overwrite existing file?") == JOptionPane.YES_OPTION) {
                            Files.writeString(file.toPath(), outputPane.getText(), StandardOpenOption.TRUNCATE_EXISTING);
                        } else {
                            return;
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                try{
                    DataObject dao = DataObject.find(FileUtil.toFileObject(file));
                    OpenCookie oc = dao.getLookup().lookup(OpenCookie.class);
                    if(oc != null) {
                        oc.open();
                    }else{
                        EditCookie ec = dao.getLookup().lookup(EditCookie.class);
                        if(ec != null) {
                            ec.edit();
                        }
                    }
                }catch(DataObjectNotFoundException ex) {
                    Logger.getLogger(this.getClass().getName()).log(
                        Level.WARNING, "could not open exported xml file in editor", ex);
                }
            }
        }
    }//GEN-LAST:event_saveButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JEditorPane outputPane;
    // End of variables declaration//GEN-END:variables
    private JEditorPane xpathTextField;

    private JScrollPane createXPathEditor() {
        JComponent[] comp = Utilities.createSingleLineEditor(XPathDataObject.MIME_TYPE);
        xpathTextField = (JEditorPane) comp[1];
        return (JScrollPane) comp[0];
    }

    private void editorFocusChanged(JTextComponent inFocus) {
        if (inFocus != null && xpathTextField != inFocus && !inFocus.getClass().getPackageName().startsWith("org.netbeans.modules.quicksearch")) {
            activeDao = NbEditorUtilities.getDataObject(inFocus.getDocument());
            DataObject dao = activeDao;
            if (dao != null) {
                FileObject file = dao.getPrimaryFile();
                if (file != null) {
                    lastFilename = file.getNameExt();
                    return;
                }
            }
            lastFilename = "unknown";
        }
    }

    private void xpathTextFieldChanged(Document doc) {
        try {
            if (activeDao != null) {
                String editorContent = getSourceEditorText();

                if (!editorContent.isBlank()) {
                    setDisplayName("XPath ["+lastFilename+"]");
                    evaluator.asyncEval(doc.getText(0, doc.getLength()), editorContent);
                    return;
                }
            }
            setDisplayName("XPath");
            outputPane.setText("focus xml document");
        } catch (BadLocationException ignored) {}
    }

    public String getSourceEditorText() {
        if (activeDao == null) {
            return "";
        }
        Document doc = activeDao.getLookup().lookup(EditorCookie.class).getDocument();
        if (doc == null) {
            return "";
        }
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException ignored) {}
        return "";
    }

    public static synchronized XPathTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win instanceof XPathTopComponent tc) {
            return tc;
        }
        Logger.getLogger(XPathTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +"' ID!");
        if (instance == null) {
            instance = new XPathTopComponent();
        }
        return instance;
    }

    @Override
    public void componentOpened() {
        setDisplayName("XPath");
        if (evaluator == null) {
            evaluator = new XPathEvaluatorThread(outputPane);
        }
        // xxx: this fixes disappearing auto completion after the window was closed
        // setting editor kit replaces the document too
        xpathTextField.getDocument().removeDocumentListener(docListener);
        xpathTextField.setEditorKit(CloneableEditorSupport.getEditorKit(XPathDataObject.MIME_TYPE));
        xpathTextField.getDocument().addDocumentListener(docListener);
    }

    @Override
    public void componentClosed() {
        if (evaluator != null) {
            evaluator.interrupt();
            evaluator = null;
        }
        activeDao = null;
    }

    public void requestActive(DataObject dao) {
        activeDao = dao;
        super.requestActive();
        xpathTextField.requestFocusInWindow();
    }

}
