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

package de.fraunhofer.iese.mydata.timer.validation;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.validation.SimpleNamespaceContext;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.util.SecureXmlUtils;

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
 * The Class TimerValidator.
 */
class TimerValidator3_2 implements ITimerValidator {

  /**
   * The Constant LOG.
   */
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TimerValidator3_2.class);

  /**
   * The Constant SCHEMA_RESOURCE_FILEPATH.
   */
  private static final String SCHEMA_RESOURCE_FILEPATH = "/languageSchema3_2/ind2uceLanguageTimer.xsd";

  /**
   * The schema.
   */
  private Schema schema;

  /**
   * The validator.
   */
  private Validator validator;

  public TimerValidator3_2() {
    try {
      final SchemaFactory schemaFactory = SchemaFactory
          .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      final URL url = TimerValidator3_2.class.getResource(SCHEMA_RESOURCE_FILEPATH);
      this.schema = schemaFactory.newSchema(url);

      this.validator = this.schema.newValidator();
      this.validator.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      this.validator.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
          LOG.error("Validation error: {}", exception.getMessage());
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          LOG.error("Validation fatal error: {}", exception.getMessage());
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          LOG.error("Validation warning: {}", exception.getMessage());
          throw exception;
        }
      });

      LOG.info("Successfully loaded schema");
    } catch (final SAXException e) {
      LOG.error("Unable to create schema", e);
    }
  }

  @Override
  public void validateXMLSchema(String timerXML) {
    try {
      this.validator.validate(new StreamSource(new StringReader(timerXML)));
    } catch (IOException | SAXException e) {
      LOG.info("Exception: {}", e.getMessage());
      throw new IllegalArgumentException("Timer is not valid according to XML Schema", e);
    }
  }

  @Override
  public void validateTimerSolutionAndComponents(SolutionId solutionId, String timer)
      throws InvalidEntityException {

    try {

      final Document document = SecureXmlUtils.parseXml(timer);
      final XPath xpath = SecureXmlUtils.createSecureXPath();

      final Map<String, String> prefMap = new HashMap<>();
      prefMap.put("tns", "http://www.iese.fraunhofer.de/ind2uce/3.2.46/ind2uceLanguageTimer");
      prefMap.put("pip", "http://www.iese.fraunhofer.de/ind2uce/3.2.46/pip");
      prefMap.put("parameter", "http://www.iese.fraunhofer.de/ind2uce/3.2.46/parameter");
      prefMap.put("event", "http://www.iese.fraunhofer.de/ind2uce/3.2.46/event");
      prefMap.put("constant", "http://www.iese.fraunhofer.de/ind2uce/3.2.46/constant");

      final SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
      xpath.setNamespaceContext(namespaces);

      // check PEP events
      final XPathExpression expr = xpath.compile(".//tns:mechanism");
      final NodeList names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

      final List<String> errors = new ArrayList<>();
      this.checkEvent(solutionId, names, errors);
      this.checkMethod(solutionId, document, xpath, errors);
      this.checkExecute(solutionId, document, xpath, errors);

      if (!errors.isEmpty()) {
        final StringBuilder b = new StringBuilder();
        b.append("Timer is invalid due to the following errors:\n");
        for (final String error : errors) {
          b.append(error);
          b.append("\n");
        }
        throw new IllegalArgumentException(b.toString());
      }

    } catch (IOException | SAXException | XPathExpressionException | ParserConfigurationException
        | XPathFactoryConfigurationException e) {
      throw new IllegalArgumentException("Timer validation failed", e);
    }

  }

  void checkExecute(SolutionId solutionId, Document document, XPath xpath, List<String> errors)
      throws XPathExpressionException, InvalidEntityException {
    XPathExpression expr;
    NodeList names;
    // check PXP methods
    expr = xpath.compile(".//tns:execute");
    names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
    for (int i = 0; i < names.getLength(); i++) {
      final Node n = names.item(i);
      final String actionName = n.getAttributes().getNamedItem("action").getNodeValue();
      final ActionId actionId = new ActionId(actionName);
      final SolutionId actionSolutionId = SolutionId.fromActionId(actionId);
      if (!actionSolutionId.equals(solutionId)) {
        errors.add("ExecuteAction "
            + actionName
            + " does not refer to a PXP of solution "
            + solutionId
            + ".");
      }
    }
  }

  void checkMethod(final SolutionId solutionId, final Document document, final XPath xpath,
      List<String> errors) throws XPathExpressionException, InvalidEntityException {
    XPathExpression expr;
    NodeList names;
    // check PIP methods
    expr = xpath.compile(".//pip:string|.//pip:boolean|.//pip:number|.//pip:object|.//pip:list");
    names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
    for (int i = 0; i < names.getLength(); i++) {
      final Node n = names.item(i);
      final String actionName = n.getAttributes().getNamedItem("method").getNodeValue();
      final ActionId actionId = new ActionId(actionName);
      final SolutionId actionSolutionId = SolutionId.fromActionId(actionId);
      if (!actionSolutionId.equals(solutionId)) {
        errors.add("PIP " + actionName + " does not belong to solution " + solutionId + ".");
      }
    }
  }

  void checkEvent(final SolutionId solutionId, NodeList names, final List<String> errors)
      throws InvalidEntityException {
    for (int i = 0; i < names.getLength(); i++) {
      final Node n = names.item(i);
      final String actionName = n.getAttributes().getNamedItem("event").getNodeValue();
      final ActionId actionId = new ActionId(actionName);
      final SolutionId actionSolutionId = SolutionId.fromActionId(actionId);
      if (!actionSolutionId.equals(solutionId)) {
        errors.add(
            "Event " + actionName + " does not refer to an action of solution " + solutionId + ".");
      }
    }
  }

}
