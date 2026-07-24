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

package de.fraunhofer.iese.mydata.timer;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.validation.MyDataTimer;
import de.fraunhofer.iese.mydata.timer.validation.TimerValidator;
import de.fraunhofer.iese.mydata.util.ClockProvider;
import de.fraunhofer.iese.mydata.util.SecureXmlUtils;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

@Entity
@Getter
@Setter
@Table(indexes = {
    @Index(columnList = "timer_id", unique = true)
})
public class Timer extends MyDataEntity {

  /**
   * The logger.
   */
  private static final Logger log = LoggerFactory.getLogger(Timer.class);

  private static final String VALUE = "value";

  /**
   * URN component_id of timer.
   */
  @NotNull
  @Valid
  @EmbeddedId
  private TimerId timerId;

  /**
   * Cron value for this
   */
  @Column(length = 64, nullable = false)
  @NotBlank // TODO Not really sufficient
  private String cronValue;

  /**
   * The deployed.
   */
  @Column(nullable = false)
  private boolean deployed;

  /**
   * The description.
   */
  //  @Column(length = 512)
  @Lob
  @JdbcTypeCode(Types.LONGVARCHAR)
  private String description;

  /**
   * Timer as XML.
   */
  @NotNull
  @MyDataTimer(groups = TimerDeployableGroup.class)
  @Lob
  @JdbcTypeCode(Types.LONGVARCHAR)
  @Column(length = 5 * 1024 * 1024)
  private String content;

  /**
   * List of events to be executed when timer is executed.
   */
  @Transient
  private Set<Event> events;

  @Transient
  private boolean xmlValid = false;

  @Transient
  private boolean languageValid = false;

  @Transient
  private boolean solutionAndComponentsValid = false;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "solution_id")
  private Solution solution;

  @NotNull
  @Min(1)
  @Column(name = "modification_time")
  private long modificationTime;

  /**
   * default constructor
   */
  public Timer() {
    // needed by JPA
  }

  /**
   * create a new timer based on a xml string
   *
   * @param xml
   */
  public Timer(String xml) {
    super();
    this.setContent(xml);
  }

  public void setContent(String timerXML) {
    this.solutionAndComponentsValid = false;
    this.languageValid = false;
    this.content = this.translateToLatestLanguageVersion(timerXML);
    this.modificationTime = ClockProvider.getClock().getCurrentEpochTime();
    try {
      this.timerId = extractId(timerXML);
    } catch (final IllegalArgumentException e) {
      log.debug("Timer ID cannot be set", e);
    }
    try {
      this.description = extractDescription(timerXML);
    } catch (final IllegalArgumentException e) {
      log.debug("Timer Description cannot be set", e);
    }
    try {
      this.cronValue = extractCron(timerXML);
    } catch (final IllegalArgumentException e) {
      log.debug("Timer Cron cannot be set", e);
    }

    this.doInternalValidation();

  }

  private String translateToLatestLanguageVersion(String input) {
    if (input.contains("http://www.iese.fraunhofer.de/ind2uce/3.2.46")) {
      String output = input.replace("http://www.iese.fraunhofer.de/ind2uce/3.2.46/ind2uceLanguage",
          "http://www.mydata-control.de/4.0/mydataLanguage");
      output = output.replace("http://www.iese.fraunhofer.de/ind2uce/3.2.46",
          "http://www.mydata-control.de/4.0");
      return output;
    }

    return input;
  }

  private void buildFromURN(String urn) {
    this.timerId = new TimerId(urn);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final Timer timer = (Timer) o;

    return this.timerId.equals(timer.timerId);
  }

  /**
   * extracts the timerId from the raw xml
   *
   * @param  timer
   * @return       the TimerId
   */
  public static TimerId extractId(String timer) {
    try {
      return new TimerId(readAttribute(timer, "//timer/@id"));
    } catch (ParserConfigurationException | IllegalArgumentException | SAXException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static String extractDescription(String timer) {
    try {
      return readAttribute(timer, "//timer/@description");
    } catch (ParserConfigurationException | IllegalArgumentException | SAXException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static String extractCron(String timer) {
    try {
      return readAttribute(timer, "//timer/@cron");
    } catch (ParserConfigurationException | IllegalArgumentException | SAXException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Read attribute.
   *
   * @param  xpathString              the xpath string
   * @return                          the string
   * @throws IllegalArgumentException
   */
  private static String readAttribute(String timer, String xpathString)
      throws ParserConfigurationException, SAXException {
    try {
      return SecureXmlUtils.readAttribute(timer, xpathString);
    } catch (final XPathExpressionException | XPathFactoryConfigurationException | IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @SuppressWarnings("javadoc")
  public Set<Event> getEvents() {
    if (this.events == null || this.events.isEmpty()) {
      this.parse();
    }
    return this.events;
  }

  private Collection<Parameter<?>> getParametersByTagName(Node item, String string) {
    final Collection<Parameter<?>> params = new ArrayList<>();
    final NodeList elementsByTagName = ((Element) item).getElementsByTagName("parameter:" + string);

    for (int i = 0; i < elementsByTagName.getLength(); i++) {
      final Node node = elementsByTagName.item(i);
      final String name = node.getAttributes().getNamedItem("name").getNodeValue();
      switch (string) {
        case "string":
          final String stringValue = node.getAttributes().getNamedItem(VALUE).getNodeValue();
          if (null != stringValue) {
            params.add(new Parameter<>(name, stringValue));
          }
          break;
        case "number":
          final Double doubleValue = Double
              .valueOf(node.getAttributes().getNamedItem(VALUE).getNodeValue());
          if (null != doubleValue) {
            params.add(new Parameter<>(name, doubleValue));
          }
          break;
        case "boolean":
          final Boolean booleanValue = Boolean
              .valueOf(node.getAttributes().getNamedItem(VALUE).getNodeValue());
          if (null != booleanValue) {
            params.add(new Parameter<>(name, booleanValue));
          }
          break;
        default:
          log.info(
              "No match found for string [ method: getParametersByTagName | class TimerService]");
      }
    }
    return params;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.timerId);
    return builder.toHashCode();
  }

  public boolean isDeployed() {
    return this.deployed;
  }

  public void setDeployed(boolean deployed) {
    this.deployed = deployed;
  }

  public boolean isScopeValid() {
    return this.solutionAndComponentsValid;
  }

  /**
   * @return the languageValid
   */
  public boolean isLanguageValid() {
    return this.languageValid;
  }

  /**
   * @param languageValid the languageValid to set
   */
  public void setLanguageValid(boolean languageValid) {
    this.languageValid = languageValid;
  }

  /**
   * @param isScopeValid the isScopeValid to set
   */
  public void setScopeValid(boolean isScopeValid) {
    this.solutionAndComponentsValid = isScopeValid;
  }

  /**
   * @return the isXmlValid
   */
  public boolean isXmlValid() {
    return this.xmlValid;
  }

  /**
   * @param xmlValid the isXmlValid to set
   */
  public void setXmlValid(boolean xmlValid) {
    this.xmlValid = xmlValid;
  }

  private void parse() {
    try {
      final DocumentBuilderFactory documentumentBuilderFactory = DocumentBuilderFactory
          .newInstance();
      documentumentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      documentumentBuilderFactory.setNamespaceAware(false);
      final DocumentBuilder documentumentBuilder = documentumentBuilderFactory.newDocumentBuilder();
      final Document document = documentumentBuilder
          .parse(new InputSource(new StringReader(this.content)));
      final XPathFactory xpathFactory = XPathFactory.newInstance();
      final XPath xpath = xpathFactory.newXPath();

      // Fetch metadata and initialize timer.
      this.buildFromURN(
          (String) xpath.compile("//timer/@id").evaluate(document, XPathConstants.STRING));
      this.setCronValue(
          (String) xpath.compile("//timer/@cron").evaluate(document, XPathConstants.STRING));
      this.setDescription(
          (String) xpath.compile("//timer/@description").evaluate(document, XPathConstants.STRING));

      // Fetch event data
      final XPathExpression expr = xpath.compile("//timer/event");
      final NodeList names = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

      this.events = new HashSet<>();

      for (int i = 0; i < names.getLength(); i++) {
        this.events.add(this.parseEvent(names.item(i)));
      }
    } catch (IOException | ParserConfigurationException | SAXException
        | XPathExpressionException e) {
      log.debug("The timer could not be parsed because of {}", e.getMessage(), e);
      throw new IllegalArgumentException("The Timer was not valid");
    }

  }

  private Event parseEvent(Node item) {
    final String action = item.getAttributes().getNamedItem("action").getNodeValue();
    final ParameterList params = new ParameterList();

    params.addAll(this.getParametersByTagName(item, "string"));

    params.addAll(this.getParametersByTagName(item, "boolean"));

    params.addAll(this.getParametersByTagName(item, "number"));

    params.addAll(this.getParametersByTagName(item, "object"));

    params.addAll(this.getParametersByTagName(item, "list"));

    return new Event(new ActionId(action), Instant.now(), params);
  }

  public SolutionId getSolutionId() {
    try {
      return SolutionId.fromTimerId(this.getTimerId());
    } catch (final InvalidEntityException e) {
      return null;
    }
  }

  /**
   * internal timer validation: - validate xml schema - validate language version - validate
   * solution and components
   */
  public void doInternalValidation() {
    this.xmlValid = false;
    this.languageValid = false;
    this.solutionAndComponentsValid = false;
    final TimerValidator tv = new TimerValidator();
    try {
      tv.validateXMLSchema(this.content);
      this.xmlValid = true;
    } catch (InvalidEntityException | IllegalArgumentException e) {
      log.info("Timer is not valid according to the xml schema", e);
    }
    // TODO maybe we can skip the other checks when timer is invalid...
    try {
      tv.validateLanguageVersion(this);
      this.languageValid = true;
    } catch (final InvalidEntityException e) {
      log.info("Timer language version is not valid", e);
    }

    try {
      tv.validateTimerSolutionAndComponents(SolutionId.fromTimerId(this.timerId), this.content);
      this.solutionAndComponentsValid = true;
    } catch (final InvalidEntityException e) {
      log.info(
          "Timer solutions are not valid - do not forget to check the PIP and Events in the timer",
          e);
    }
  }

}
