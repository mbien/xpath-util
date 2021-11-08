/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.xpath;

import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

import static org.junit.Assert.*;

public class XPathDataObjectTest {

    @Test
    public void testDataObject() throws Exception {
        FileObject root =  FileUtil.getConfigRoot();
        FileObject template = root.getFileObject("Templates/Other/XPathTemplate.xpath");
        assertNotNull("Template file shall be found", template);

        DataObject obj = DataObject.find(template);
        assertEquals("It is our data object", XPathDataObject.class, obj.getClass());
    }
    
}
