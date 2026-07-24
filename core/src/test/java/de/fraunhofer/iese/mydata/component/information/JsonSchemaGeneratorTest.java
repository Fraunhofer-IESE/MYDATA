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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class JsonSchemaGeneratorTest {

  private static Gson gson;

  @BeforeAll
  static void init() {
    gson = MyDataEntity.getGson();
  }

  @Test
  void simpleListStringTest() {
    final Type listProject = new TypeToken<List<String>>() {
    }.getType();
    final List<String> strings = new LinkedList<>();
    final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(listProject,
        strings.getClass());
    System.out.println(gson.toJson(typeDescription));
  }

  @Test
  void complexObjectTest() {
    final Type projectType = new TypeToken<Project>() {
    }.getType();
    final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(projectType,
        Project.class);
    System.out.println(gson.toJson(typeDescription));
  }

  @Test
  void listListTest() {
    final Type listListType = new TypeToken<List<List<Project>>>() {
    }.getType();
    final List<List<Project>> list = new ArrayList<>();
    final TypeDescription typeDescription = JsonSchemaGenerator
        .generateTypeDescription(listListType, list.getClass());
    System.out.println(gson.toJson(typeDescription));
  }

  @Test
  void mapMapTest() {
    final Type listListType = new TypeToken<Map<String, Map<String, Project>>>() {
    }.getType();
    final Map<String, Map<String, Project>> map = new HashMap<>();
    final TypeDescription typeDescription = JsonSchemaGenerator
        .generateTypeDescription(listListType, map.getClass());
    System.out.println(gson.toJson(typeDescription));
  }

  @Test
  void complexObjectListTest() {
    final Type taskType = new TypeToken<Task>() {
    }.getType();
    final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(taskType,
        Task.class);
    System.out.println(gson.toJson(typeDescription));
  }

  @Test
  void map() {
    final Type mapType = new TypeToken<Map<String, Task>>() {
    }.getType();
    final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(mapType,
        Map.class);
    System.out.println(gson.toJson(typeDescription));
  }

  @Test
  void masAsHas() {
    final Type mapType = new TypeToken<ClassWithMapAsHas>() {
    }.getType();
    final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(mapType,
        ClassWithMapAsHas.class);
    System.out.println(gson.toJson(typeDescription));
  }

  @Test
  void array() {
    final Type arrayType = new TypeToken<ClassWithArrayAsHas>() {
    }.getType();
    final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(arrayType,
        ClassWithArrayAsHas.class);
    System.out.println(gson.toJson(typeDescription));

  }

  @Test
  void loopTest() {
    final Type type = new TypeToken<ClassA>() {
    }.getType();
    final TypeDescription typeDescription = JsonSchemaGenerator.generateTypeDescription(type,
        ClassA.class);
    for (final TypeDescription d : typeDescription.getReferencedTypeDescriptions().values()) {
      System.out.println(d.getTypeName());
    }
  }

  @Test
  void test() {
    JsonSchemaGenerator.getAllFields(ObjectTop2.class);
  }

}
