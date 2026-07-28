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

package de.fraunhofer.iese.mydata.policy.decision;

import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * A modifier describes a method that can be performed on events. Despite a key and value, it also
 * contains an engine.
 */
// TODO change the name to EventParameter as PDP
@Getter
public class Modifier extends MyDataEntity {

  /**
   * The name.
   */
  @NotBlank
  private final String name;

  /**
   * The JsonPath expression.
   */
  @Setter
  @Nullable
  private String expression;

  /**
   * An Engine used to modify the parameter value.
   */
  private List<@Valid ModifierEngine> engine;

  /**
   * Instantiates a new modifier.
   *
   * @param name the name
   */
  public Modifier(String name) {
    this.name = name;
  }

  /**
   * Instantiates a new modifier.
   *
   * @param name   the name of the parameter
   * @param engine the engine to be used
   */
  public Modifier(String name, List<ModifierEngine> engine) {
    this.name = name;
    this.engine = engine;
  }

  /**
   * Adds the engine.
   *
   * @param engine the engine to set
   */
  public void addEngine(ModifierEngine engine) {
    if (this.engine == null) {
      this.engine = new ArrayList<>();
    }
    this.engine.add(engine);
  }

}
