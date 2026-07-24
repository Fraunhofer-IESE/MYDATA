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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;

import org.junit.jupiter.api.Test;

class PipRequestTest {

  @Test
  void validPipRequest() throws Exception {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    MyDataEntity.validateAndNullCheck(pipRequest);
    // everything should be fine
  }

  @Test
  void invalidPipRequestCausedByInvalidId() throws Exception {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:mir-egal"));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(pipRequest));
  }

  @Test
  void invalidPipRequestCausedByInvalidParameter() throws Exception {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"),
          new Parameter<>("", false));
    assertThrows(InvalidEntityException.class, () ->
      MyDataEntity.validateAndNullCheck(pipRequest));
  }

  @Test
  void addParamater() {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> parameter = new Parameter<>("StringParameter", "teststring");
    pipRequest.addParameter(parameter);

    assertTrue(pipRequest.getParameters().contains(parameter));
  }

  @Test
  void addParameterAsKeyValue() {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> parameter = new Parameter<>("StringParameter", "teststring");
    pipRequest.addParameter("StringParameter", "teststring");

    assertTrue(pipRequest.getParameters().contains(parameter));
  }

  @Test
  void clearParameters() {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> parameter = new Parameter<>("StringParameter", "teststring");
    pipRequest.addParameter(parameter);

    pipRequest.clearParameters();

    assertTrue(pipRequest.getParameters().isEmpty());
  }

  @Test
  void getParameterForName() {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> parameter = new Parameter<>("StringParameter", "teststring");
    pipRequest.addParameter(parameter);

    assertEquals(parameter, pipRequest.getParameterForName(parameter.getName()));
  }

  @Test
  void setParameters() {
    final Parameter<String> param1 = new Parameter<>("StringParameter1", "teststring");
    final Parameter<String> param2 = new Parameter<>("StringParameter2", "teststring1234");
    final ParameterList parameterList = new ParameterList(param1, param2);

    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    pipRequest.setParameters(parameterList);

    assertEquals(parameterList, pipRequest.getParameters());
  }

  @Test
  void getParameterValue() {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> parameter = new Parameter<>("StringParameter", "teststring");
    pipRequest.addParameter(parameter);

    assertEquals(parameter.getValue(),
        pipRequest.getParameterValue(parameter.getName(), parameter.getType()));
  }

  @Test
  void removeParameter() {
    final PipRequest pipRequest = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> param1 = new Parameter<>("StringParameter1", "teststring");
    final Parameter<String> param2 = new Parameter<>("StringParameter2", "teststring1234");
    pipRequest.addParameter(param1);
    pipRequest.addParameter(param2);

    pipRequest.removeParameter(param1.getName());

    assertEquals(1, pipRequest.getParameters().size());
    assertFalse(pipRequest.getParameters().contains(param1));
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() {
    final PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:test:test"));
    final PipRequest pipRequest2 = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> parameter = new Parameter<>("StringParameter", "teststring");
    pipRequest1.addParameter(parameter);
    pipRequest2.addParameter(parameter);

    assertEquals(pipRequest1, pipRequest2);
    assertEquals(pipRequest1.hashCode(), pipRequest2.hashCode());
  }

  @Test
  void whenDifferentInfoIdsThenEqualsAndHashCodeFail() {
    final PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:test:test1"));
    final PipRequest pipRequest2 = new PipRequest(new InfoId("urn:info:test:test2"));
    final Parameter<String> parameter = new Parameter<>("StringParameter", "teststring");
    pipRequest1.addParameter(parameter);
    pipRequest2.addParameter(parameter);

    assertNotEquals(pipRequest1, pipRequest2);
    assertNotSame(pipRequest1.hashCode(), pipRequest2.hashCode());
  }

  @Test
  void whenDifferentParameterCountThenEqualsAndHashCodeFail() {
    final PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:test:test"));
    final PipRequest pipRequest2 = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> param1 = new Parameter<>("StringParameter", "teststring");
    final Parameter<Integer> param2 = new Parameter<>("IntegerParameter", 1234);

    pipRequest1.addParameter(param1);
    pipRequest1.addParameter(param2);
    pipRequest2.addParameter(param1);

    assertNotEquals(pipRequest1, pipRequest2);
    assertNotSame(pipRequest1.hashCode(), pipRequest2.hashCode());
  }

  @Test
  void whenDifferentParametersThenEqualsAndHashCodeFail() {
    final PipRequest pipRequest1 = new PipRequest(new InfoId("urn:info:test:test"));
    final PipRequest pipRequest2 = new PipRequest(new InfoId("urn:info:test:test"));
    final Parameter<String> param1 = new Parameter<>("Parameter1", "teststring");
    final Parameter<String> param2 = new Parameter<>("Parameter2", "teststring");

    pipRequest1.addParameter(param1);
    pipRequest2.addParameter(param2);

    assertNotEquals(pipRequest1, pipRequest2);
    assertNotSame(pipRequest1.hashCode(), pipRequest2.hashCode());
  }

}
