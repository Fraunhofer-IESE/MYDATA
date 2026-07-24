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

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.pep.common.DecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.enforce.JsonPathDecisionEnforcer;
import de.fraunhofer.iese.mydata.pep.modifiers.arithmetic.AddModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.utils.DataStruct;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class AddModifierMethodTest {

  private ParameterList add1Modify(Parameter<?> targetParameter, String expression)
      throws InhibitException {
    // Core/PDP code
    final ModifierEngine engine = new ModifierEngine("add",
        new ParameterList(new Parameter<Integer>("operand", 1)));

    final Modifier modifyTarget = new Modifier(targetParameter.getName());
    modifyTarget.addEngine(engine);
    if (expression != null) {
      modifyTarget.setExpression(expression);
    }

    final AuthorizationDecision decision = new AuthorizationDecision();
    decision.addModifier(modifyTarget);

    // SDK/Pep code
    final DecisionEnforcer enforcer = new JsonPathDecisionEnforcer();
    enforcer.addModificationMethod(new AddModifierMethod());

    return enforcer.enforce(decision, new ParameterList(targetParameter));
  }

  @Test
  public void testAddPOJOs() throws InhibitException {
    final Parameter<Integer> targetParameter1 = new Parameter<Integer>("integer", -5);
    final ParameterList result1 = this.add1Modify(targetParameter1, null);
    final Integer int1 = (Integer) result1.getParameterValueForName("integer");
    assertTrue(int1.equals(-4));

    final Parameter<Long> targetParameter2 = new Parameter<Long>("long", 5L);
    final ParameterList result2 = this.add1Modify(targetParameter2, null);
    final Long long2 = (Long) result2.getParameterValueForName("long");
    assertTrue(long2.equals(6L));

    final Parameter<Float> targetParameter3 = new Parameter<Float>("float", -5.5f);
    final ParameterList result3 = this.add1Modify(targetParameter3, null);
    final Float float3 = (Float) result3.getParameterValueForName("float");
    assertTrue(float3.equals(-4.5f));

    final Parameter<Double> targetParameter4 = new Parameter<Double>("double", 5.5);
    final ParameterList result4 = this.add1Modify(targetParameter4, null);
    final Double double4 = (Double) result4.getParameterValueForName("double");
    assertTrue(double4.equals(6.5));
  }

  // DataStruct values:
  // public Integer natural = 5;
  // public Double real = 5.5;

  @Test
  public void testAddNumberExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.add1Modify(targetParameter, "$.natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(ds1.natural.equals(6));

    final ParameterList result2 = this.add1Modify(targetParameter, "$.real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(ds2.real.equals(6.5));
  }

  @Test
  public void testAddNumberDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.add1Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(ds1.natural.equals(6));

    final ParameterList result2 = this.add1Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(ds2.real.equals(6.5));
  }

  @Test
  public void testAddNumberDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());
    targetParameter.getValue().other = new DataStruct();

    final ParameterList result1 = this.add1Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(ds1.natural.equals(6));
    assertTrue(ds1.other.natural.equals(6));

    final ParameterList result2 = this.add1Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(ds2.real.equals(6.5));
    assertTrue(ds2.other.real.equals(6.5));
  }

  // DataStruct values:
  // public Integer[] naturalList = new Integer[]{-5, 0, 5};
  // public Double realList[] = new Double[]{-5.5, 0.0, 5.5};
  // public Number[] mixedList = new Number[]{-5.5, -5, -0.0, 0, 5, 5.5};

  @Test
  public void testAddNumberListExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.add1Modify(targetParameter, "$.naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -4, 1, 6
    }));

    final ParameterList result2 = this.add1Modify(targetParameter, "$.realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -4.5, 1.0, 6.5
    }));

    final ParameterList result3 = this.add1Modify(targetParameter, "$.mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -4.5, -4, 1.0, 1, 6, 6.5
    }));
  }

  @Test
  public void testAddNumberListDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.add1Modify(modifyTarget, "$..naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -4, 1, 6
    }));

    final ParameterList result2 = this.add1Modify(modifyTarget, "$..realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -4.5, 1.0, 6.5
    }));

    final ParameterList result3 = this.add1Modify(modifyTarget, "$..mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -4.5, -4, 1.0, 1, 6, 6.5
    }));
  }

  @Test
  public void testAddNumberListDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());
    modifyTarget.getValue().other = new DataStruct();

    final ParameterList result1 = this.add1Modify(modifyTarget, "$..naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -4, 1, 6
    }));
    assertTrue(Arrays.equals(ds1.other.naturalList, new Integer[] {
        -4, 1, 6
    }));

    final ParameterList result2 = this.add1Modify(modifyTarget, "$..realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -4.5, 1.0, 6.5
    }));
    assertTrue(Arrays.equals(ds2.other.realList, new Double[] {
        -4.5, 1.0, 6.5
    }));

    final ParameterList result3 = this.add1Modify(modifyTarget, "$..mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    ds3.other.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -4.5, -4, 1.0, 1, 6, 6.5
    }));
    assertTrue(Arrays.equals(ds3.other.mixedList, new Number[] {
        -4.5, -4, 1.0, 1, 6, 6.5
    }));
  }

}
