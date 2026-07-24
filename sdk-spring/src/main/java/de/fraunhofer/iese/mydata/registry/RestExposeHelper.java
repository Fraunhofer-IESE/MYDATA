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

package de.fraunhofer.iese.mydata.registry;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.interfaces.IMyDataComponent;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the mapping Path --> componentId and retrieves Health-Information from {@link IMyDataComponent} instances
 */
@Component
@ConditionalOnProperty(prefix = "mydata", name = "external-server-url") // TODO maybe bind to the "operational mode"?
public class RestExposeHelper {
  private static final Logger LOG = LoggerFactory.getLogger(RestExposeHelper.class);
  private final Map<String, ComponentId> pipPathToComponentId = new HashMap<>();
  private final Map<String, ComponentId> pxpPathToComponentId = new HashMap<>();

  /**
   * Retrieve the componentId of PIP associated with the given path
   *
   * @param path the path
   * @return the componentId associated with that path or null
   */
  public Optional<ComponentId> getPipComponentIdForPath(final String path) {
    return Optional.ofNullable(this.pipPathToComponentId.get(path));
  }

  /**
   * Retrieve the componentId of PXP associated with the given path
   *
   * @param path the path
   * @return the componentId associated with that path or null
   */
  public Optional<ComponentId> getPxpComponentIdForPath(final String path) {
    return Optional.ofNullable(this.pxpPathToComponentId.get(path));
  }

  /**
   * Registers a PIP componentId to a path
   *
   * @param path the path
   * @param id   the componentId
   * @throws ConflictingResourceException when there is already a mapping for that path
   */
  void registerPipComponentIdToPath(final String path, final ComponentId id) throws ConflictingResourceException {
    if (this.pipPathToComponentId.containsKey(path)) {
      throw new ConflictingResourceException(
          String.format("Cannot register PIP component %s to path '%s' as the component %s is already registered to that path",
              id, path, this.pipPathToComponentId.get(path))
      );
    }
    this.pipPathToComponentId.put(path, id);
  }

  /**
   * Registers a PXP componentId to a path
   *
   * @param path the path
   * @param id   the componentId
   * @throws ConflictingResourceException when there is already a mapping for that path
   */
  void registerPxpComponentIdToPath(final String path, final ComponentId id) throws ConflictingResourceException {
    if (this.pxpPathToComponentId.containsKey(path)) {
      throw new ConflictingResourceException(
          String.format("Cannot register PXP component %s to path '%s' as the component %s is already registered to that path",
              id, path, this.pxpPathToComponentId.get(path))
      );
    }
    this.pxpPathToComponentId.put(path, id);
  }
}
