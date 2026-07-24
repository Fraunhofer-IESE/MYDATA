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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.timer.validation.TimerValidator;

import jakarta.validation.groups.Default;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

class TimerValidatorTest {

  @Test
  void XMLisNull() {
    final TimerValidator pv = new TimerValidator();
    assertThrows(InvalidEntityException.class, () ->
      pv.validateXMLSchema(null));
  }

  @Test
  void XMLisInvalid() {
    final TimerValidator pv = new TimerValidator();
    assertThrows(InvalidEntityException.class, () ->
      pv.validateXMLSchema("http://www.iese.fraunhofer.de/ind2uce/3.0.255/enforcementLanguage"));
  }

  @Test
  void XMLisOld() {
    final TimerValidator pv = new TimerValidator();
    final Timer p = new Timer("http://www.iese.fraunhofer.de/ind2uce/3.0.255/enforcementLanguage");
    try {
      pv.validateLanguageVersion(p);
    } catch (final InvalidEntityException e) {
      assertTrue(e.getMessage().contains("language version is outdated"));
      return;
    }
    fail();
  }

  /**
   * Invalid timer test.
   * 
   * @throws InvalidEntityException
   */
  @Test
  void invalidTimerTest() {
    assertThrows(InvalidEntityException.class, () ->
      new TimerValidator().validateXMLSchema("<timer/>"));
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
    return new String(Files.readAllBytes(Paths
        .get(Objects.requireNonNull(this.getClass().getClassLoader().getResource(file)).toURI())));
  }

  /**
   * Valid timer test.
   * 
   * @throws InvalidEntityException
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void validTimerTest() throws Exception {
    new TimerValidator().validateXMLSchema(this.readResourceFile("timer_valid.xml"));
  }

  @Test
  void invalidTimerWithEventOccurenceEventFromOtherSolution() throws Exception {
    final Timer p = new Timer(this.readResourceFile("timer_with_event_other_solution.xml"));
    assertFalse(p.isSolutionAndComponentsValid());
    Class x = TimerDeployableGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(p, x, x1));
  }

}
