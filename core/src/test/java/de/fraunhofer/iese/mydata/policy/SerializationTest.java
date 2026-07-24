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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Class TestSerialization.
 */
class SerializationTest {

  /**
   * Test correct component type.
   */
  @Test
  void correctComponentType() {
    final Event e = new Event(new ActionId("urn:action:b:a"), new Parameter<>("blA", true),
        new Parameter<>("blubb", URI.create("https://A.de")));
    final String json = e.toString();
    final Event e1 = MyDataEntity.fromJson(json, Event.class);

    //    assertThat(e1, notNullValue());
    //    assertThat(e.getActionId(), equalTo(e1.getActionId()));
    //    assertThat(e1.getParameterForName("blA"), notNullValue());
    //    assertThat(e1.getParameterForName("blubb"), notNullValue());
    //    assertThat(e1.getParameterForName("blubb").getValue(),
    //        equalTo(e.getParameterForName("blubb").getValue()));
    //    assertThat(e1.getParameterForName("blubb").getType(),
    //        equalTo((Class) e.getParameterForName("blubb").getType()));

    assertNotNull(e1);
    assertEquals(e.getActionId(), e1.getActionId());
    assertNotNull(e1.getParameterForName("blA"));
    assertNotNull(e1.getParameterForName("blubb"));
    assertEquals(e.getParameterForName("blubb").getValue(),
        e1.getParameterForName("blubb").getValue());
    assertEquals(e.getParameterForName("blubb").getType(),
        e1.getParameterForName("blubb").getType());

  }

  @Test
  void eventDeserialization() throws Exception {
    final String json = this.readResourceFile("jsonEvent.json");
    final Event e = MyDataEntity.fromJson(json, Event.class);
  }

  /**
   * Read resource file.
   *
   * @param  file               the file
   * @return                    the string
   * @throws IOException        Signals that an I/O exception has occurred.
   * @throws URISyntaxException the URI syntax exception
   */
  private String readResourceFile(String file) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(file).toURI())));
  }

}
