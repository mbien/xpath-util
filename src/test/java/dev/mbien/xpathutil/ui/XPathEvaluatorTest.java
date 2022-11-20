package dev.mbien.xpathutil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author mbien
 */
public class XPathEvaluatorTest {

    private static final String XML_SIMPLE = """
        <!-- simple xml -->
        <root>

            <table>
                <tr>
                    <td>Apples</td>
                    <td>Bananas</td>
                </tr>
            </table>

            <!-- another comment -->
            <table>
                <name>Coffee Table</name>
                <width>80</width>
                <length>120</length>
                <height>60</height>
            </table>

            <table name="rudolf" width="80" length="120" height="60">
                <desk>true</desk>
                <standing>false</standing>
            </table>

        </root>
        """;

    private static final String XML_NAMESPACE_1 = """
        <!-- namespaces -->
        <h:root xmlns:h="http://www.w3.org/TR/html4/"
                xmlns:f="http://www.w3schools.com/furniture">

            <h:table>
                <h:tr>
                    <h:td>Apples</h:td>
                    <h:td>Bananas</h:td>
                </h:tr>
            </h:table>

            <f:table>
                <f:name>Coffee Table</f:name>
                <f:size>80</f:size>
            </f:table>

            <f:table>
                <f:name>Hash Table</f:name>
                <f:size>512</f:size>
            </f:table>

        </h:root>
        """;


    private static final String XML_NAMESPACE_2 = """
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>
            <groupId>dev.mbien</groupId>
            <artifactId>mavenproject2</artifactId>
            <version>0.1-SNAPSHOT</version>
            <packaging>jar</packaging>
            <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <maven.compiler.source>18</maven.compiler.source>
                <maven.compiler.target>18</maven.compiler.target>
                <exec.mainClass>dev.mbien.mavenproject2.Mavenproject2</exec.mainClass>
            </properties>
            <build>
                <plugins>
                    <plugin>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.10.1</version>
                    </plugin>
                    <plugin>
                        <artifactId>maven-surefire-plugin</artifactId>
                        <version>3.0.0-M5</version>
                    </plugin>
                </plugins>
            </build>
        </project>
        """;


    @Test
    public void evalXPathSimple() throws Exception {

        XPathEvaluator eval = new XPathEvaluator();

        assertEquals("<name>Coffee Table</name>", eval.evalXPathToString("/root/table/name", XML_SIMPLE).strip());
        assertEquals("<name>Coffee Table</name>", eval.evalXPathToString("//name", XML_SIMPLE).strip());
        assertEquals("rudolf", eval.evalXPathToString("//@name", XML_SIMPLE).strip());
        assertEquals("rudolf", eval.evalXPathToString("/root/table[@name]/@name", XML_SIMPLE).strip());
    }

    @Test
    public void evalXPathNameSpace1() throws Exception {

        XPathEvaluator eval = new XPathEvaluator();

        assertEquals("Hash Table", eval.evalXPathToString("//f:table[f:size>100]/f:name/text()", XML_NAMESPACE_1).strip());
        assertEquals("""
                     Apples
                     Bananas""",
                eval.evalXPathToString("/h:root/h:table/h:tr/h:td/text()", XML_NAMESPACE_1).strip());
    }

    @Test
    public void evalXPathNameSpace2() throws Exception {

        XPathEvaluator eval = new XPathEvaluator();

        assertEquals("18", eval.evalXPathToString("/:project/:properties/:maven.compiler.source/text()", XML_NAMESPACE_2).strip());
        assertEquals("""
                     maven-compiler-plugin
                     maven-surefire-plugin""",
                eval.evalXPathToString("//:plugin/:artifactId/text()", XML_NAMESPACE_2).strip());
    }


}
