package minimmorts.config;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLConfig {
    private Document xdoc;
    private Element xroot;

    public XMLConfig(String file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        xdoc = db.parse(new File(file));
        xroot = xdoc.getDocumentElement();
    }

    private Element getElement(String elementPath) {
        StringTokenizer st = new StringTokenizer(elementPath, "/");
        Element e = xroot;
        NodeList nl;

        while (st.hasMoreTokens()) {
            String tagName = st.nextToken();

            if (!e.hasChildNodes()) {
                return null;
            }

            nl = e.getChildNodes();
            e = null;

            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    e = (Element) nl.item(i);
                    if (e.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
                        if (st.hasMoreTokens()) {
                            break;
                        } else {
                            return e;
                        }
                    } else {
                        e = null;
                    }
                }
            }

            if (e == null) {
                return null;
            }
        }

        return null;
    }

    public String getString(String elementPath, String defaultValue) {
        Element e = getElement(elementPath);

        if (e == null) {
            return defaultValue;
        }

        return e.getTextContent();
    }

    public int getInt(String elementPath, int defaultValue) {
        Element e = getElement(elementPath);

        if (e == null) {
            return defaultValue;
        }

        return Integer.parseInt(e.getTextContent());
    }
}
