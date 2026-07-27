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

package de.fraunhofer.iese.mydata.policy.validation;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.util.SecureXmlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

/**
 * The Class PolicyValidator.
 * 
 * @deprecated
 */
@Deprecated
class PolicyValidator3_0 implements IPolicyValidator {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PolicyValidator3_0.class);

  /**
   * The Constant SCHEMA_RESOURCE_FILEPATH.
   */
  private static final String SCHEMA_RESOURCE_FILEPATH = "/languageSchema/enfLanguage.xsd";

  /**
   * The validator.
   */
  private final Validator validator;

  @Deprecated
  public PolicyValidator3_0() {
    try {
      final SchemaFactory schemaFactory = SchemaFactory
          .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      final URL url = PolicyValidator3_0.class.getResource(SCHEMA_RESOURCE_FILEPATH);
      final Schema schema = schemaFactory.newSchema(url);

      this.validator = schema.newValidator();
      this.validator.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      this.validator.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
          LOG.debug("Validation error: {}", exception.getMessage());
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          LOG.debug("Validation fatal error: {}", exception.getMessage());
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          LOG.debug("Validation warning: {}", exception.getMessage());
          throw exception;
        }
      });

      LOG.debug("Successfully loaded schema");
    } catch (final SAXException e) {
      throw new RuntimeException("Unable to create schema", e);
    }
  }

  @Deprecated
  @Override
  public void validateXMLSchema(String policyString) throws InvalidEntityException {
    try {
      this.validator.validate(new StreamSource(new StringReader(policyString)));
    } catch (IOException | SAXException e) {
      throw new InvalidEntityException("Policy is not valid according to XML Schema", e);
    }
  }

  @Deprecated
  @Override
  public void validatePolicySolutionAndComponents(SolutionId solutionId, String p)
      throws InvalidEntityException {

    try {
      final Document document = SecureXmlUtils.parseXml(p);

      final XPath xpath = SecureXmlUtils.createSecureXPath();

      final Map<String, String> prefMap = new HashMap<>();

      prefMap.put("tns", "http://www.iese.fraunhofer.de/ind2uce/3.0.25/enforcementLanguage");
      prefMap.put("pip", "http://www.iese.fraunhofer.de/ind2uce/3.0.25/pip");
      prefMap.put("param", "http://www.iese.fraunhofer.de/ind2uce/3.0.25/param");
      prefMap.put("event", "http://www.iese.fraunhofer.de/ind2uce/3.0.25/event");
      prefMap.put("constant", "http://www.iese.fraunhofer.de/ind2uce/3.0.25/constant");
      prefMap.put("time", "http://www.iese.fraunhofer.de/ind2uce/3.0.25/time");

      final SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
      xpath.setNamespaceContext(namespaces);

      // check PEP events
      XPathExpression expr = xpath.compile(".//tns:event");
      NodeList names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

      final List<String> errors = new ArrayList<>();

      for (int i = 0; i < names.getLength(); i++) {
        final Node n = names.item(i);
        final String actionName = n.getAttributes().getNamedItem("action").getNodeValue();
        final String v = actionName.split(":")[2];
        if (!v.equals(solutionId.getIdentifier())) {
          errors.add("Event "
              + actionName
              + " does not refer to an action of solution "
              + solutionId
              + ".");
        }
      }

      // check PIP methods
      expr = xpath.compile(
          ".//pip:string|.//pip:boolean|.//pip:int|.//pip:float|.//pip:double|.//pip:object|.//pip:list");
      names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
      for (int i = 0; i < names.getLength(); i++) {
        final Node n = names.item(i);
        final String actionName = n.getAttributes().getNamedItem("method").getNodeValue();
        final String v = actionName.split(":")[2];
        if (!v.equals(solutionId.getIdentifier())) {
          errors.add("ExecuteAction "
              + actionName
              + " does not refer to a PXP of solution "
              + solutionId
              + ".");
        }
      }

      // check PXP methods
      expr = xpath.compile(".//tns:executeAction");
      names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
      for (int i = 0; i < names.getLength(); i++) {
        final Node n = names.item(i);
        final String methodName = n.getAttributes().getNamedItem("name").getNodeValue();
        final String v = methodName.split(":")[2];
        if (!v.equals(solutionId.getIdentifier())) {
          errors.add("PIP " + methodName + " does not belong to solution " + solutionId + ".");
        }
      }

      if (!errors.isEmpty()) {
        final StringBuilder b = new StringBuilder();
        b.append("Policy is invalid due to the following errors:\n");
        for (final String error : errors) {
          b.append(error);
          b.append("\n");
        }
        throw new InvalidEntityException(b.toString());
      }

    } catch (IOException | SAXException | XPathExpressionException | ParserConfigurationException
        | XPathFactoryConfigurationException e) {
      throw new InvalidEntityException("Policy does not have attribute", e);
    }
  }

}
