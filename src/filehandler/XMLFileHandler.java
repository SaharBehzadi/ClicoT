package filehandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import attributes.Attribute;
import attributes.AttributeStructure;
import attributes.CategoricalAttribute;
import attributes.NumericalAttribute;
import data.DataSet;
import misc.Utils;
import tree.Node;

import org.xml.sax.ext.DefaultHandler2;

public class XMLFileHandler {

	public static StringBuilder dataString = new StringBuilder();
	public static AttributeStructure attributes = new AttributeStructure();
	public static Attribute as = new Attribute();
	public static DataSet data = null;
	private static int Node_id = 0;
	private static int Numerical_id = 0;

	public DataSet read(String filename) {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler2() {

				boolean dataset_tag = false;
				boolean attributes_tag = false;
				boolean attribute_tag = false;
				boolean hierarchy_tag = false;
				boolean cdata_tag = false;
				boolean categorical_tag = false;
				boolean type_tag = false;
				boolean name_tag = false;
				int num_of_numerical_ats = 0;
				int num_of_categorical_ats = 0;
				int cdatacount = 0;
				ArrayList<String> nameslist = new ArrayList<String>();
				String typeString = new String();

				public void startElement(String uri, String localName, String qName, Attributes xmlattributes)
						throws SAXException {

					// System.out.println("Start Element :" + qName);

					if (qName.equalsIgnoreCase("DATASET")) {
						dataset_tag = true;
					}

					if (qName.equalsIgnoreCase("ATTRIBUTES")) {
						attributes_tag = true;
					}

					if (qName.equalsIgnoreCase("ATTRIBUTE")) {
						attribute_tag = true;
					}

					if (qName.equalsIgnoreCase("HIERARCHY")) {
						hierarchy_tag = true;
					}

					if (qName.equalsIgnoreCase("TYPE")) {
						type_tag = true;
					}

					if (qName.equalsIgnoreCase("NAME")) {
						name_tag = true;
					}

					if (nameslist.contains(qName)) {
						categorical_tag = true;

						ArrayList<CategoricalAttribute> categoricalAts = attributes.getCategoricalAttributes();
						for (int i = 0; i < categoricalAts.size(); i++) {
							CategoricalAttribute at = categoricalAts.get(i);

							if (at.getName().equals(qName)) {
								at.setDescription(xmlattributes.getValue("name"));
							}

						}
					}

				}// Start Element

				public void startCDATA() throws SAXException {

					cdata_tag = true;

				}

				public void endElement(String uri, String localName, String qName) throws SAXException {

					if (qName == "dataset") {

						data = new DataSet(dataString.toString(), attributes, "xml");
						dataset_tag = false;
					}

					if (qName == "attributes") {
						attributes.setNumOfNumericals(num_of_numerical_ats);
						attributes.setNumOfCategoricals(num_of_categorical_ats);
					}

					if (qName == "attribute") {
						attributes.add(as);
					}

				}

				public void characters(char ch[], int start, int length) throws SAXException {

					if (dataset_tag) {
						dataString.append(new String(ch, start, length));

					}

					if (attributes_tag) {
						attributes_tag = false;
					}

					if (attribute_tag) {

						attribute_tag = false;
					}

					if (type_tag) {

						typeString = new String(ch, start, length);
						if (typeString.equals("Numerical")) {
							as = new NumericalAttribute();
							num_of_numerical_ats += 1;
						}
						if (typeString.equals("Categorical")) {
							as = new CategoricalAttribute();
							num_of_categorical_ats += 1;
						}
						as.setType(typeString);
						type_tag = false;

					}

					if (name_tag) {
						String namestring = new String(ch, start, length);
						as.setName(namestring);
						if (typeString.contains("Categorical")) {
							nameslist.add(namestring);
						}
						name_tag = false;
					}

					if (hierarchy_tag) {

						// hierarchy_tag = false;
					}

					if (categorical_tag) {

						categorical_tag = false;
					}

					if (cdata_tag) {
						ArrayList<CategoricalAttribute> categoricalAts = attributes.getCategoricalAttributes();
						String attributename = nameslist.get(cdatacount);
						for (int i = 0; i < categoricalAts.size(); i++) {
							CategoricalAttribute at = categoricalAts.get(i);

							if (at.getName().equals(attributename)) {
								
								((CategoricalAttribute) at).rootNode = buildTreeFromString(
										new String(ch, start, length), ((CategoricalAttribute) at).getDescription(), 0,
										Node_id);
								break;
							}
						}
						cdata_tag = false;
						cdatacount++;
					}

				}

			};

			File file = new File(filename);
			InputStream inputStream = new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");

			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");

			saxParser.setProperty("http://xml.org/sax/properties/" + "lexical-handler", handler);
			saxParser.parse(is, handler);

			// saxParser.parse(filename, handler);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;

	}// read

	private Node buildTreeFromString(String xmlString, String desc, int pathlength, int id) {

		int i = 0, j;
		Node node = new Node(Utils.trim(desc), pathlength, id);

		if (pathlength == 0) {
			node.setRoot(true);
			node.id = Node_id++;
		}

		pathlength++;

		while (i < xmlString.length() - 1) {

			String next = xmlString.substring(xmlString.indexOf("<", i) + 1, j = xmlString.indexOf(">", i + 1));
			String enclosed = xmlString.substring(j + 1, i = xmlString.indexOf("</" + next, j));
			i += next.length() + 3;
			node.addChild(buildTreeFromString(enclosed, next, pathlength, Node_id++));

		}

		if (i == 0)
			node.setLeaf(true);

		return node;
	}

}
