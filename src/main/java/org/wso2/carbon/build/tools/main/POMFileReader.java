package org.wso2.carbon.build.tools.main;

import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.wso2.carbon.build.tools.database.PopulateDatabase;


/**
 * POM file Reader
 *
 */
public class POMFileReader
{
    private static final Log logger = LogFactory.getLog(POMFileReader.class);

    public static void main( String[] args )  throws Exception
    {
        readPOMFiles();
        System.out.println( "Hello World!" );
    }


    /**
     * Read pom files and get the required elements
     * @param
     * @return
     */
    public static void readPOMFiles() throws Exception {
        ArrayList<File> pomFiles = POMFileReader.loadPOMFiles(Constants.ROOT_PATH);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        if(pomFiles.size() > 0){
            PopulateDatabase.connect();
            for (int i= 0 ; i < pomFiles.size(); i++) {
                System.out.println("POMfile"+(i+1));
                if (Constants.IS_ALL_POMS ||
                        pomFiles.get(i).getPath().split(File.separator)[Constants.ROOT_PATH.split(File.separator).length + 1]
                                .equals(Constants.POM_FILE_NAME)) {

                    try {

                        Document doc = dBuilder.parse(pomFiles.get(i));
                        doc.getDocumentElement().normalize();

                        String artifactId = getXpathValue(doc, Constants.XPATH_ARTIFACT_SOURCE);
                        String groupId = getXpathValue(doc, Constants.XPATH_GROUP_ID);

                        if (groupId.equals("")){
                            groupId = getXpathValue(doc, Constants.XPATH_PARENT_GROUP_ID);
                        }

                        PopulateDatabase.insertData(groupId,artifactId);

                        //Start reading dependencies in the pom file
                        NodeList nList1 = doc.getElementsByTagName("dependencyManagement");

                        if(nList1.getLength() != 0){

                            NodeList nList2 = doc.getElementsByTagName("dependencies");
                            if(nList2.getLength() != 0){
                                Node nNode = nList2.item(0);
                                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) nNode;
                                    //Get all the dependencies under the depencies tag
                                    System.out.println("Inside");
                                    for (int temp = 0; temp < eElement.getElementsByTagName("artifactId").getLength(); temp++) {
                                        String groupId1 = eElement.getElementsByTagName("groupId").item(temp).getTextContent();
                                        System.out.println("GroupID : " + groupId1);
                                        String artifactId1 = eElement.getElementsByTagName("artifactId").item(temp).getTextContent();
                                        System.out.println("ArtifactID : " + artifactId1);
                                        PopulateDatabase.insertData(groupId1,artifactId1);
                                    }
                                }
                            }
                            System.out.println( "End!\n" );
                        }else{
                            System.out.println("other");
                            NodeList nList2 = doc.getElementsByTagName("dependencies");
                            if(nList2.getLength() !=0){
                                Node nNode = nList2.item(0);
                                Element eElement = (Element) nNode;
                                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                    for (int temp = 0; temp < eElement.getElementsByTagName("artifactId").getLength(); temp++) {
                                        String groupId1 = eElement.getElementsByTagName("groupId").item(temp).getTextContent();
                                        System.out.println("GroupID : " + groupId1);
                                        String artifactId1 = eElement.getElementsByTagName("artifactId").item(temp).getTextContent();
                                        System.out.println("ArtifactID : " + artifactId1);
                                        PopulateDatabase.insertData(groupId1,artifactId1);
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception ex) {
                        logger.error("Exception occurred when loading pom" + ex.getMessage());
                    }

                }
            }

            PopulateDatabase.closeConnection();
        }


    }


    /**
     * Load all pom.xml files recursively in given path
     * @param rootPath
     * @return
     */
    public static ArrayList<File> loadPOMFiles(String rootPath) {
        File folder = new File(rootPath);
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> pomFiles = new ArrayList<File>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().equals(Constants.POM_FILE_NAME)) {
                    pomFiles.add(listOfFiles[i]);
                }
            } else if (listOfFiles[i].isDirectory()) {
                pomFiles.addAll(POMFileReader.loadPOMFiles(rootPath + File.separator + listOfFiles[i].getName()));
            }
        }

        return  pomFiles;
    }

    /**
     * Provide value of given xpath expression and xml document
     * @param doc
     * @param expression
     * @return
     * @throws Exception
     */
    public static String getXpathValue(Document doc, String expression)
            throws Exception {
        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                return nNode.getTextContent();
            }
        }

        return "";
    }
}
