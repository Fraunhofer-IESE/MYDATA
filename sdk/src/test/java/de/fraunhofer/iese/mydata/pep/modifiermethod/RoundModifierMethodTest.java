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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.arithmetic.RoundModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.utils.DataStruct;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class RoundModifierMethodTest {

  private ParameterList roundModify(Parameter<?> targetParameter, String expression,
      ParameterList modifyParameters) throws InhibitException {
    // Core/PDP code
    final ModifierEngine engine = new ModifierEngine("round", modifyParameters);

    final Modifier modifyTarget = new Modifier(targetParameter.getName());
    modifyTarget.addEngine(engine);
    if (expression != null) {
      modifyTarget.setExpression(expression);
    }

    final AuthorizationDecision decision = new AuthorizationDecision();
    decision.addModifier(modifyTarget);

    // SDK/Pep code
    final DecisionEnforcer enforcer = new JsonPathDecisionEnforcer();
    enforcer.addModificationMethod(new RoundModifierMethod());

    return enforcer.enforce(decision, new ParameterList(targetParameter));
  }

  @Test
  void testWrongMode() throws InhibitException {
    assertThrows(IllegalArgumentException.class,
        () -> this.roundModify(new Parameter<Integer>("integer", -5), null,
            new ParameterList(new Parameter<String>("mode", "wololoo"))));
  }

  @Test
  void testRoundPOJOs() throws InhibitException {
    final Parameter<Integer> targetParameter1 = new Parameter<Integer>("integer", -5);
    final ParameterList result1 = this.roundModify(targetParameter1, null,
        new ParameterList(new Parameter<Double>("interval", 2.0)));
    final Integer int1 = (Integer) result1.getParameterValueForName("integer");
    assertEquals(-4, int1);

    final Parameter<Long> targetParameter2 = new Parameter<Long>("long", 5L);
    final ParameterList result2 = this.roundModify(targetParameter2, null, new ParameterList(
        new Parameter<Double>("interval", 5.0), new Parameter<Double>("offset", 2.0)));
    final Long long2 = (Long) result2.getParameterValueForName("long");
    assertEquals(7L, long2);

    final Parameter<Float> targetParameter3 = new Parameter<Float>("float", -5.5f);
    final ParameterList result3 = this.roundModify(targetParameter3, null,
        new ParameterList(new Parameter<Double>("offset", 0.25)));
    final Float float3 = (Float) result3.getParameterValueForName("float");
    assertEquals(-5.75f, float3);

    final Parameter<Double> targetParameter4 = new Parameter<Double>("double", 5.5);
    final ParameterList result4 = this.roundModify(targetParameter4, null, null);
    final Double double4 = (Double) result4.getParameterValueForName("double");
    assertEquals(6.0, double4);
  }

  // DataStruct values:
  // public Integer natural = 5;
  // public Double real = 5.5;

  @Test
  void testRoundNumberExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.roundModify(targetParameter, "$.natural",
        new ParameterList(new Parameter<String>("mode", "floor"),
            new Parameter<Double>("interval", 5.0), new Parameter<Double>("offset", 1.0)));
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(1, ds1.natural);

    final ParameterList result2 = this.roundModify(targetParameter, "$.real",
        new ParameterList(new Parameter<String>("mode", "floor"),
            new Parameter<Double>("interval", 5.0), new Parameter<Double>("offset", 1.0)));
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(1.0, ds2.real);
  }

  @Test
  void testRoundNumberDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.roundModify(targetParameter, "$..natural",
        new ParameterList(new Parameter<String>("mode", "ceil"),
            new Parameter<Double>("interval", 5.0), new Parameter<Double>("offset", 4.0)));
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(9, ds1.natural);

    final ParameterList result2 = this.roundModify(targetParameter, "$..real",
        new ParameterList(new Parameter<String>("mode", "ceil"),
            new Parameter<Double>("interval", 5.0), new Parameter<Double>("offset", 4.0)));
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(9.0, ds2.real);
  }

  @Test
  void testRoundNumberDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());
    targetParameter.getValue().other = new DataStruct();

    final ParameterList result1 = this.roundModify(targetParameter, "$..natural",
        new ParameterList(new Parameter<String>("mode", "round"),
            new Parameter<Double>("interval", 0.7), new Parameter<Double>("offset", 0.0)));
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(4, ds1.natural); // rounds to 4.9 then casts to Integer
    assertEquals(4, ds1.other.natural); // rounds to 4.9 then casts to Integer

    final ParameterList result2 = this.roundModify(targetParameter, "$..real",
        new ParameterList(new Parameter<String>("mode", "round"),
            new Parameter<Double>("interval", 0.7), new Parameter<Double>("offset", 0.0)));
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(5.6, ds2.real);
    assertEquals(5.6, ds2.other.real);
  }

  // DataStruct values:
  // public Integer[] naturalList = new Integer[]{-5, 0, 5};
  // public Double realList[] = new Double[]{-5.5, 0.0, 5.5};
  // public Number[] mixedList = new Number[]{-5.5, -5, -0.0, 0, 5, 5.5};

  @Test
  void testRoundNumberListExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.roundModify(targetParameter, "$.naturalList",
        new ParameterList(new Parameter<String>("mode", "round"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -4, 0, 5
    }));

    final ParameterList result2 = this.roundModify(targetParameter, "$.realList",
        new ParameterList(new Parameter<String>("mode", "round"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -4.75, 0.25, 5.25
    }));

    final ParameterList result3 = this.roundModify(targetParameter, "$.mixedList",
        new ParameterList(new Parameter<String>("mode", "round"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -4.75, -4, 0.25, 0, 5, 5.25
    }));
  }

  @Test
  void testRoundNumberListDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.roundModify(modifyTarget, "$..naturalList",
        new ParameterList(new Parameter<String>("mode", "floor"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -7, -2, 2
    }));

    final ParameterList result2 = this.roundModify(modifyTarget, "$..realList",
        new ParameterList(new Parameter<String>("mode", "floor"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -7.25, -2.25, 5.25
    }));

    final ParameterList result3 = this.roundModify(modifyTarget, "$..mixedList",
        new ParameterList(new Parameter<String>("mode", "floor"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -7.25, -7, -2.25, -2, 2, 5.25
    }));
  }

  @Test
  void testRoundNumberListDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());
    modifyTarget.getValue().other = new DataStruct();

    final ParameterList result1 = this.roundModify(modifyTarget, "$..naturalList",
        new ParameterList(new Parameter<String>("mode", "ceil"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -4, 0, 5
    }));
    assertTrue(Arrays.equals(ds1.other.naturalList, new Integer[] {
        -4, 0, 5
    }));

    final ParameterList result2 = this.roundModify(modifyTarget, "$..realList",
        new ParameterList(new Parameter<String>("mode", "ceil"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -4.75, 0.25, 7.75
    }));
    assertTrue(Arrays.equals(ds2.other.realList, new Double[] {
        -4.75, 0.25, 7.75
    }));

    final ParameterList result3 = this.roundModify(modifyTarget, "$..mixedList",
        new ParameterList(new Parameter<String>("mode", "ceil"),
            new Parameter<Double>("interval", 2.5), new Parameter<Double>("offset", 0.25)));
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    ds3.other.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -4.75, -4, 0.25, 0, 5, 7.75
    }));
    assertTrue(Arrays.equals(ds3.other.mixedList, new Number[] {
        -4.75, -4, 0.25, 0, 5, 7.75
    }));
  }

}
