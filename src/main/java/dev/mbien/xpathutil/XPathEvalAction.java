package dev.mbien.xpathutil;

import dev.mbien.xpathutil.ui.XPathTopComponent;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Opens the XPath evaluator view.
 * @author Michael Bien
 */
@ActionID(id = "dev.mbien.xpathutil.XPathEvalAction", category = "XML")
@ActionRegistration(displayName = "Evaluate XPath", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Menu/BuildProject", position = 887),
    @ActionReference(path = "Loaders/text/xml-mime/Actions", position = 650),
    @ActionReference(path = "Loaders/text/html/Actions", position = 650),
    @ActionReference(path = "Loaders/text/dtd-xml/Actions", position = 20050),
    @ActionReference(path = "Loaders/text/x-maven-pom+xml/Actions", position = 350),
    @ActionReference(path = "Loaders/text/xhtml/Actions", position = 885)
})
@NbBundle.Messages({
    "CTL_XPathEvalAction=Evaluate XPath"
})
public final class XPathEvalAction extends CookieAction {

    private static final long serialVersionUID = 1L;

    @Override
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (dataObject != null) {
            XPathTopComponent xpathComponent = XPathTopComponent.findInstance();
            xpathComponent.open();
            EditorCookie file = dataObject.getLookup().lookup(EditorCookie.class);
            if (file != null) {
                file.open();
            }
            xpathComponent.requestActive(dataObject);
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(XPathEvalAction.class, "CTL_XPathEvalAction");
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class<?>[] { ValidateXMLCookie.class };
    }

    @Override
    protected String iconResource() {
        return XPathTopComponent.ICON_PATH;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}

