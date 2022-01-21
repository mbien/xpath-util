package dev.mbien.xpathutil;

import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorKit;

/**
 * @author Michael Bien
 */
@MimeRegistration(mimeType = XPathDataObject.MIME_TYPE, service = EditorKit.class)
public class XPathEditorKit extends NbEditorKit {

    private static final long serialVersionUID = 1L;

    @Override
    public String getContentType() {
        return XPathDataObject.MIME_TYPE;
    }

}
