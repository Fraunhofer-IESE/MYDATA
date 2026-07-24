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
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class PolicyValidator.
 */
public class PolicyValidator
    implements IPolicyValidator, ConstraintValidator<MyDataPolicy, String> {

  private static final String SUPPORTED_LANGUAGE_VERSION = "4.0";

  @SuppressWarnings("deprecation")
  private static final IPolicyValidator POLICY_VALIDATOR_3_0 = new PolicyValidator3_0();

  private static final IPolicyValidator POLICY_VALIDATOR_3_2 = new PolicyValidator3_2();

  private static final IPolicyValidator POLICY_VALIDATOR_4_0 = new PolicyValidator4_0();

  private String supportedLanguageVersion = SUPPORTED_LANGUAGE_VERSION;

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PolicyValidator.class);

  public PolicyValidator() {
    final Properties prop = new Properties();
    InputStream input = null;

    final String filename = "application.properties";
    input = this.getClass().getClassLoader().getResourceAsStream(filename);
    if (input == null) {
      LOG.warn("Could not parse the properties file {}, use default supported language version {}",
          filename, SUPPORTED_LANGUAGE_VERSION);
      return;
    }

    // load a properties file from class path, inside static method
    try {
      prop.load(input);
    } catch (final IOException e) {
      LOG.warn("Could not parse the properties file {}, use default supported language version {}",
          filename, SUPPORTED_LANGUAGE_VERSION, e);
    }
    this.supportedLanguageVersion = prop.getProperty("mydata.supportedlanguage.version",
        SUPPORTED_LANGUAGE_VERSION);

  }

  @Override
  public void validateXMLSchema(String policy) throws InvalidEntityException {
    if (StringUtils.isBlank(policy)) {
      throw new InvalidEntityException("Policy String must not be blank");
    }

    if (policy.contains("http://www.iese.fraunhofer.de/ind2uce/3.0")) {
      POLICY_VALIDATOR_3_0.validateXMLSchema(policy);
    } else if (policy.contains("http://www.iese.fraunhofer.de/ind2uce/3.2")) {
      POLICY_VALIDATOR_3_2.validateXMLSchema(policy);
    } else if (policy.contains("http://www.mydata-control.de/4.0")) {
      POLICY_VALIDATOR_4_0.validateXMLSchema(policy);
    } else { // no supported policy version
      throw new InvalidEntityException("Unsupported policy version");
    }

  }

  @Override
  public void validatePolicySolutionAndComponents(SolutionId solutionId, String policy)
      throws InvalidEntityException {
    if (StringUtils.isBlank(policy)) {
      throw new InvalidEntityException("Policy String must not be blank");
    }

    if (!policy.contains("http://www.iese.fraunhofer.de/ind2uce/" + this.supportedLanguageVersion)
        && !policy.contains("http://www.mydata-control.de/" + this.supportedLanguageVersion)) {
      throw new InvalidEntityException(
          "The language version is outdated, please update your policies first.");
    }
    if (policy.contains("http://www.iese.fraunhofer.de/ind2uce/3.0.25/enforcementLanguage")) {
      POLICY_VALIDATOR_3_0.validatePolicySolutionAndComponents(solutionId, policy);
    } else if (policy.contains("http://www.iese.fraunhofer.de/ind2uce/3.2")) {
      POLICY_VALIDATOR_3_2.validatePolicySolutionAndComponents(solutionId, policy);
    } else if (policy.contains("http://www.mydata-control.de/4.0")) {
      POLICY_VALIDATOR_4_0.validatePolicySolutionAndComponents(solutionId, policy);
    } else { // no supported policy version
      throw new InvalidEntityException("Unsupported policy version");
    }
  }

  public void validateLanguageVersion(Policy policy) throws InvalidEntityException {
    policy.setLanguageValid(true);

    if (!policy.getContent()
        .contains("http://www.iese.fraunhofer.de/ind2uce/" + this.supportedLanguageVersion)
        && !policy.getContent()
            .contains("http://www.mydata-control.de/" + this.supportedLanguageVersion)) {
      policy.setLanguageValid(false);
      throw new InvalidEntityException(
          "The language version is outdated, please update your policies first.");
    }
  }

  public void validateLanguageVersion(String policyString) throws InvalidEntityException {
    if (!policyString
        .contains("http://www.iese.fraunhofer.de/ind2uce/" + this.supportedLanguageVersion)
        && !policyString
            .contains("http://www.mydata-control.de/" + this.supportedLanguageVersion)) {
      throw new InvalidEntityException(
          "The language version is outdated, please update your policies first.");
    }
  }

  /**
   * Implementing this method is optional and is usually blank in example code. Use it to setup your
   * constraint validator. In this case, I've created a Pattern object to test the post code.
   *
   * @see jakarta.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
   */
  @Override
  public void initialize(MyDataPolicy constraintAnnotation) {

  }

  /**
   * Use this method to test the constraint.
   *
   * @see jakarta.validation.ConstraintValidator#isValid(java.lang.Object,
   *      jakarta.validation.ConstraintValidatorContext)
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      this.validateXMLSchema(value);
      final SolutionId solutionId = SolutionId.fromPolicyId(Policy.extractId(value));
      this.validatePolicySolutionAndComponents(solutionId, value);
      this.validateLanguageVersion(value);
      return true;
    } catch (final InvalidEntityException e) {
      LOG.debug("Policy String is invalid because of: {}", e.getMessage(), e);
      return false;
    }
  }

}
