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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * The Class TimerValidator.
 */
class TimerValidator4_0 implements ITimerValidator {

  /**
   * The Constant LOG.
   */
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TimerValidator4_0.class);

  /**
   * The Constant SCHEMA_RESOURCE_FILEPATH.
   */
  private static final String SCHEMA_RESOURCE_FILEPATH = "/languageSchema4_0/mydataLanguageTimer.xsd";

  /**
   * The validator.
   */
  private final Validator validator;

  public TimerValidator4_0() {
    try {
      final SchemaFactory schemaFactory = SchemaFactory
          .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      final URL url = TimerValidator4_0.class.getResource(SCHEMA_RESOURCE_FILEPATH);
      final Schema schema = schemaFactory.newSchema(url);

      this.validator = schema.newValidator();
      this.validator.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      this.validator.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
          LOG.debug("Validation error: {}", exception.getMessage(), exception);
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          LOG.debug("Validation fatal error: {}", exception.getMessage(), exception);
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          LOG.debug("Validation warning: {}", exception.getMessage(), exception);
          throw exception;
        }
      });

      LOG.info("Successfully loaded schema");
    } catch (final SAXException e) {
      throw new RuntimeException("Unable to create schema", e);
    }
  }

  @Override
  public void validateXMLSchema(String timerXML) throws InvalidEntityException {
    try {
      this.validator.validate(new StreamSource(new StringReader(timerXML)));
    } catch (final Exception e) {
      throw new InvalidEntityException("Timer is not valid according to XML Schema", e);
    }
  }

  private Document getDocument(String p)
      throws SAXException, IOException, ParserConfigurationException {
    final Document doc = SecureXmlUtils.parseXml(p);
    doc.getDocumentElement().normalize();
    return doc;
  }

  @Override
  public void validateTimerSolutionAndComponents(SolutionId solutionId, String p)
      throws InvalidEntityException {

    try {
      final Document document = this.getDocument(p);
      final List<String> errors = new ArrayList<>();

      this.checkEvents(solutionId, document, errors);

      this.checkPip(solutionId, document, errors);

      this.checkPxp(solutionId, document, errors);

      if (!errors.isEmpty()) {
        final StringBuilder b = new StringBuilder();
        b.append("Policy is invalid due to the following errors:\n");
        for (final String error : errors) {
          b.append(error);
          b.append("\n");
        }
        throw new InvalidEntityException(b.toString());
      }

    } catch (IOException | SAXException | ParserConfigurationException e) {
      throw new InvalidEntityException("Policy validation failed", e);
    }

  }

  private void checkPxp(final SolutionId solutionId, final Document doc, final List<String> errors)
      throws InvalidEntityException {
    NodeList names;
    // check PXP methods
    names = doc.getElementsByTagName("execute");
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

  private void checkPip(final SolutionId solutionId, final Document doc, final List<String> errors)
      throws InvalidEntityException {
    final List<String> pipElements = Arrays.asList("pip:string", "pip:boolean", "pip:number",
        "pip:object", "pip:list");

    NodeList names;
    // check PIP methods
    for (final String pipElt : pipElements) {

      names = doc.getElementsByTagName(pipElt);
      for (int i = 0; i < names.getLength(); i++) {
        final Node n = names.item(i);
        final String actionName = n.getAttributes().getNamedItem("method").getNodeValue();
        final InfoId iid = new InfoId(actionName);
        MyDataEntity.validate(iid);
        final SolutionId actionSolutionId = SolutionId.fromInfoId(iid);
        if (!actionSolutionId.equals(solutionId)) {
          errors.add("PIP " + actionName + " does not belong to solution " + solutionId + ".");
        }
      }
    }
  }

  private void checkEvents(final SolutionId solutionId, Document doc, final List<String> errors)
      throws InvalidEntityException {
    final List<String> eventElements = Collections.singletonList("event");

    NodeList names;
    for (final String evtElt : eventElements) {
      names = doc.getElementsByTagName(evtElt);
      for (int i = 0; i < names.getLength(); i++) {
        final Node n = names.item(i);
        final String actionName = n.getAttributes().getNamedItem("action").getNodeValue();
        final ActionId actionId = new ActionId(actionName);
        final SolutionId actionSolutionId = SolutionId.fromActionId(actionId);
        if (!actionSolutionId.equals(solutionId)) {
          errors.add("Event with action "
              + actionName
              + " does not refer to an action of solution "
              + solutionId
              + ".");
        }
      }
    }
  }

}
