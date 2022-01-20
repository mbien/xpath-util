package net.java.xpath.completion;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import net.java.xpath.XPathDataObject;
import net.java.xpath.ui.XPathEvaluator;
import net.java.xpath.ui.XPathTopComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael Bien
 */
@MimeRegistration(mimeType = XPathDataObject.MIME_TYPE, service = CompletionProvider.class)
public class XPathCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int type, JTextComponent textComponent) {

        if (type != CompletionProvider.COMPLETION_QUERY_TYPE || XPathTopComponent.getDefault().lastFocusedEditor == null) {
            return null;
        }

        return new AsyncCompletionTask(new XPathCompletionQuery(), textComponent);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String str) {
        if (str != null && (str.endsWith("/") || str.endsWith("@"))) {
            return CompletionProvider.COMPLETION_QUERY_TYPE;
        } else {
            return 0;
        }
    }

    private static class XPathCompletionQuery extends AsyncCompletionQuery {

        @Override
        protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {

            try {
                final StyledDocument bDoc = (StyledDocument) document;

                String lineTilCaret = bDoc.getText(0, caretOffset);
                String lineTilCaretTrimed = lineTilCaret.strip();

                String exp;
                String filterToken;

                int slashIndex = lineTilCaret.lastIndexOf('/');
                int atIndex = lineTilCaret.lastIndexOf('@');

                int dotOffset;
                if(lineTilCaretTrimed.length() >= 2 && lineTilCaretTrimed.charAt(0) == '/' && lineTilCaretTrimed.charAt(1) == '/' && lineTilCaretTrimed.lastIndexOf('/') == 1) {
                    exp = lineTilCaret.substring(0, slashIndex+1) + "*";
                    dotOffset = caretOffset - lineTilCaret.length() + exp.length()-1;
                }else if(lineTilCaretTrimed.length() >= 1 && lineTilCaretTrimed.charAt(0) == '/' && lineTilCaretTrimed.lastIndexOf('/') == 0) {
                    exp = lineTilCaret.substring(0, slashIndex+1);
                    dotOffset = caretOffset - lineTilCaret.length() + exp.length();
                }else if(slashIndex != -1 || atIndex != -1) {
                    exp = lineTilCaret.substring(0, Math.max(atIndex-1, slashIndex));
                    dotOffset = caretOffset - lineTilCaret.length() + exp.length()+1;
                } else{
                    exp = "/";
                    dotOffset = 0;
                }

                filterToken = lineTilCaret.substring(Math.max(slashIndex, atIndex)+1, caretOffset);

                XPathEvaluator eval = new XPathEvaluator();

                try {
                    final String xml = XPathTopComponent.getDefault().lastFocusedEditor.getText();

                    NodeList list = (NodeList)eval.evaluate(exp, xml, XPathConstants.NODESET);

                    if(list != null) {

                        Set<String> set = new HashSet<>();

                        for(int b = 0; b < list.getLength(); b++) {

                            Node current = list.item(b);

                            NamedNodeMap attributes = current.getAttributes();
                            if (attributes != null) {
                                for(int a = 0; a < attributes.getLength(); a++) {
                                    String name = attributes.item(a).getNodeName();
                                    if(name != null && name.startsWith(filterToken)) {
                                        set.add("@"+name);
                                    }
                                }
                            }
                            NodeList childNodes = current.getChildNodes();
                            for(int n = 0; n < childNodes.getLength(); n++) {
                                Node item = childNodes.item(n);
                                String name = item.getNodeName();
                                if(item.getNodeType() == Node.ELEMENT_NODE && name.startsWith(filterToken)) {
                                    set.add(name);
                                }
                            }

                        }

                        for (String item : set) {
                            completionResultSet.addItem(new XPathCompletionItem(item, dotOffset, caretOffset));
                        }
                    }else{
                        Completion.get().hideAll();
                    }

                } catch (SAXException | XPathExpressionException ex) {
                    // nothing to auto complete if the document or xpath is not valid
                }

            } catch (BadLocationException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            completionResultSet.finish();

        }
    }


}
