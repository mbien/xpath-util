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
        return type == CompletionProvider.COMPLETION_QUERY_TYPE || type == CompletionProvider.COMPLETION_ALL_QUERY_TYPE
                ? new AsyncCompletionTask(new XPathCompletionQuery(), textComponent) : null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String str) {
        return str != null && (str.endsWith("/") || str.endsWith("@") || str.endsWith("[")) ? CompletionProvider.COMPLETION_QUERY_TYPE : 0;
    }

    private static class XPathCompletionQuery extends AsyncCompletionQuery {

        private final static XPathEvaluator eval = new XPathEvaluator();

        @Override
        protected void query(CompletionResultSet completionResultSet, Document doc, int caretIndex) {

            try {
                String line = doc.getText(0, caretIndex);
                String line_stripped = line.strip();

                String exp;

                int slashIndex = line.lastIndexOf('/');
                int slashIndex_stripped = line_stripped.lastIndexOf('/');

                int atIndex = line.lastIndexOf('@');
                int bracketIndex = line.lastIndexOf('[');

                boolean in_square_bracket = bracketIndex > line.lastIndexOf(']');

                int dotOffset;
                if (in_square_bracket) {
                    exp = line.substring(0, bracketIndex);
                    String subpath = line.substring(bracketIndex+1, caretIndex);
                    if (subpath.contains("/")) {
                        subpath = subpath.substring(0, subpath.lastIndexOf('/') + 1);
                        if (subpath.endsWith("/") && !subpath.startsWith("@")) {
                            exp += "/" + subpath.substring(0, subpath.length() - 1);
                        }
                    }
                    dotOffset = caretIndex - line.length() + exp.length() + 1;
                } else if (slashIndex_stripped == 1 && line_stripped.startsWith("//")) {
                    exp = line.substring(0, slashIndex + 1) + "*";
                    dotOffset = caretIndex - line.length() + exp.length() - 1;
                } else if (slashIndex_stripped == 0) {
                    exp = line.substring(0, slashIndex + 1);
                    dotOffset = caretIndex - line.length() + exp.length();
                } else if (slashIndex_stripped != -1 || atIndex != -1) {
                    exp = line.substring(0, max(atIndex - 1, slashIndex));
                    dotOffset = caretIndex - line.length() + exp.length() + 1;
                } else {
                    exp = "/";
                    dotOffset = 0;
                }

                String prefix = line.substring(max(slashIndex, atIndex - 1, bracketIndex) + 1, caretIndex);
                if (prefix.startsWith(":")) {
                    prefix = prefix.substring(1);
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

                        Set<String> items = new HashSet<>();

                        for (int b = 0; b < list.getLength(); b++) {

                            Node current = list.item(b);

                            NamedNodeMap attributes = current.getAttributes();
                            if (attributes != null) {
                                for(int a = 0; a < attributes.getLength(); a++) {
                                    String name = attributes.item(a).getNodeName();
                                    if (name != null && (name = "@"+name).startsWith(prefix)) {
                                        items.add(name);
                                    }
                                }
                            }
                            NodeList childNodes = current.getChildNodes();
                            for (int n = 0; n < childNodes.getLength(); n++) {
                                Node item = childNodes.item(n);
                                String name = item.getNodeName();
                                String localName = item.getLocalName();
                                if (item.getNodeType() == Node.ELEMENT_NODE && (name.startsWith(prefix) || localName.startsWith(prefix))) {
                                    if (hasNamespace && item.getPrefix() == null) {
                                        items.add(":"+name);
                                    } else {
                                        items.add(name);
                                    }
                                }
                            }

                        }

                        if (items.isEmpty() && "text()".startsWith(prefix)) {
                            items.add("text()");
                        }

                        for (String item : items) {
                            completionResultSet.addItem(new XPathCompletionItem(item, dotOffset, caretIndex));
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

        private static int max(int a, int b) {
            return Math.max(a, b);
        }

        private static int max(int a, int b, int c) {
            return Math.max(Math.max(a, b), c);
        }
    }


}
