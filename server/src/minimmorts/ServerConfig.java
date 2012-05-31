package minimmorts;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author andrey
 */
class ServerConfig {
    private String webPort;

    private String dbHost;
    private String dbPort;
    private String databaseName;
    private String dbUser;
    private String dbPassword;

    public ServerConfig(String confName) throws ParserConfigurationException, SAXException, IOException, NullPointerException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        File f = new File(confName);
        Logger.getLogger(ServerConfig.class.getName()).log(Level.INFO, "Read config file {0}", f.getAbsolutePath());
        Document doc = (Document) docBuilder.parse(f);
        doc.getDocumentElement().normalize();

        Element configElement = (Element) doc.getElementsByTagName("config").item(0);

        Element webserverElement = (Element) configElement.getElementsByTagName("webserver").item(0);
        Element webPortElement = (Element) webserverElement.getElementsByTagName("port").item(0);
        webPort = ((Node) webPortElement.getChildNodes().item(0)).getNodeValue().trim();

        Element databaseElement = (Element) configElement.getElementsByTagName("database").item(0);
        Element dbHostElement = (Element) databaseElement.getElementsByTagName("server").item(0);
        Element dbPortElement = (Element) databaseElement.getElementsByTagName("port").item(0);
        Element databaseNameElement = (Element) databaseElement.getElementsByTagName("database").item(0);
        Element dbUserElement = (Element) databaseElement.getElementsByTagName("user").item(0);
        Element dbPasswordElement = (Element) databaseElement.getElementsByTagName("password").item(0);
        dbHost = ((Node) dbHostElement.getChildNodes().item(0)).getNodeValue().trim();
        dbPort = ((Node) dbPortElement.getChildNodes().item(0)).getNodeValue().trim();
        databaseName = ((Node) databaseNameElement.getChildNodes().item(0)).getNodeValue().trim();
        dbUser = ((Node) dbUserElement.getChildNodes().item(0)).getNodeValue().trim();
        if (dbPasswordElement.getChildNodes().item(0) != null) {
            dbPassword = ((Node) dbPasswordElement.getChildNodes().item(0)).getNodeValue().trim();
        } else {
            dbPassword = "";
        }
    }

    public String getWebPort() {
        return webPort;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}
