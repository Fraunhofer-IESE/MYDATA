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

package de.fraunhofer.iese.mydata.component.information;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.information.method.TypeDescription;
import de.fraunhofer.iese.mydata.testmodel.ClassA;
import de.fraunhofer.iese.mydata.testmodel.ClassWithArrayAsHas;
import de.fraunhofer.iese.mydata.testmodel.ClassWithMapAsHas;
import de.fraunhofer.iese.mydata.testmodel.ObjectTop2;
import de.fraunhofer.iese.mydata.testmodel.Project;
import de.fraunhofer.iese.mydata.testmodel.Task;

import ch.qos.logback.classic.Level;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class JsonSchemaGeneratorTest {

  private static final boolean ENABLE_LOGGING = false;
  private static final Logger logger = LoggerFactory.getLogger(JsonSchemaGeneratorTest.class);

  private static Gson gson;

  @BeforeAll
  static void init() {
    if (ENABLE_LOGGING && logger instanceof ch.qos.logback.classic.Logger logbackLogger) {
      logbackLogger.setLevel(Level.DEBUG);
    }
    gson = MyDataEntity.getGson();
  }

  @Test
  void simpleListStringTest() {
    final Type listProject = new TypeToken<List<String>>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(listProject,
          LinkedList.class);
      logger.debug(gson.toJson(typeDescription));
    });
  }

  @Test
  void complexObjectTest() {
    final Type projectType = new TypeToken<Project>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(projectType,
          Project.class);
      logger.debug(gson.toJson(typeDescription));
    });
  }

  @Test
  void listListTest() {
    final Type listListType = new TypeToken<List<List<Project>>>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator
          .generateTypeDescription(listListType, ArrayList.class);
      logger.debug(gson.toJson(typeDescription));
    });
  }

  @Test
  void mapMapTest() {
    final Type listListType = new TypeToken<Map<String, Map<String, Project>>>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator
          .generateTypeDescription(listListType, HashMap.class);
      logger.debug(gson.toJson(typeDescription));
    });
  }

  @Test
  void complexObjectListTest() {
    final Type taskType = new TypeToken<Task>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(taskType,
          Task.class);
      logger.debug(gson.toJson(typeDescription));
    });
  }

  @Test
  void map() {
    final Type mapType = new TypeToken<Map<String, Task>>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(mapType,
          Map.class);
      logger.debug(gson.toJson(typeDescription));
    });
  }

  @Test
  void masAsHas() {
    final Type mapType = new TypeToken<ClassWithMapAsHas>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(mapType,
          ClassWithMapAsHas.class);
      logger.debug(gson.toJson(typeDescription));
    });
  }

  @Test
  void array() {
    final Type arrayType = new TypeToken<ClassWithArrayAsHas>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(arrayType,
          ClassWithArrayAsHas.class);
      logger.debug(gson.toJson(typeDescription));
    });

  }

  @Test
  void loopTest() {
    final Type type = new TypeToken<ClassA>() {
    }.getType();
    Assertions.assertDoesNotThrow(() -> {
      final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(type,
          ClassA.class);
      for (final TypeDescription d : typeDescription.getReferencedTypeDescriptions().values()) {
        logger.debug(d.getTypeName());
      }
    });
  }

  @Test
  void test() {
    Assertions.assertDoesNotThrow(() -> {
      final Field[] allFields = JsonSchemaGenerator.getAllFields(ObjectTop2.class);
      logger.debug(Arrays.toString(allFields));
    });
  }

}
