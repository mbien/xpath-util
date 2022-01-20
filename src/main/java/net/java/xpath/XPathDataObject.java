package net.java.xpath;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;

/**
 *
 * @author Michael Bien
 */
@MIMEResolver.ExtensionRegistration(
        displayName = "XPath File",
        mimeType = XPathDataObject.MIME_TYPE,
        extension = {"xpath"}
)
@DataObject.Registration(
        mimeType = XPathDataObject.MIME_TYPE,
        iconBase = "net/java/xpath/ui/utilities-terminal.png",
        displayName = "XPath File Loader",
        position = 300
)
public class XPathDataObject extends MultiDataObject {

    private static final long serialVersionUID = 1L;

    public static final String MIME_TYPE = "text/x-xpath";

    public XPathDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
