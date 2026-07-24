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

import static org.junit.jupiter.api.Assertions.assertNull;

import de.fraunhofer.iese.mydata.DataFactory;
import de.fraunhofer.iese.mydata.pep.modifiers.basic.DeleteModifierMethod;
import de.fraunhofer.iese.mydata.pep.testdata.model.Project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Tests for the delete modifier
 */
public class DeleteModifierMethodTest {

  private DocumentContext documentContext;

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @BeforeEach
  public void initialize() {
    this.documentContext = JsonPath.parse(new Gson().toJson(DataFactory.getProjects()));
  }

  @Test
  public void deleteProjectLeader() {
    final DeleteModifierMethod modifierMethod = new DeleteModifierMethod();
    final DocumentContext modifiedObject = modifierMethod.doModification(this.documentContext,
        "$..projectLeader", null);
    final Type type = new TypeToken<List<Project>>() {
    }.getType();
    final List<Project> projects = this.gson.fromJson(this.documentContext.read("$").toString(),
        type);
    projects.forEach(project -> assertNull(project.getProjectLeader()));
  }
}
