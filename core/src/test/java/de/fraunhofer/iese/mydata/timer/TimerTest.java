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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import jakarta.validation.groups.Default;
import org.junit.jupiter.api.Test;

class TimerTest {

  private final String timerXML = " <timer  xmlns='http://www.mydata-control.de/4.0/mydataLanguageTimer'"
      + "  xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguageTimer'"
      + "   xmlns:parameter='http://www.mydata-control.de/4.0/parameter'"
      + "  xmlns:pip='http://www.mydata-control.de/4.0/pip'"
      + "  xmlns:event='http://www.mydata-control.de/4.0/event' "
      + "  xmlns:constant='http://www.mydata-control.de/4.0/constant' "
      + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
      + "   cron='0/5 * * * * *' id='urn:timer:cs4:log' description='Its my description.'>"
      + "  </timer>";

  private final String validTimer = " <timer  xmlns='http://www.mydata-control.de/4.0/mydataLanguageTimer'"
      + "  xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguageTimer'"
      + "   xmlns:parameter='http://www.mydata-control.de/4.0/parameter'"
      + "  xmlns:pip='http://www.mydata-control.de/4.0/pip'"
      + "  xmlns:event='http://www.mydata-control.de/4.0/event' "
      + "  xmlns:constant='http://www.mydata-control.de/4.0/constant' "
      + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
      + "   cron='0/5 * * * * *' id='urn:timer:cs4:log' description='Its my description.'>"
      + "   <event action='urn:action:cs4:writeData'>"
      + "  <parameter:string name='blubb' value='xc'/>"
      + "  <parameter:number name='blubb' value='1'/>"
      + "  <parameter:boolean name='blubb' value='true'/>"
      + "  <parameter:object name='blubb' value='xc'/> </event> </timer>";

  private final String validTimer2 = " <timer  xmlns='http://www.mydata-control.de/4.0/mydataLanguageTimer'"
      + "  xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguageTimer'"
      + "   xmlns:parameter='http://www.mydata-control.de/4.0/parameter'"
      + "  xmlns:pip='http://www.mydata-control.de/4.0/pip'"
      + "  xmlns:event='http://www.mydata-control.de/4.0/event' "
      + "  xmlns:constant='http://www.mydata-control.de/4.0/constant' "
      + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
      + "   cron='0/5 * * * * *' id='urn:timer:cs4:othertimer' description='Its my description.'>"
      + "   <event action='urn:action:cs4:writeData'>"
      + "  <parameter:object name='blubb' value='xc'/> </event> </timer>";

  @Test
  void invalid() throws Exception {
    final Timer t = new Timer("not a valid timer content");
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(t));
  }

  @Test
  void invalidToDeploy() throws Exception {
    final Timer t = new Timer(this.timerXML);
    assertEquals(this.timerXML, t.getContent());
    assertTrue(t.isScopeValid());
    assertFalse(t.isXmlValid());
    Class x = TimerDeployableGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(t, x, x1));
  }

  @Test
  void validTimer() throws Exception {
    final Timer t = new Timer(this.validTimer);
    MyDataEntity.validateAndNullCheck(t);
    assertEquals(this.validTimer, t.getContent());
    assertEquals("urn:timer:cs4:log", t.getTimerId().getUrn());
    assertTrue(t.isScopeValid());
    assertTrue(t.isXmlValid());
    assertFalse(t.isDeployed());
    assertTrue(t.isLanguageValid());
  }

  @Test
  void deployValidTimer() {
    final Timer t = new Timer(this.validTimer);
    t.setDeployed(true);
    assertEquals(this.validTimer, t.getContent());
    assertTrue(t.isScopeValid());
    assertTrue(t.isXmlValid());
    assertTrue(t.isDeployed());
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final Timer t1 = new Timer(this.validTimer);
    final Timer t2 = new Timer(this.validTimer);

    assertEquals(t1, t2);
    assertEquals(t1.hashCode(), t2.hashCode());
  }

  @Test
  void whenSameTimerObjectThenEqualsAndHashCodeOk() {
    final Timer t = new Timer(this.validTimer);

    assertEquals(t, t);
    assertEquals(t.hashCode(), t.hashCode());
  }

  @Test
  void whenNotATimerObjectThenEqualsAndHashCodeFail() {
    final Timer timer = new Timer(this.validTimer);
    final TimerId timerId = timer.getTimerId();

    assertNotEquals(timer, timerId);
    assertNotEquals(timer.hashCode(), timerId.hashCode());
  }

  @Test
  void whenNotEqualsThenEqualsAndHashCodeFail() {
    final Timer t1 = new Timer(this.validTimer);
    final Timer t2 = new Timer(this.validTimer2);

    assertNotEquals(t1, t2);
    assertNotEquals(t1.hashCode(), t2.hashCode());
  }

}
