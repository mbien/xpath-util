package dev.mbien.xpathutil.ui;

import dev.mbien.xpathutil.XPathDataObject;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
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
 * Top component for XPath evaluation.
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

    private XPathEvaluatorThread evaluator;
    private JTextComponent lastFocusedEditor;
    private final DocumentListener docListener;

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
        xpathTextField.setEditorKit(CloneableEditorSupport.getEditorKit(XPathDataObject.MIME_TYPE));

        docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { xpathTextFieldChanged(e.getDocument()); }
            @Override public void removeUpdate(DocumentEvent e)  { xpathTextFieldChanged(e.getDocument()); }
            @Override public void changedUpdate(DocumentEvent e) { xpathTextFieldChanged(e.getDocument()); }
        };
        xpathTextField.getDocument().addDocumentListener(docListener);

        xpathTextField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume(); // hack: consume the return key to prevent new lines
                    xpathTextFieldChanged(((JTextComponent)e.getComponent()).getDocument());
                }
            }
        });

        EditorRegistry.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName()) && evt.getOldValue() instanceof JTextComponent) {
                editorFocusChanged((JTextComponent) evt.getOldValue());
            } else if (EditorRegistry.COMPONENT_REMOVED_PROPERTY.equals(evt.getPropertyName())) {
                if (evt.getOldValue() == lastFocusedEditor) {
                    lastFocusedEditor = null;
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
        xpathTextField = new JEditorPane();

        Mnemonics.setLocalizedText(expressionLabel, NbBundle.getMessage(XPathTopComponent.class, "XPathTopComponent.expressionLabel.text")); // NOI18N

        outputPane.setEditable(false);
        outputPane.setFocusable(false);
        scrollPane.setViewportView(outputPane);

        Mnemonics.setLocalizedText(saveButton, NbBundle.getMessage(XPathTopComponent.class, "XPathTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        xpathTextField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        xpathTextField.setFont(new JTextField().getFont());

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
                        .addComponent(xpathTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(saveButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
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
    private JEditorPane xpathTextField;
    // End of variables declaration//GEN-END:variables


    private void editorFocusChanged(JTextComponent last) {
        if (last != null && xpathTextField != last && !last.getClass().getPackageName().startsWith("org.netbeans.modules.quicksearch")) {
            lastFocusedEditor = last;
        }
    }

    private void xpathTextFieldChanged(Document doc) {
        try {
            editorFocusChanged(EditorRegistry.lastFocusedComponent());
            if(lastFocusedEditor != null) {
                String editorContent = getSourceEditorText();

                if(editorContent != null) {
                    evaluator.asyncEval(doc.getText(0, doc.getLength()), editorContent);
                    return;
                }
            }
            outputPane.setText("please focus an edior containing a xml file");
        } catch (BadLocationException ignored) {}
    }

    public String getSourceEditorText() {
        if (lastFocusedEditor == null || lastFocusedEditor.getDocument() == null) {
            return "";
        }
        Document doc = lastFocusedEditor.getDocument();
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException ignored) {}
        return "";
    }

    public static synchronized XPathTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win instanceof XPathTopComponent) {
            return (XPathTopComponent) win;
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
        if(evaluator == null) {
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
        if(evaluator != null) {
            evaluator.interrupt();
            evaluator = null;
        }
    }

}
