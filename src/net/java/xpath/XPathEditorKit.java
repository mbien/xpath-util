
package net.java.xpath;

import org.netbeans.modules.editor.NbEditorKit;

/**
 * @author Michael Bien
 */
public class XPathEditorKit extends NbEditorKit {

    @Override
    public String getContentType() {
        return "text/x-xpath";
    }

}
