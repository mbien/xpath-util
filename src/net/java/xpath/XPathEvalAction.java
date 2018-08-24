package net.java.xpath;

import net.java.xpath.ui.XPathTopComponent;
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
public final class XPathEvalAction extends CookieAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        if(dataObject != null) {
            dataObject.getCookie(OpenCookie.class).open();
            XPathTopComponent.findInstance().open();
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(XPathEvalAction.class, "CTL_XPathEvalAction");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    protected String iconResource() {
        return XPathTopComponent.ICON_PATH;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

}

