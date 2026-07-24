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
 * The Class LessFunctionTest.
 */
public class LessFunctionTest {
  public Evaluation evaluation;

  /**
   * Less function basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @ParameterizedTest(name = "less {0}")
  public void lessFunctionBasicTests(Evaluation evaluation) throws EvaluationUndecidableException {

    this.initLessFunctionTest(evaluation);

    if (evaluation.expectedException != null) {
      assertThrows(evaluation.expectedException, () -> {

        final BooleanFunction function = new LessFunction();
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

    final BooleanFunction function = new LessFunction();
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
            new Evaluation(false, 3, 1)
        }, {
            new Evaluation(false, 0, 0)
        }, {
            new Evaluation(false, 3.0, 1)
        }, {
            new Evaluation(true, 1, 3)
        }, {
            new Evaluation(true, 1, 3.0)
        }, {
            new Evaluation(false, 8, 7, 6, 4, 4, 3, 3, 1, 0.4)
        }, {
            new Evaluation(true, 1, 4, 6, 8, 9, 12, Integer.MAX_VALUE)
        }, {
            new Evaluation(false, 1, 4, 6, 6, 8, 9, 12, Integer.MAX_VALUE)
        }, {
            new Evaluation(false, Integer.MAX_VALUE, Integer.MIN_VALUE)
        }, {
            new Evaluation(IllegalArgumentException.class, 3, "2")
        }, {
            new Evaluation(EvaluationUndecidableException.class, 3)
        }, {
            new Evaluation(EvaluationUndecidableException.class)
        }
    });
  }

  public void initLessFunctionTest(Evaluation evaluation) {
    this.evaluation = evaluation;
  }

}
