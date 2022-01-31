package dev.mbien.xpathutil.ui;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael Bien
 */
public class XPathEvaluator {

    private Transformer transformer;
    private DocumentBuilder docBuilder;
    private final XPathFactory xFac;

    public XPathEvaluator() {
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (TransformerConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            docBuilder = factory.newDocumentBuilder();
            docBuilder.setEntityResolver((String publicId, String systemId) -> new InputSource(new StringReader("")));
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        xFac = XPathFactory.newDefaultInstance();
    }

    public String evalXPathToString(String xpath, String xml) throws SAXException, IOException, TransformerException, XPathExpressionException {

        if (docBuilder == null || transformer == null) {
            return "";
        }

        NodeList resultXML = (NodeList) evaluate(xpath, xml, XPathConstants.NODESET);

        if (resultXML.getLength() > 0) {

            StringBuilder sb = new StringBuilder(resultXML.getLength()*80);

            for (int i = 0; i < resultXML.getLength(); i++) {

                Node item = resultXML.item(i);
                String nodeValue = item.getNodeValue();
                if (nodeValue == null) {
                    stripWhitespace(item);
                    StringWriter writer = new StringWriter(128);
                    transformer.transform(new DOMSource(item), new StreamResult(writer));
                    sb.append(writer.getBuffer());
                } else {
                    sb.append(nodeValue).append("\n");
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public Object evaluate(String xpath, String xml, QName ret) throws SAXException, IOException, XPathExpressionException {

        Document sourceXML = docBuilder.parse(new InputSource(new StringReader(xml)));

        XPath xPath = xFac.newXPath();
        xPath.setNamespaceContext(new UniversalNamespaceResolver(sourceXML));
        XPathExpression expr = xPath.compile(xpath);
        return expr.evaluate(sourceXML, ret);

    }

    public static void stripWhitespace(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                child.setTextContent(child.getTextContent().strip());
            }
            stripWhitespace(child);
        }
    }

    private final static class UniversalNamespaceResolver implements NamespaceContext {

        private final Document sourceDocument;
        
        public UniversalNamespaceResolver(Document document) {
            sourceDocument = document;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return sourceDocument.lookupNamespaceURI(null);
            } else {
                return sourceDocument.lookupNamespaceURI(prefix);
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return sourceDocument.lookupPrefix(namespaceURI);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
