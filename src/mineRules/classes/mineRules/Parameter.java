package mineRules;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Parameter {
	
	private boolean MRAR, MRARE, LOD, PREVIEW;
	private double Support, Confidence;
	private int minLevel, maxLevel;
	private String Dataset, Endpoint, NameOutputMRAR, PathOutputMRAR, NameOutputMRARE, PathOutputMRARE;
	private List<String> Labels, Namespaces, QueryPropertiesLabel;
	
	public Parameter(String file) throws Exception {
		readParameters(file);
	}
	
	public boolean isMRAR() {
		return MRAR;
	}
	
	public boolean isMRARE() {
		return MRARE;
	}
	
	public boolean isLOD() {
		return LOD;
	}
	
	public boolean isPREVIEW() {
		return PREVIEW;
	}

	public double getSupport() {
		return Support;
	}

	public double getConfidence() {
		return Confidence;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public String getDataset() {
		return Dataset;
	}
	
	public String getEndpoint() {
		return Endpoint;
	}
	
	public String getNameOutputMRAR() {
		return NameOutputMRAR;
	}
	
	public String getPathOutputMRAR() {
		return PathOutputMRAR;
	}
	
	public String getNameOutputMRARE() {
		return NameOutputMRARE;
	}
	
	public String getPathOutputMRARE() {
		return PathOutputMRARE;
	}
	
	public List<String> getLabels(){
		return Labels;
	}
	
	public List<String> getNamespaces(){
		return Namespaces;
	}
	
	public List<String> getQueryPropertiesLabel() {
		return QueryPropertiesLabel;
	}
	
	private void readParameters(String arg) throws Exception  {
		//Build DOM
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	Document doc = builder.parse(arg);
    	
    	//Create XPath
    	 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        
        //Read Parameters
        
        this.MRAR = getBool(doc, xpath, "MODULE/MRAR");
        this.MRARE = getBool(doc, xpath, "MODULE/MRARE");
        this.LOD = getBool(doc, xpath, "MODULE/LOD");
        this.PREVIEW = getBool(doc, xpath, "INPUT/TARGET/PREVIEW");
        this.Support = getNumber(doc, xpath, "RULE/SUPPORT");
        this.Confidence = getNumber(doc, xpath, "RULE/CONFIDENCE");
        this.minLevel = (int)getNumber(doc, xpath, "RULE/LEVEL/MIN");
        this.maxLevel = (int) getNumber(doc, xpath, "RULE/LEVEL/MAX");
        this.Dataset = getString(doc, xpath, "INPUT/SOURCE/FILE");
        this.QueryPropertiesLabel = getList(doc, xpath, "INPUT/TARGET/PROPERTY");
        this.Endpoint = getString(doc, xpath, "INPUT/TARGET/ENDPOINT");
        this.NameOutputMRAR = getString(doc, xpath, "OUTPUT/MRAR/NAME");
        this.PathOutputMRAR = getString(doc, xpath, "OUTPUT/MRAR/PATH");
        this.NameOutputMRARE = getString(doc, xpath, "OUTPUT/MRARE/NAME");
        this.PathOutputMRARE = getString(doc, xpath, "OUTPUT/MRARE/PATH");
        this.Labels = getList(doc, xpath, "INPUT/TARGET/PREFIX/LABEL");
        this.Namespaces = getList(doc, xpath, "INPUT/TARGET/PREFIX/NAMESPACE");
	}
	
	private String getString(Document doc, XPath xpath, String name) throws XPathExpressionException {
		XPathExpression expr = xpath.compile("//"+name+"/text()");
        String result = (String) expr.evaluate(doc, XPathConstants.STRING);
		return result;
	}
	
	private boolean getBool(Document doc, XPath xpath, String name) throws XPathExpressionException {
		XPathExpression expr = xpath.compile("//"+name+"/text()");
		boolean bool = false;
        String result = (String) expr.evaluate(doc, XPathConstants.STRING);
        if(result.equals("ON"))
        	bool = true;
        else if(result.equals("OFF"))
        	bool = false;
		return bool;
	}
	
	private double getNumber(Document doc, XPath xpath, String name) throws XPathExpressionException {
		XPathExpression expr = xpath.compile("//"+name+"/text()");
        double result = (double) expr.evaluate(doc, XPathConstants.NUMBER);
		return result;
	}
	
	private List<String> getList(Document doc, XPath xpath, String name) throws XPathExpressionException {
	XPathExpression expr = xpath.compile("//"+name+"/text()");
	List<String> list = new ArrayList<>();
    NodeList resultNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    for(int i = 0; i < resultNodes.getLength(); i++)
    	list.add(resultNodes.item(i).getNodeValue());
    return list;   
}
	
}