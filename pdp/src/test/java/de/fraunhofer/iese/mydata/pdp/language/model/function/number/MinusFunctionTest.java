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

package de.fraunhofer.iese.mydata.pdp.language.model.function.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.pdp.Evaluation;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The Class AndOperatorTest.
 */
public class MinusFunctionTest {
  public Evaluation evaluation;

  /**
   * And operator basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @ParameterizedTest(name = "minus {0}")
  public void minusOperatorBasicTests(Evaluation evaluation) throws EvaluationUndecidableException {

    this.initMinusFunctionTest(evaluation);

    if (evaluation.expectedException != null) {
      assertThrows(evaluation.expectedException, () -> {

        final MinusFunction operator = new MinusFunction();
        final List<IFunction> params = new ArrayList<>();
        for (final Object o : evaluation.parameters) {
          params.add(new PolicyConstant(o));
        }

        operator.setParameters(params);

        final DataObject<Number> result = operator.evaluate(null);
        assertEquals(evaluation.expectedResult, result.getValue());
      });
      return;
    }

    final MinusFunction operator = new MinusFunction();
    final List<IFunction> params = new ArrayList<>();
    for (final Object o : evaluation.parameters) {
      params.add(new PolicyConstant(o));
    }

    operator.setParameters(params);

    final DataObject<Number> result = operator.evaluate(null);
    assertEquals(evaluation.expectedResult, result.getValue());
  }

  /**
   * Data.
   *
   * @return the collection
   */
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {
            new Evaluation(0, 1, 1)
        }, {
            new Evaluation(0, -1, -1)
        }, {
            new Evaluation(0, 0, 0)
        }, {
            new Evaluation(-1, 1, 2)
        }, {
            new Evaluation(1, 2, 1)
        }, {
            new Evaluation(-8, 1, 2, 3, 4)
        }, {
            new Evaluation(-8, 1.0f, 2, 3, 4)
        }, {
            new Evaluation(-4.4, 6, 1.4, 2, 3, 4)
        }, {
            new Evaluation(-7, 1.5d, 2, 2.5f, 4l)
        }, {
            new Evaluation(1, 3, "2")
        }, {
            new Evaluation(0.5, 3, "2.5")
        }, {
            new Evaluation(1, 3, "2.0")
        }, {
            new Evaluation(new Double(Float.MAX_VALUE), Float.MAX_VALUE, 0)
        }, {
            new Evaluation(new Double(Float.MAX_VALUE - 1.0d), Float.MAX_VALUE, 1d)
        }, {
            new Evaluation(Integer.MAX_VALUE - 300, Integer.MAX_VALUE, 300l)

        }, {
            new Evaluation(EvaluationUndecidableException.class, 3, "a String")
        }, {
            new Evaluation(EvaluationUndecidableException.class)
        }
    });
  }

  public void initMinusFunctionTest(Evaluation evaluation) {
    this.evaluation = evaluation;
  }

}
