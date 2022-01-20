package net.java.xpath;

import net.java.xpath.ui.XPathTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Opens the XPath evaluator view.
 * @author Michael Bien
 */
@ActionID(id = "net.java.xpath.XPathEvalAction", category = "XML")
@ActionRegistration(displayName = "Evaluate XPath", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Menu/BuildProject", position = 887),
    @ActionReference(path = "Loaders/text/xml-mime/Actions", position = 650)
})
public final class XPathEvalAction extends CookieAction {

    private static final long serialVersionUID = 1L;

    @Override
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        if(dataObject != null) {
            dataObject.getLookup().lookup(OpenCookie.class).open();
            XPathTopComponent.findInstance().open();
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{DataObject.class};
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

