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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.validation.PolicyValidator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

/**
 * The Class PolicySchemaTest.
 */
public class PolicySchemaTest {
  public String name;
  public boolean expectedResult;
  public String expectedErrorMessage;

  /**
   * Required because this test-class expects error-texts in englisch.
   */
  @BeforeAll
  static void forceLanguageEN() {
    Locale.setDefault(Locale.ENGLISH);
  }

  /**
   * Parameter basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   * @throws IOException                    Signals that an I/O exception has occurred.
   * @throws URISyntaxException             the URI syntax exception
   * @throws IllegalArgumentException
   */
  @MethodSource("data")
  @ParameterizedTest(name = "{0} Test expects {1}")
  public void parameterBasicTests(String name, boolean expectedResult, String expectedErrorMessage) throws Exception {
    initPolicySchemaTest(name, expectedResult, expectedErrorMessage);
    try {
      new PolicyValidator().validateXMLSchema(this.readResourceFile(this.name + ".xml"));
    } catch (final InvalidEntityException e) {
      if (this.expectedResult) {
        fail();
      }

      if (this.expectedErrorMessage != null) {
        assertTrue(e.getCause().getMessage().contains(this.expectedErrorMessage));
      }
    }
  }

  /**
   * Read resource file.
   *
   * @param  file               the file
   * @return                    the string
   * @throws IOException        Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }

  /**
   * Data.
   *
   * @return the collection
   */
  public static Collection<Object[]> data() {

    return Arrays.asList(new Object[][] {
        {
            "whenLargeValidPolicy_thenTrue", true, null
        }, {
            "whenInvalidFloatValue_thenSAXParseException", false,
            "'bla' is not a valid value for 'float'"
        }, {
            "whenNoMechanism_thenSAXParseException", false,
            "The content of element 'policy' is not complete"
        }, {
            "whenSmallestPreventive_thenTrue", true, null
        }, {
            "whenSmallestDetective_thenTrue", true, null
        }, {
            "whenValid46Policy_thenTrue", true, null
        },
    });
  }

  public void initPolicySchemaTest(String name, boolean expectedResult, String expectedErrorMessage) {
    this.name = name;
    this.expectedResult = expectedResult;
    this.expectedErrorMessage = expectedErrorMessage;
  }

}
