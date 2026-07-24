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

package de.fraunhofer.iese.mydata.pdp.language.model.function.bool;

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
 * The Class GreaterEqualFunctionTest.
 */
public class GreaterEqualFunctionTest {
  public Evaluation evaluation;

  /**
   * Greater equal function basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @ParameterizedTest(name = "greaterEqual {0}")
  public void greaterEqualFunctionBasicTests(Evaluation evaluation)
      throws EvaluationUndecidableException {

    this.initGreaterEqualFunctionTest(evaluation);

    if (evaluation.expectedException != null) {
      assertThrows(evaluation.expectedException, () -> {

        final GreaterEqualFunction function = new GreaterEqualFunction();
        final List<IFunction> params = new ArrayList<>();
        for (final Object o : evaluation.parameters) {
          params.add(new PolicyConstant(o));
        }

        function.setParameters(params);

        final DataObject<Boolean> result = function.evaluate(null);
        assertEquals(evaluation.expectedResult, result.getValue());
      });
      return;
    }

    final GreaterEqualFunction function = new GreaterEqualFunction();
    final List<IFunction> params = new ArrayList<>();
    for (final Object o : evaluation.parameters) {
      params.add(new PolicyConstant(o));
    }

    function.setParameters(params);

    final DataObject<Boolean> result = function.evaluate(null);
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
            new Evaluation(true, 3, 1)
        }, {
            new Evaluation(true, 0, 0)
        }, {
            new Evaluation(true, 3.0, 1)
        }, {
            new Evaluation(false, 1, 3)
        }, {
            new Evaluation(false, 1, 3.0)
        }, {
            new Evaluation(true, 8, 7, 6, 4, 4, 3, 3, 1, 0.4)
        }, {
            new Evaluation(false, 8, 7, 6, 4, 2, 3, 3, 1)
        }, {
            new Evaluation(true,
                new Double(Integer.MAX_VALUE + 1l) * new Double(Integer.MAX_VALUE + 1l),
                4611686014132420609l)
        }, {
            new Evaluation(IllegalArgumentException.class, 3, "2")
        }, {
            new Evaluation(EvaluationUndecidableException.class, 3)
        }, {
            new Evaluation(EvaluationUndecidableException.class)
        }
    });
  }

  public void initGreaterEqualFunctionTest(Evaluation evaluation) {
    this.evaluation = evaluation;
  }

}
