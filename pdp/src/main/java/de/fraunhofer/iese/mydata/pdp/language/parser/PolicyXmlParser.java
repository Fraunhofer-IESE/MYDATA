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

package de.fraunhofer.iese.mydata.pdp.language.parser;

import de.fraunhofer.iese.mydata.pdp.language.model.Policy;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;

/**
 * XML Parser implementation for our MYDATA schema.
 */
class PolicyXmlParser implements IPolicyParser {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PolicyXmlParser.class);

  private final Unmarshaller jaxbUnmarshaller;

  // newInstance vs. newFactory: https://github.com/dita-ot/dita-ot/issues/3272
  // conflict with Java 8
  final XMLInputFactory xif = XMLInputFactory.newInstance();

  PolicyXmlParser() {
    JAXBContext jaxbContext = null;
    try {
      jaxbContext = JAXBContext.newInstance(Policy.class);
      this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();

    } catch (final JAXBException e) {
      throw new IllegalStateException(e);
    }

  }

  private String read(InputStream input) throws IOException {
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  private Policy parsePolicyFile(final InputStream document) {
    try {
      return this.parsePolicyText(this.read(document));
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  //  public Policy parsePolicyFile(final Reader document) {
  //    try {
  //
  //      final Policy policy = (Policy)this.jaxbUnmarshaller.unmarshal(document);
  //      this.postProcessPolicy(policy);
  //      return policy;
  //
  //    } catch (final JAXBException e) {
  //      throw new IllegalArgumentException(e);
  //    }
  //  }

  @Override
  public Policy parsePolicyFile(File file) throws IllegalArgumentException {
    try {
      return this.parsePolicyFile(new FileInputStream(file));
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see IPolicyParser# parsePolicyText(java.lang.String)
   */
  @Override
  public Policy parsePolicyText(final String document) {
    if (document == null) {
      throw new IllegalArgumentException("XML document is null.");
    }
    // TODO this is done to translate old policy language policies to new policy
    // language version, should be removed?
    final de.fraunhofer.iese.mydata.policy.Policy corePolicyInstance = new de.fraunhofer.iese.mydata.policy.Policy(
        document);
    try (final StringReader stringReader = new StringReader(corePolicyInstance.getContent())) {
      final Policy policy = (Policy) this.jaxbUnmarshaller.unmarshal(stringReader);
      this.postProcessPolicy(policy);
      return policy;

    } catch (final JAXBException e) {
      throw new IllegalArgumentException(e);
    }

  }

  private void postProcessPolicy(Policy policy) {
    try {
      policy.accept(new VariableReplacementVisitor());
    } catch (final Exception e) {
      if (!e.getCause().toString().contains("ind2uce/3.0.25")) {
        throw e;
      } else {
        LOG.info("old policy, no variables -- to fix, update all policies!");
      }
    }
  }

}
