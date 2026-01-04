package com.converter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * FileConverter - Handles local (offline) JSON/XML conversions
 * This class does NOT use any external APIs
 */
public class FileConverter {
    
    private static final int JSON_INDENT_FACTOR = 4;
    
    /**
     * Reads content from a file
     */
    public String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }
    
    /**
     * Writes content to a file
     */
    public void writeFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Converts JSON string to XML string
     */
    public String jsonToXml(String jsonString) throws Exception {
        // Parse JSON
        Object json = parseJson(jsonString);
        
        // Create XML document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        
        // Create root element
        Element rootElement = doc.createElement("root");
        doc.appendChild(rootElement);
        
        // Convert JSON to XML
        if (json instanceof JSONObject) {
            jsonObjectToXml(doc, rootElement, (JSONObject) json);
        } else if (json instanceof JSONArray) {
            jsonArrayToXml(doc, rootElement, (JSONArray) json);
        }
        
        // Convert document to string
        return documentToString(doc);
    }
    
    /**
     * Converts XML string to JSON string
     */
    public String xmlToJson(String xmlString) throws Exception {
        // Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
        
        // Normalize the document
        doc.getDocumentElement().normalize();
        
        // Convert to JSON
        JSONObject json = new JSONObject();
        Element root = doc.getDocumentElement();
        
        // If root has children, convert them
        if (root.hasChildNodes()) {
            NodeList children = root.getChildNodes();
            if (hasOnlyTextContent(root)) {
                // If root only contains text, return simple structure
                json.put(root.getNodeName(), root.getTextContent().trim());
            } else {
                // Convert complex structure
                JSONObject rootObj = xmlNodeToJson(root);
                if (rootObj.length() > 0) {
                    json.put(root.getNodeName(), rootObj);
                } else {
                    json = rootObj;
                }
            }
        }
        
        return json.toString(JSON_INDENT_FACTOR);
    }
    
    /**
     * Parses JSON string (handles both objects and arrays)
     */
    private Object parseJson(String jsonString) throws Exception {
        jsonString = jsonString.trim();
        if (jsonString.startsWith("[")) {
            return new JSONArray(jsonString);
        } else if (jsonString.startsWith("{")) {
            return new JSONObject(jsonString);
        } else {
            throw new Exception("Invalid JSON format. Must start with { or [");
        }
    }
    
    /**
     * Converts JSONObject to XML elements
     */
    private void jsonObjectToXml(Document doc, Element parent, JSONObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            Element element = doc.createElement(sanitizeElementName(key));
            
            if (value instanceof JSONObject) {
                jsonObjectToXml(doc, element, (JSONObject) value);
            } else if (value instanceof JSONArray) {
                jsonArrayToXml(doc, element, (JSONArray) value);
            } else if (value == JSONObject.NULL) {
                element.setAttribute("null", "true");
            } else {
                element.setTextContent(value.toString());
            }
            
            parent.appendChild(element);
        }
    }
    
    /**
     * Converts JSONArray to XML elements
     */
    private void jsonArrayToXml(Document doc, Element parent, JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            Element element = doc.createElement("item");
            
            if (value instanceof JSONObject) {
                jsonObjectToXml(doc, element, (JSONObject) value);
            } else if (value instanceof JSONArray) {
                jsonArrayToXml(doc, element, (JSONArray) value);
            } else if (value == JSONObject.NULL) {
                element.setAttribute("null", "true");
            } else {
                element.setTextContent(value.toString());
            }
            
            parent.appendChild(element);
        }
    }
    
    /**
     * Converts XML node to JSON object
     */
    private JSONObject xmlNodeToJson(Node node) {
        JSONObject json = new JSONObject();
        
        // Get attributes
        if (node.hasAttributes()) {
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                Node attr = node.getAttributes().item(i);
                json.put("@" + attr.getNodeName(), attr.getNodeValue());
            }
        }
        
        // Get child nodes
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            
            // Check if it's simple text content
            if (hasOnlyTextContent(node)) {
                String text = node.getTextContent().trim();
                if (!text.isEmpty()) {
                    return json.length() > 0 ? json.put("value", text) : new JSONObject().put(node.getNodeName(), text);
                }
            } else {
                // Process child elements
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        String childName = child.getNodeName();
                        
                        // Check if this element name already exists (for arrays)
                        if (json.has(childName)) {
                            Object existing = json.get(childName);
                            
                            // Convert to array if not already
                            if (!(existing instanceof JSONArray)) {
                                JSONArray array = new JSONArray();
                                array.put(existing);
                                json.put(childName, array);
                            }
                            
                            // Add new element
                            JSONArray array = json.getJSONArray(childName);
                            if (hasOnlyTextContent(child)) {
                                array.put(child.getTextContent().trim());
                            } else {
                                array.put(xmlNodeToJson(child));
                            }
                        } else {
                            // First occurrence of this element
                            if (hasOnlyTextContent(child)) {
                                json.put(childName, child.getTextContent().trim());
                            } else {
                                json.put(childName, xmlNodeToJson(child));
                            }
                        }
                    }
                }
            }
        }
        
        return json;
    }
    
    /**
     * Checks if a node contains only text content (no child elements)
     */
    private boolean hasOnlyTextContent(Node node) {
        if (!node.hasChildNodes()) {
            return true;
        }
        
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Converts XML Document to formatted string
     */
    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
    
    /**
     * Sanitizes element names to be valid XML
     */
    private String sanitizeElementName(String name) {
        // Replace invalid characters with underscore
        name = name.replaceAll("[^a-zA-Z0-9_-]", "_");
        
        // Ensure it doesn't start with a number
        if (name.matches("^[0-9].*")) {
            name = "_" + name;
        }
        
        return name;
    }
}
