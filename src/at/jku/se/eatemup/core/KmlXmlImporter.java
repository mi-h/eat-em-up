package at.jku.se.eatemup.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.jku.se.eatemup.core.database.DataStore2;
import at.jku.se.eatemup.core.model.Position;

public class KmlXmlImporter {

	public static void Import(DataStore2 datastore) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		try {
			Document document = builder.parse(new FileInputStream(
					"./kml_xml_files/LocationPoints.kml.xml"));
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//coordinates";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
					document, XPathConstants.NODESET);
			ArrayList<Double[]> values = new ArrayList<>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				String temp = nodeList.item(i).getFirstChild().getNodeValue();
				try {
					String[] split = temp.split(",");
					Double[] coords = new Double[2];
					coords[0] = Double.parseDouble(split[0]);
					coords[1] = Double.parseDouble(split[1]);
					values.add(coords);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			datastore.createTables();
			int cnt = 1;
			for (Double[] val : values){
				Position temp = new Position();
				temp.setLongitude(val[0]);
				temp.setLatitude(val[1]);
				System.out.println("adding goodielocation "+cnt+": "+val[0]+", "+val[1]);
				cnt++;
				datastore.addGoodiePosition(temp);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} finally {
			datastore.closeConnection();
		}
	}

	public static void main(String[] args) {
		Import(new DataStore2());
	}
}
