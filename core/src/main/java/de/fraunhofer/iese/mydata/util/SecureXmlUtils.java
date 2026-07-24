/*
 * =================================LICENSE_START=================================
 * MYDATA Control Technologies
 *
 * Copyright (C) 2016 - present Fraunhofer-Gesellschaft zur Foerderung der
 * angewandten Forschung e.V. acting on behalf of its Fraunhofer Institute
 * for Experimental Software Engineering (IESE)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =================================LICENSE_END===================================
 */

package de.fraunhofer.iese.mydata.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

public final class SecureXmlUtils {

  private SecureXmlUtils() {
    // utility class
  }

  public static DocumentBuilderFactory createSecureDocumentBuilderFactory()
      throws ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setNamespaceAware(false);
    return factory;
  }

  public static DocumentBuilder createSecureDocumentBuilder() throws ParserConfigurationException {
    return createSecureDocumentBuilderFactory().newDocumentBuilder();
  }

  public static Document parseXml(String xml)
      throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilder builder = createSecureDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml)));
  }

  public static String evaluateXPathAsString(Document document, String xpathExpression)
      throws XPathExpressionException, XPathFactoryConfigurationException {
    final XPathFactory xpathFactory = XPathFactory.newInstance();
    xpathFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    final XPath xpath = xpathFactory.newXPath();
    return (String) xpath.compile(xpathExpression).evaluate(document, XPathConstants.STRING);
  }

  public static String readAttribute(String xml, String xpathExpression)
      throws ParserConfigurationException, SAXException, IOException, XPathExpressionException,
      XPathFactoryConfigurationException {
    final Document document = parseXml(xml);
    return evaluateXPathAsString(document, xpathExpression);
  }

  public static XPathFactory createSecureXPathFactory() throws XPathFactoryConfigurationException {
    final XPathFactory factory = XPathFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    return factory;
  }

  public static XPath createSecureXPath() throws XPathFactoryConfigurationException {
    return createSecureXPathFactory().newXPath();
  }

  public static XPath createSecureXPath(NamespaceContext namespaceContext)
      throws XPathFactoryConfigurationException {
    final XPath xpath = createSecureXPath();
    xpath.setNamespaceContext(namespaceContext);
    return xpath;
  }

  public static NodeList evaluateNodeList(XPath xpath, Object item, String expression)
      throws XPathExpressionException {
    return (NodeList) xpath.compile(expression).evaluate(item, XPathConstants.NODESET);
  }

  public static TransformerFactory createSecureTransformerFactory()
      throws TransformerConfigurationException {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

    // Prevent XXE-style resolution in XSLT/transformer context
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    return factory;
  }

  public static Transformer createSecureTransformer() throws TransformerConfigurationException {
    return createSecureTransformerFactory().newTransformer();
  }

  public static String serializeNode(Node node) throws TransformerException {
    final StringWriter outText = new StringWriter();
    final StreamResult result = new StreamResult(outText);

    final Transformer transformer = createSecureTransformer();
    final Properties outputProps = new Properties();
    outputProps.put(OutputKeys.METHOD, "xml");
    transformer.setOutputProperties(outputProps);
    transformer.transform(new DOMSource(node), result);

    return outText.toString();
  }

}
