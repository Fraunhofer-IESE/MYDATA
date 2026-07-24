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

package de.fraunhofer.iese.mydata.policy;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.validation.MyDataPolicy;
import de.fraunhofer.iese.mydata.policy.validation.PolicyValidator;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.solution.SolutionId;
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
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Types;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

/**
 * The Class Policy.
 */
@Entity
@Getter
@Setter
@Table(indexes = {
    @Index(columnList = "policy_id", unique = true)
})
public class Policy extends MyDataEntity {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Policy.class);

  /**
   * The component_id.
   */
  @NotNull
  @Valid
  @EmbeddedId
  private PolicyId policyId;

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

  private boolean deployed;

  @Lob
  @JdbcTypeCode(Types.LONGVARCHAR)
  private String description;

  /**
   * The raw content of the policy.
   */
  @Lob
  @JdbcTypeCode(Types.LONGVARCHAR)
  @Column(length = 5 * 1024 * 1024)
  @MyDataPolicy(groups = PolicyDeployableGroup.class)
  private String content;

  private Long deploymentDate;

  @NotNull
  @Min(1)
  @Column(name = "modification_time")
  private long modificationTime;

  /**
   * Required for JPA.
   */
  public Policy() {
    // required by JPA
  }

  /**
   * Instantiates a new policy.
   *
   * @param content the policy
   */
  public Policy(String content) {
    super();
    this.setContent(content);
  }

  public static PolicyId extractId(String policy) {
    try {
      return new PolicyId(readAttribute(policy, "//policy/@id"));
    } catch (ParserConfigurationException | IllegalArgumentException | SAXException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static String extractDescription(String policy) {
    try {
      return readAttribute(policy, "//policy/@description");
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
  private static String readAttribute(String policy, String xpathString)
      throws ParserConfigurationException, SAXException {
    try {
      return SecureXmlUtils.readAttribute(policy, xpathString);
    } catch (final XPathExpressionException | XPathFactoryConfigurationException | IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
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

  public boolean isSolutionAndComponentsValid() {
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

  public boolean isXmlValid() {
    return this.xmlValid;
  }

  /**
   * Gets the policy.
   *
   * @return the policy
   */
  public String getContent() {
    return this.translateToLatestLanguageVersion(this.content);
  }

  /**
   * @param p the MYDATA Policy as String
   */
  public void setContent(String p) {
    this.solutionAndComponentsValid = false;
    this.languageValid = false;
    this.content = this.translateToLatestLanguageVersion(p);
    this.modificationTime = ClockProvider.getClock().getCurrentEpochTime();

    try {
      this.policyId = extractId(this.content);
    } catch (final IllegalArgumentException e) {
      LOG.debug("Policy ID cannot be set", e);
    }
    try {
      this.description = extractDescription(this.content);
    } catch (final IllegalArgumentException e) {
      LOG.debug("Policy Description cannot be set", e);
    }

    this.doInternalValidation();
  }

  /**
   * Typically used when loading a policy from the db, it checks if it's valid or not
   */
  public void doInternalValidation() {
    this.xmlValid = false;
    this.languageValid = false;
    this.solutionAndComponentsValid = false;
    final PolicyValidator pv = new PolicyValidator();
    try {
      pv.validateXMLSchema(this.content);
      this.xmlValid = true;
    } catch (InvalidEntityException | IllegalArgumentException e) {
      LOG.info("Policy is not valid according to the xml schema", e);
    }
    // TODO maybe we can skip the other checks when policy is invalid...
    try {
      pv.validateLanguageVersion(this);
      this.languageValid = true;
    } catch (final InvalidEntityException e) {
      LOG.info("Policy language version is not valid", e);
    }

    try {
      pv.validatePolicySolutionAndComponents(SolutionId.fromPolicyId(this.policyId), this.content);
      this.solutionAndComponentsValid = true;
    } catch (final InvalidEntityException e) {
      LOG.info(
          "Policy solutions are not valid - do not forget to check the PIP and Events in the policy",
          e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Policy)) {
      return false;
    }

    final Policy that = (Policy) o;

    return new EqualsBuilder().append(this.getPolicyId(), that.getPolicyId()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getPolicyId()).toHashCode();
  }
}
