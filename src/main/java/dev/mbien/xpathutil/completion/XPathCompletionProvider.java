package dev.mbien.xpathutil.completion;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import dev.mbien.xpathutil.XPathDataObject;
import dev.mbien.xpathutil.ui.XPathEvaluator;
import dev.mbien.xpathutil.ui.XPathTopComponent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
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

        if (type != CompletionProvider.COMPLETION_QUERY_TYPE && type != CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new XPathCompletionQuery(), textComponent);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String str) {
        return str != null && (str.endsWith("/") || str.endsWith("@")) ? CompletionProvider.COMPLETION_QUERY_TYPE : 0;
    }

    private static class XPathCompletionQuery extends AsyncCompletionQuery {

        private final static XPathEvaluator eval = new XPathEvaluator();

        @Override
        protected void query(CompletionResultSet completionResultSet, Document doc, int caretOffset) {

            try {
                String lineTilCaret = doc.getText(0, caretOffset);
                String lineTilCaretStripped = lineTilCaret.strip();

                String exp;

                int slashIndex = lineTilCaret.lastIndexOf('/');
                int atIndex = lineTilCaret.lastIndexOf('@');

                int dotOffset;
                if (lineTilCaretStripped.length() >= 2 && lineTilCaretStripped.charAt(0) == '/' && lineTilCaretStripped.charAt(1) == '/' && lineTilCaretStripped.lastIndexOf('/') == 1) {
                    exp = lineTilCaret.substring(0, slashIndex+1) + "*";
                    dotOffset = caretOffset - lineTilCaret.length() + exp.length()-1;
                } else if (lineTilCaretStripped.length() >= 1 && lineTilCaretStripped.charAt(0) == '/' && lineTilCaretStripped.lastIndexOf('/') == 0) {
                    exp = lineTilCaret.substring(0, slashIndex+1);
                    dotOffset = caretOffset - lineTilCaret.length() + exp.length();
                } else if (slashIndex != -1 || atIndex != -1) {
                    exp = lineTilCaret.substring(0, Math.max(atIndex-1, slashIndex));
                    dotOffset = caretOffset - lineTilCaret.length() + exp.length()+1;
                } else {
                    exp = "/";
                    dotOffset = 0;
                }

                String filterToken = lineTilCaret.substring(Math.max(slashIndex, atIndex)+1, caretOffset);
                if (filterToken.startsWith(":")) {
                    filterToken = filterToken.substring(1);
                }

                try {
                    String xml;
                    {
                        String[] res = new String[1];
                        SwingUtilities.invokeAndWait(() -> res[0] = XPathTopComponent.findInstance().getSourceEditorText());
                        xml = res[0];
                    }

                    NodeList list = (NodeList)eval.evaluate(exp, xml, XPathConstants.NODESET);

                    if (list != null) {

                        // this is faster than parsing a document but kinda hacky
                        boolean hasNamespace = xml.contains("xmlns");

                        Set<String> set = new HashSet<>();

                        for (int b = 0; b < list.getLength(); b++) {

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
                            for (int n = 0; n < childNodes.getLength(); n++) {
                                Node item = childNodes.item(n);
                                String name = item.getNodeName();
                                String localName = item.getLocalName();
                                if (item.getNodeType() == Node.ELEMENT_NODE && (name.startsWith(filterToken) || localName.startsWith(filterToken))) {
                                    if (hasNamespace && item.getPrefix() == null) {
                                        set.add(":"+name);
                                    } else {
                                        set.add(name);
                                    }
                                }
                            }

                        }

                        for (String item : set) {
                            completionResultSet.addItem(new XPathCompletionItem(item, dotOffset, caretOffset));
                        }
                    } else {
                        Completion.get().hideAll();
                    }

                } catch (SAXException | XPathExpressionException ex) {
                    // nothing to auto complete if the document or xpath is not valid
                } catch (InterruptedException | InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }

            } catch (BadLocationException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            completionResultSet.finish();

        }
    }


}
