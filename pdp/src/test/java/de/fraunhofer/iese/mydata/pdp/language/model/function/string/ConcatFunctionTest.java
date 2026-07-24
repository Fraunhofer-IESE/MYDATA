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

package de.fraunhofer.iese.mydata.pdp.language.model.function.string;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.pdp.Evaluation;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The Class ConcatFunctionTest.
 */
public class ConcatFunctionTest {
  public Evaluation evaluation;

  /**
   * Concat function basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @ParameterizedTest(name = "concat {0}")
  public void concatFunctionBasicTests(Evaluation evaluation)
      throws EvaluationUndecidableException {

    this.initConcatFunctionTest(evaluation);

    if (this.evaluation.expectedException != null) {
      assertThrows(this.evaluation.expectedException, () -> {

        final ConcatFunction function = new ConcatFunction();
        final List<IFunction> params = new ArrayList<>();
        for (final Object o : this.evaluation.parameters) {
          params.add(new PolicyConstant(o));
        }

        function.setParameters(params);

        final DataObject<String> result = function.evaluate(null);
        assertEquals(this.evaluation.expectedResult, result.getValue());
      });
      return;
    }

    final ConcatFunction function = new ConcatFunction();
    final List<IFunction> params = new ArrayList<>();
    for (final Object o : this.evaluation.parameters) {
      params.add(new PolicyConstant(o));
    }

    function.setParameters(params);

    final DataObject<String> result = function.evaluate(null);
    assertEquals(this.evaluation.expectedResult, result.getValue());
  }

  /**
   * Data.
   *
   * @return                    the collection
   * @throws URISyntaxException
   * @throws IOException
   */
  public static Collection<Object[]> data() throws IOException, URISyntaxException {
    final de.fraunhofer.iese.mydata.policy.parameter.Parameter p = getUserParameter();
    return Arrays.asList(new Object[][] {
        {
            new Evaluation("31", 3, 1)
        }, {
            new Evaluation("blabla", "bla", "bla")
        }, {
            new Evaluation("bla", "bla")
        }, {
            new Evaluation("bla1", "bla", 1)
        }, {
            new Evaluation("")
        }, {
            new Evaluation("urn:bla:blubb1", URI.create("urn:bla:blubb"), 1)
        }, {
            new Evaluation(
                "{\"name\":{\"firstName\":\"John\",\"lastName\":\"doe\"},\"addresses\":[{\"street\":{\"streetName\":\"naist street\",\"number\":32},\"city\":\"Lautre\",\"zip\":67663},{\"street\":{\"streetName\":\"bla street\",\"number\":42},\"city\":\"Monnem\",\"zip\":68161}],\"age\":26}1",
                p.getValue(), 1)
        }

    });
  }

  private static de.fraunhofer.iese.mydata.policy.parameter.Parameter getUserParameter()
      throws IOException, URISyntaxException {
    return MyDataEntity.fromJson(readResourceFile("unknownParameter.json"),
        de.fraunhofer.iese.mydata.policy.parameter.Parameter.class);
  }

  /**
   * Read resource file.
   *
   * @param  file               the file
   * @return                    the string
   * @throws IOException        Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private static String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(Files.readAllBytes(
        Paths.get(ConcatFunctionTest.class.getClassLoader().getResource(file).toURI())));
  }

  public void initConcatFunctionTest(Evaluation evaluation) {
    this.evaluation = evaluation;
  }
}
