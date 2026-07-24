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

package de.fraunhofer.iese.mydata.pep.modifiermethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.arithmetic.MultiplyModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.utils.DataStruct;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class MultiplyModifierMethodTest {

  private ParameterList mulBy2Modify(Parameter<?> targetParameter, String expression)
      throws InhibitException {
    // Core/PDP code
    final ModifierEngine engine = new ModifierEngine("multiply",
        new ParameterList(new Parameter<Integer>("operand", 2)));

    final Modifier modifyTarget = new Modifier(targetParameter.getName());
    modifyTarget.addEngine(engine);
    if (expression != null) {
      modifyTarget.setExpression(expression);
    }

    final AuthorizationDecision decision = new AuthorizationDecision();
    decision.addModifier(modifyTarget);

    // SDK/Pep code
    final DecisionEnforcer enforcer = new JsonPathDecisionEnforcer();
    enforcer.addModificationMethod(new MultiplyModifierMethod());

    return enforcer.enforce(decision, new ParameterList(targetParameter));
  }

  @Test
  void testMulPOJOs() throws InhibitException {
    final Parameter<Integer> targetParameter1 = new Parameter<Integer>("integer", -5);
    final ParameterList result1 = this.mulBy2Modify(targetParameter1, null);
    final Integer int1 = (Integer) result1.getParameterValueForName("integer");
    assertEquals(-10, int1);

    final Parameter<Long> targetParameter2 = new Parameter<Long>("long", 5L);
    final ParameterList result2 = this.mulBy2Modify(targetParameter2, null);
    final Long long2 = (Long) result2.getParameterValueForName("long");
    assertEquals(10L, long2);

    final Parameter<Float> targetParameter3 = new Parameter<Float>("float", -5.5f);
    final ParameterList result3 = this.mulBy2Modify(targetParameter3, null);
    final Float float3 = (Float) result3.getParameterValueForName("float");
    assertEquals(-11.0f, float3);

    final Parameter<Double> targetParameter4 = new Parameter<Double>("double", 5.5);
    final ParameterList result4 = this.mulBy2Modify(targetParameter4, null);
    final Double double4 = (Double) result4.getParameterValueForName("double");
    assertEquals(11.0, double4);
  }

  // DataStruct values:
  // public Integer natural = 5;
  // public Double real = 5.5;

  @Test
  void testMulNumberExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.mulBy2Modify(targetParameter, "$.natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(10, ds1.natural);

    final ParameterList result2 = this.mulBy2Modify(targetParameter, "$.real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(11.0, ds2.real);
  }

  @Test
  void testMulNumberDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.mulBy2Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(10, ds1.natural);

    final ParameterList result2 = this.mulBy2Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(11.0, ds2.real);
  }

  @Test
  void testMulNumberDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());
    targetParameter.getValue().other = new DataStruct();

    final ParameterList result1 = this.mulBy2Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(10, ds1.natural);
    assertEquals(10, ds1.other.natural);

    final ParameterList result2 = this.mulBy2Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(11.0, ds2.real);
    assertEquals(11.0, ds2.other.real);
  }

  // DataStruct values:
  // public Integer[] naturalList = new Integer[]{-5, 0, 5};
  // public Double realList[] = new Double[]{-5.5, 0.0, 5.5};
  // public Number[] mixedList = new Number[]{-5.5, -5, -0.0, 0, 5, 5.5};

  @Test
  void testMulNumberListExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.mulBy2Modify(targetParameter, "$.naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -10, 0, 10
    }));

    final ParameterList result2 = this.mulBy2Modify(targetParameter, "$.realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -11.0, 0.0, 11.0
    }));

    final ParameterList result3 = this.mulBy2Modify(targetParameter, "$.mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -11.0, -10, -0.0, 0, 10, 11.0
    }));
  }

  @Test
  void testMulNumberListDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.mulBy2Modify(modifyTarget, "$..naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -10, 0, 10
    }));

    final ParameterList result2 = this.mulBy2Modify(modifyTarget, "$..realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -11.0, 0.0, 11.0
    }));

    final ParameterList result3 = this.mulBy2Modify(modifyTarget, "$..mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -11.0, -10, -0.0, 0, 10, 11.0
    }));
  }

  @Test
  void testMulNumberListDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());
    modifyTarget.getValue().other = new DataStruct();

    final ParameterList result1 = this.mulBy2Modify(modifyTarget, "$..naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -10, 0, 10
    }));
    assertTrue(Arrays.equals(ds1.other.naturalList, new Integer[] {
        -10, 0, 10
    }));

    final ParameterList result2 = this.mulBy2Modify(modifyTarget, "$..realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -11.0, 0.0, 11.0
    }));
    assertTrue(Arrays.equals(ds2.other.realList, new Double[] {
        -11.0, 0.0, 11.0
    }));

    final ParameterList result3 = this.mulBy2Modify(modifyTarget, "$..mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    ds3.other.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -11.0, -10, -0.0, 0, 10, 11.0
    }));
    assertTrue(Arrays.equals(ds3.other.mixedList, new Number[] {
        -11.0, -10, -0.0, 0, 10, 11.0
    }));
  }

}
