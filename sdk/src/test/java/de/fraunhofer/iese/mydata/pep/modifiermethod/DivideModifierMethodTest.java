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
import de.fraunhofer.iese.mydata.pep.modifiers.arithmetic.DivideModifierMethod;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.utils.DataStruct;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class DivideModifierMethodTest {

  private ParameterList divBy5Modify(Parameter<?> targetParameter, String expression)
      throws InhibitException {
    // Core/PDP code
    final ModifierEngine engine = new ModifierEngine("divide",
        new ParameterList(new Parameter<Integer>("denominator", 5)));

    final Modifier modifyTarget = new Modifier(targetParameter.getName());
    modifyTarget.addEngine(engine);
    if (expression != null) {
      modifyTarget.setExpression(expression);
    }

    final AuthorizationDecision decision = new AuthorizationDecision();
    decision.addModifier(modifyTarget);

    // SDK/Pep code
    final DecisionEnforcer enforcer = new JsonPathDecisionEnforcer();
    enforcer.addModificationMethod(new DivideModifierMethod());

    return enforcer.enforce(decision, new ParameterList(targetParameter));
  }

  @Test
  void testDivPOJOs() throws InhibitException {
    final Parameter<Integer> targetParameter1 = new Parameter<Integer>("integer", -5);
    final ParameterList result1 = this.divBy5Modify(targetParameter1, null);
    final Integer int1 = (Integer) result1.getParameterValueForName("integer");
    assertEquals(-1, int1);

    final Parameter<Long> targetParameter2 = new Parameter<Long>("long", 5L);
    final ParameterList result2 = this.divBy5Modify(targetParameter2, null);
    final Long long2 = (Long) result2.getParameterValueForName("long");
    assertEquals(1L, long2);

    final Parameter<Float> targetParameter3 = new Parameter<Float>("float", -5.5f);
    final ParameterList result3 = this.divBy5Modify(targetParameter3, null);
    final Float float3 = (Float) result3.getParameterValueForName("float");
    assertEquals(-1.1f, float3);

    final Parameter<Double> targetParameter4 = new Parameter<Double>("double", 5.5);
    final ParameterList result4 = this.divBy5Modify(targetParameter4, null);
    final Double double4 = (Double) result4.getParameterValueForName("double");
    assertEquals(1.1, double4);
  }

  @Test
  void testDivNumberExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.divBy5Modify(targetParameter, "$.natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(1, ds1.natural);

    final ParameterList result2 = this.divBy5Modify(targetParameter, "$.real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(1.1, ds2.real);
  }

  @Test
  void testDivNumberDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.divBy5Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(1, ds1.natural);

    final ParameterList result2 = this.divBy5Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(1.1, ds2.real);
  }

  @Test
  void testDivNumberDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());
    targetParameter.getValue().other = new DataStruct();

    final ParameterList result1 = this.divBy5Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(1, ds1.natural);
    assertEquals(1, ds1.other.natural);

    final ParameterList result2 = this.divBy5Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(1.1, ds2.real);
    assertEquals(1.1, ds2.other.real);
  }

  // DataStruct values:
  // public Integer[] naturalList = new Integer[]{-5, 0, 5};
  // public Double realList[] = new Double[]{-5.5, 0.0, 5.5};
  // public Number[] mixedList = new Number[]{-5.5, -5, -0.0, 0, 5, 5.5};

  @Test
  void testDivNumberListExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.divBy5Modify(targetParameter, "$.naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -1, 0, 1
    }));

    final ParameterList result2 = this.divBy5Modify(targetParameter, "$.realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -1.1, 0.0, 1.1
    }));

    final ParameterList result3 = this.divBy5Modify(targetParameter, "$.mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -1.1, -1, -0.0, 0, 1, 1.1
    }));
  }

  @Test
  void testDivNumberListDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.divBy5Modify(modifyTarget, "$..naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -1, 0, 1
    }));

    final ParameterList result2 = this.divBy5Modify(modifyTarget, "$..realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -1.1, 0.0, 1.1
    }));

    final ParameterList result3 = this.divBy5Modify(modifyTarget, "$..mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -1.1, -1, -0.0, 0, 1, 1.1
    }));
  }

  @Test
  void testDivNumberListDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());
    modifyTarget.getValue().other = new DataStruct();

    final ParameterList result1 = this.divBy5Modify(modifyTarget, "$..naturalList");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
        -1, 0, 1
    }));
    assertTrue(Arrays.equals(ds1.other.naturalList, new Integer[] {
        -1, 0, 1
    }));

    final ParameterList result2 = this.divBy5Modify(modifyTarget, "$..realList");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertTrue(Arrays.equals(ds2.realList, new Double[] {
        -1.1, 0.0, 1.1
    }));
    assertTrue(Arrays.equals(ds2.other.realList, new Double[] {
        -1.1, 0.0, 1.1
    }));

    final ParameterList result3 = this.divBy5Modify(modifyTarget, "$..mixedList");
    final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
    // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
    // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
    // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
    // so making a new array to compare, even one containing the exact same numbers, doesn't work
    ds3.numberFix();
    ds3.other.numberFix();
    assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
        -1.1, -1, -0.0, 0, 1, 1.1
    }));
    assertTrue(Arrays.equals(ds3.other.mixedList, new Number[] {
        -1.1, -1, -0.0, 0, 1, 1.1
    }));
  }

  private ParameterList denOf19dot255Modify(Parameter<?> targetParameter, String expression)
      throws InhibitException {
    // Core/PDP code
    final ModifierEngine engine = new ModifierEngine("divide",
        new ParameterList(new Parameter<Double>("numerator", 19.25)));

    final Modifier modifyTarget = new Modifier(targetParameter.getName());
    modifyTarget.addEngine(engine);
    if (expression != null) {
      modifyTarget.setExpression(expression);
    }

    final AuthorizationDecision decision = new AuthorizationDecision();
    decision.addModifier(modifyTarget);

    // SDK/Pep code
    final DecisionEnforcer enforcer = new JsonPathDecisionEnforcer();
    enforcer.addModificationMethod(new DivideModifierMethod());

    return enforcer.enforce(decision, new ParameterList(targetParameter));
  }

  @Test
  void testDenPOJOs() throws InhibitException {
    final Parameter<Integer> targetParameter1 = new Parameter<>("integer", -5);
    final ParameterList result1 = this.denOf19dot255Modify(targetParameter1, null);
    final Integer int1 = (Integer) result1.getParameterValueForName("integer");
    assertEquals(-3, int1);

    final Parameter<Long> targetParameter2 = new Parameter<>("long", 5L);
    final ParameterList result2 = this.denOf19dot255Modify(targetParameter2, null);
    final Long long2 = (Long) result2.getParameterValueForName("long");
    assertEquals(3L, long2);

    final Parameter<Float> targetParameter3 = new Parameter<>("float", -5.5f);
    final ParameterList result3 = this.denOf19dot255Modify(targetParameter3, null);
    final Float float3 = (Float) result3.getParameterValueForName("float");
    assertEquals(-3.5f, float3);

    final Parameter<Double> targetParameter4 = new Parameter<>("double", 5.5);
    final ParameterList result4 = this.denOf19dot255Modify(targetParameter4, null);
    final Double double4 = (Double) result4.getParameterValueForName("double");
    assertEquals(3.5, double4);
  }

  // DataStruct values:
  // public Integer natural = 5;
  // public Double real = 5.5;

  @Test
  void testDenNumberExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.denOf19dot255Modify(targetParameter, "$.natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(3, ds1.natural);

    final ParameterList result2 = this.denOf19dot255Modify(targetParameter, "$.real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(3.5, ds2.real);
  }

  @Test
  void testDenNumberDeepSearchExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    final ParameterList result1 = this.denOf19dot255Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(3, ds1.natural);

    final ParameterList result2 = this.denOf19dot255Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(3.5, ds2.real);
  }

  @Test
  void testDenNumberDeepSearchExpressionWithOtherLayers() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());
    targetParameter.getValue().other = new DataStruct();

    final ParameterList result1 = this.denOf19dot255Modify(targetParameter, "$..natural");
    final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
    assertEquals(3, ds1.natural);
    assertEquals(3, ds1.other.natural);

    final ParameterList result2 = this.denOf19dot255Modify(targetParameter, "$..real");
    final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
    assertEquals(3.5, ds2.real);
    assertEquals(3.5, ds2.other.real);
  }

  // DataStruct values:
  // public Integer[] naturalList = new Integer[]{-5, 0, 5};
  // public Double realList[] = new Double[]{-5.5, 0.0, 5.5};
  // public Number[] mixedList = new Number[]{-5.5, -5, -0.0, 0, 5, 5.5};

  @Test
  void testDenNumberListExpression() throws InhibitException {
    final Parameter<DataStruct> targetParameter = new Parameter<DataStruct>("ds", new DataStruct());

    assertThrows(InhibitException.class, () -> {
      final ParameterList result1 = this.denOf19dot255Modify(targetParameter, "$.naturalList");
      final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
      assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
          -3, 0, 3
      }));
    });

    assertThrows(InhibitException.class, () -> {
      final ParameterList result2 = this.denOf19dot255Modify(targetParameter, "$.realList");
      final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
      assertTrue(Arrays.equals(ds2.realList, new Double[] {
          -3.5, Double.NaN, 3.5
      }));
    });

    assertThrows(InhibitException.class, () -> {
      final ParameterList result3 = this.denOf19dot255Modify(targetParameter, "$.mixedList");
      final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
      // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
      // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
      // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
      // so making a new array to compare, even one containing the exact same numbers, doesn't work
      ds3.numberFix();
      assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
          -3.5, -3, Double.NaN, 0, 3, 3.5
      }));
    });
  }

  @Test
  void testDenNumberListDeepSearchExpression1() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<>("ds", new DataStruct());

    assertThrows(InhibitException.class, () -> {
      final ParameterList result1 = this.denOf19dot255Modify(modifyTarget, "$..naturalList");
      final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
      assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
          -3, 0, 3
      }));
    });
  }

  @Test
  void testDenNumberListDeepSearchExpression2() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<>("ds", new DataStruct());

    assertThrows(InhibitException.class, () -> {
      final ParameterList result2 = this.denOf19dot255Modify(modifyTarget, "$..realList");
      final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
      assertTrue(Arrays.equals(ds2.realList, new Double[] {
          -3.5, Double.NaN, 3.5
      }));
    });
  }

  @Test
  public void testDenNumberListDeepSearchExpression3() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<>("ds", new DataStruct());

    assertThrows(InhibitException.class, () -> {
      final ParameterList result3 = this.denOf19dot255Modify(modifyTarget, "$..mixedList");
      final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
      // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
      // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
      // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
      // so making a new array to compare, even one containing the exact same numbers, doesn't work
      ds3.numberFix();
      assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
          -3.5, -3, Double.NaN, 0, 3, 3.5
      }));
    });
  }

  @Test
  void testDenNumberListDeepSearchExpressionWithOtherLayers1() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());
    modifyTarget.getValue().other = new DataStruct();

    assertThrows(InhibitException.class, () -> {
      final ParameterList result1 = this.denOf19dot255Modify(modifyTarget, "$..naturalList");
      final DataStruct ds1 = (DataStruct) result1.getParameterValueForName("ds");
      assertTrue(Arrays.equals(ds1.naturalList, new Integer[] {
          -3, 0, 3
      }));
      assertTrue(Arrays.equals(ds1.other.naturalList, new Integer[] {
          -3, 0, 3
      }));
    });
  }

  @Test
  void testDenNumberListDeepSearchExpressionWithOtherLayers2() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());
    modifyTarget.getValue().other = new DataStruct();

    assertThrows(InhibitException.class, () -> {
      final ParameterList result2 = this.denOf19dot255Modify(modifyTarget, "$..realList");
      final DataStruct ds2 = (DataStruct) result2.getParameterValueForName("ds");
      assertTrue(Arrays.equals(ds2.realList, new Double[] {
          -3.5, Double.NaN, 3.5
      }));
      assertTrue(Arrays.equals(ds2.other.realList, new Double[] {
          -3.5, Double.NaN, 3.5
      }));
    });
  }

  @Test
  void testDenNumberListDeepSearchExpressionWithOtherLayers3() throws InhibitException {
    final Parameter<DataStruct> modifyTarget = new Parameter<DataStruct>("ds", new DataStruct());
    modifyTarget.getValue().other = new DataStruct();

    assertThrows(InhibitException.class, () -> {
      final ParameterList result3 = this.denOf19dot255Modify(modifyTarget, "$..mixedList");
      final DataStruct ds3 = (DataStruct) result3.getParameterValueForName("ds");
      ds3.numberFix();
      // ds3.mixedList comes as an array of LazilyParsedNumbers because DataStruct says Number[] and
      // Gson doesn't try to be more specific even though Integer and Double class information is available at the time
      // LazilyParsedNumbers .equal() only returns true if they point to the exact same object,
      // so making a new array to compare, even one containing the exact same numbers, doesn't work
      ds3.other.numberFix();
      assertTrue(Arrays.equals(ds3.mixedList, new Number[] {
          -3.5, -3, Double.NaN, 0, 3, 3.5
      }));
      assertTrue(Arrays.equals(ds3.other.mixedList, new Number[] {
          -3.5, -3, Double.NaN, 0, 3, 3.5
      }));
    });
  }

}
