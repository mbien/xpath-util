package dev.mbien.xpathutil.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Michael Bien
 */
public class XPathCompletionItem implements CompletionItem {

    private static final ImageIcon ATTRIBUTE_ICON = ImageUtilities.loadImageIcon("dev/mbien/xpathutil/ui/attribute.png", false);
    private static final ImageIcon ELEMENT_ICON = ImageUtilities.loadImageIcon("dev/mbien/xpathutil/ui/element.png", false);

    private final String text;
    private final int caretOffset;
    private final int dotOffset;
    private final boolean attribute;

    public XPathCompletionItem(String text, int dotOffset, int caretOffset) {
        this.text = text;
        this.dotOffset = dotOffset;
        this.caretOffset = caretOffset;
        this.attribute = text.startsWith("@");
    }

    @Override
    public void defaultAction(JTextComponent component) {
        try {
            StyledDocument doc = (StyledDocument) component.getDocument();
            if(component.getSelectionStart() == component.getSelectionEnd()) {
                doc.remove(dotOffset, caretOffset - dotOffset);
            }else{
                doc.remove(dotOffset, component.getSelectionEnd() - dotOffset);
            }
            doc.insertString(dotOffset, text, null);
            Completion.get().hideAll();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if(evt.getKeyCode() == KeyEvent.VK_ENTER && evt.getSource() instanceof JTextComponent)
            defaultAction((JTextComponent) evt.getSource());
    }

    @Override
    public int getPreferredWidth(Graphics graphics, Font font) {
        return CompletionUtilities.getPreferredWidth(text, null, graphics, font);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        ImageIcon icon = attribute ? ATTRIBUTE_ICON : ELEMENT_ICON;
        Color color = selected ? Color.WHITE : backgroundColor;
        String line = "<font><b>" + text + "</b></font>";
        CompletionUtilities.renderHtml(icon, line, null, g, defaultFont, color, width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int i) {
                JToolTip toolTip = new JToolTip();
                toolTip.setTipText("Press Enter to insert \"" + text + "\"");
                completionResultSet.setToolTip(toolTip);
                completionResultSet.finish();
            }
        });
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    @Override
    public int getSortPriority() {
        return attribute ? 2 : 1;
    }

    @Override
    public CharSequence getSortText() {
        return text;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return text;
    }
}
