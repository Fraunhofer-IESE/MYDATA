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
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.Timer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class TimerValidator.
 */
public class TimerValidator implements ITimerValidator, ConstraintValidator<MyDataTimer, String> {

  private static final String DEFAULT_SUPPORTED_LANGUAGE_VERSION = "4.0";
  private static final String FILENAME = "application.properties";
  private static final String SUPPORTEDLANGUAGE_VERSION_PROPERTY = "mydata.supportedlanguage.version";

  private static final ITimerValidator TIMER_VALIDATOR_3_2 = new TimerValidator3_2();

  private static final ITimerValidator TIMER_VALIDATOR_4_0 = new TimerValidator4_0();

  private final String supportedLanguageVersion;

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TimerValidator.class);

  public TimerValidator() {
    String supportedLanguageVersionToUse = DEFAULT_SUPPORTED_LANGUAGE_VERSION;
    try (final InputStream input = this.getClass().getClassLoader().getResourceAsStream(FILENAME)) {
      if (null == input) {
        LOG.warn(
            "Could not find application properties file {}, use default supported language version {}",
            FILENAME, DEFAULT_SUPPORTED_LANGUAGE_VERSION);
      } else {
        final Properties prop = new Properties();
        prop.load(input);
        supportedLanguageVersionToUse = prop.getProperty(SUPPORTEDLANGUAGE_VERSION_PROPERTY, DEFAULT_SUPPORTED_LANGUAGE_VERSION);
      }
    } catch (IOException e) {
      LOG.warn("Could not parse the properties file {}, use default supported language version {}",
          FILENAME, DEFAULT_SUPPORTED_LANGUAGE_VERSION, e);
    } finally {
      this.supportedLanguageVersion = supportedLanguageVersionToUse;
    }
  }

  @Override
  public void validateXMLSchema(String timer) throws InvalidEntityException {
    if (timer == null) {
      throw new InvalidEntityException("Timer must not be null");
    }

    if (timer.contains("http://www.iese.fraunhofer.de/ind2uce/3.2.46/ind2uceLanguageTimer")) {
      TIMER_VALIDATOR_3_2.validateXMLSchema(timer);
    } else if (timer.contains("http://www.mydata-control.de/4.0")) {
      TIMER_VALIDATOR_4_0.validateXMLSchema(timer);
    } else {
      throw new InvalidEntityException("Unsupported timer version");
    }
  }

  /**
   * Implementing this method is optional and is usually blank in example code. Use it to setup your
   * constraint validator. In this case, I've created a Pattern object to test the post code.
   *
   * @see jakarta.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
   */
  @Override
  public void initialize(MyDataTimer constraintAnnotation) {
    // usually blank
  }

  /**
   * Use this method to test the constraint.
   *
   * @see jakarta.validation.ConstraintValidator#isValid(java.lang.Object,
   * jakarta.validation.ConstraintValidatorContext)
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      this.validateXMLSchema(value);
      final SolutionId solutionId = SolutionId.fromTimerId(Timer.extractId(value));
      this.validateTimerSolutionAndComponents(solutionId, value);
      // TODO add check for cron
      return true;
    } catch (final InvalidEntityException e) {
      LOG.debug("Timer String is invalid because of: {}", e.getMessage(), e);
      String msg = e.getMessage() != null ? e.getMessage() : "";
      final Throwable cause = e.getCause();
      if (cause != null && cause.getMessage() != null) {
        msg += " - " + cause.getMessage();
      }
      if (StringUtils.isNotBlank(msg)) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
      }
      return false;
    }
  }

  @Override
  public void validateTimerSolutionAndComponents(SolutionId solutionId, @NotNull String timer)
      throws InvalidEntityException {
    if (StringUtils.isBlank(timer)) {
      throw new InvalidEntityException("Timer String must not be blank");
    }

    if (!timer.contains("http://www.iese.fraunhofer.de/ind2uce/" + this.supportedLanguageVersion)
        && !timer.contains("http://www.mydata-control.de/" + this.supportedLanguageVersion)) {
      throw new InvalidEntityException(
          "The language version is outdated, please update your timers first.");
    }
    if (timer.contains("http://www.iese.fraunhofer.de/ind2uce/3.2")) {
      TIMER_VALIDATOR_3_2.validateTimerSolutionAndComponents(solutionId, timer);
    } else if (timer.contains("http://www.mydata-control.de/4.0")) {
      TIMER_VALIDATOR_4_0.validateTimerSolutionAndComponents(solutionId, timer);
    } else { // no supported language version
      throw new InvalidEntityException("Unsupported timer language version");
    }

  }

  public void validateLanguageVersion(Timer timer) throws InvalidEntityException {
    timer.setLanguageValid(true);

    if (!timer.getContent()
        .contains("http://www.iese.fraunhofer.de/ind2uce/" + this.supportedLanguageVersion)
        && !timer.getContent()
        .contains("http://www.mydata-control.de/" + this.supportedLanguageVersion)) {
      timer.setLanguageValid(false);
      throw new InvalidEntityException(
          "The language version is outdated, please update your timers first.");
    }
  }
}
