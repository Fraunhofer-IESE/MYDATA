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

package de.fraunhofer.iese.mydata.policy.parameter;

import de.fraunhofer.iese.mydata.policy.decision.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The Class ModifierList.
 */
public class ModifierList extends ArrayList<Modifier> {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 7913615319509313890L;

  /**
   * Instantiates a new modifier list.
   */
  public ModifierList() {
    super();
  }

  /**
   * Instantiates a new modifier list.
   *
   * @param params the params
   */
  public ModifierList(Collection<? extends Modifier> params) {
    super();
    if (params != null) {
      this.addAll(params);
    }
  }

  /**
   * Instantiates a new modifier list.
   *
   * @param params the params
   */
  public ModifierList(Modifier... params) {
    super();
    if (params != null) {
      this.addAll(Arrays.asList(params));
    }
  }

  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#add(java.lang.Object)
   */
  @Override
  public boolean add(Modifier e) {
    if (this.contains(e)) {
      return false;
    } else {
      return super.add(e);
    }
  }

  /**
   * Adds the parameter.
   *
   * @param <T>  the generic type
   * @param name the name
   */
  public <T> void addParameter(String name) {
    this.add(new Modifier(name, null));
  }

  /**
   * Gets the parameter for name.
   *
   * @param  name the name
   * @return      the parameter for name
   */
  public Modifier getParameterForName(String name) {
    if (name != null) {
      for (final Modifier param : this) {
        if (name.equals(param.getName())) {
          return param;
        }
      }
    }
    return null;
  }

  /**
   * Removes the parameter.
   *
   * @param name the name
   */
  public void removeParameter(String name) {
    this.remove(this.getParameterForName(name));
  }

  /**
   * Sets the parameters.
   *
   * @param params the new parameters
   */
  public void setParameters(ModifierList params) {
    if (params != null) {
      this.clear();
    }
    this.addAll(params);
  }

  /*
   * (non-Javadoc)
   * @see java.util.ArrayList#toArray()
   */
  @Override
  public Modifier[] toArray() {
    return this.toArray(new Modifier[this.size()]);
  }

}
