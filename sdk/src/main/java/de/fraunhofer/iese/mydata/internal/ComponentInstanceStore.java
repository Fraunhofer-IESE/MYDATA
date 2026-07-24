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

package de.fraunhofer.iese.mydata.internal;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @see IComponentInstanceStore
 */
public final class ComponentInstanceStore implements IComponentInstanceStore {
  private final Map<ComponentId, IPolicyInformationPoint> pips;
  private final Map<ComponentId, IPolicyExecutionPoint> pxps;

  public ComponentInstanceStore() {
    this.pips = new ConcurrentHashMap<>();
    this.pxps = new ConcurrentHashMap<>();
  }


  @Override
  public void addPipInstance(ComponentId componentId, IPolicyInformationPoint instance) throws InvalidEntityException, ConflictingResourceException {
    MyDataEntity.validateAndNullCheck(componentId);
    Objects.requireNonNull(instance);
    if (pips.putIfAbsent(componentId, instance) != null) {
      throw new ConflictingResourceException("Pip with id " + componentId.getUrn() + " already registered.");
    }
  }

  @Override
  public void addPxpInstance(ComponentId componentId, IPolicyExecutionPoint instance) throws InvalidEntityException, ConflictingResourceException {
    MyDataEntity.validateAndNullCheck(componentId);
    Objects.requireNonNull(instance);
    if (pxps.putIfAbsent(componentId, instance) != null) {
      throw new ConflictingResourceException("Pxp with id " + componentId.getUrn() + " already registered.");
    }
  }

  @Override
  public void removePipInstance(ComponentId componentId) {
    pips.remove(componentId);
  }

  @Override
  public void removePxpInstance(ComponentId componentId) {
    pxps.remove(componentId);
  }

  @Override
  public Optional<IPolicyInformationPoint> getPipInstanceByComponentId(ComponentId componentId) {
    return Optional.ofNullable(pips.get(componentId));
  }

  @Override
  public Optional<IPolicyExecutionPoint> getPxpInstanceByComponentId(ComponentId componentId) {
    return Optional.ofNullable(pxps.get(componentId));
  }

  @Override
  public void clear() {
    this.pips.clear();
    this.pxps.clear();
  }


}
