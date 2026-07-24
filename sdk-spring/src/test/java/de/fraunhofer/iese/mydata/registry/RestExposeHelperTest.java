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
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
public class RestExposeHelperTest {

  private RestExposeHelper restExposeHelper = new RestExposeHelper();

  @Test
  public void getPipComponentIdForPath_withNonExistingMapping() {
    Assert.assertFalse(restExposeHelper.getPipComponentIdForPath("bla").isPresent());
  }

  @Test
  public void getPxpComponentIdForPath_withNonExistingMapping() {
    Assert.assertFalse(restExposeHelper.getPxpComponentIdForPath("bla").isPresent());
  }

  @Test
  public void getPipComponentIdForPath_withExistingMapping() throws ConflictingResourceException {
    final ComponentId componentId = new ComponentId("urn:component:test:pip:123");
    final String path = "bla";
    restExposeHelper.registerPipComponentIdToPath(path, componentId);
    Optional<ComponentId> componentIdOptional = restExposeHelper.getPipComponentIdForPath(path);
    Assert.assertTrue(componentIdOptional.isPresent());
    Assert.assertEquals(componentId, componentIdOptional.get());
  }

  @Test
  public void getPxpComponentIdForPath_withExistingMapping() throws ConflictingResourceException {
    final ComponentId componentId = new ComponentId("urn:component:test:pxp:123");
    final String path = "bla";
    restExposeHelper.registerPxpComponentIdToPath(path, componentId);
    Optional<ComponentId> componentIdOptional = restExposeHelper.getPxpComponentIdForPath(path);
    Assert.assertTrue(componentIdOptional.isPresent());
    Assert.assertEquals(componentId, componentIdOptional.get());
  }

  @Test(expected = ConflictingResourceException.class)
  public void duplicateRegistrationOfPipToPathResultsInException() throws ConflictingResourceException {
    final ComponentId componentId = new ComponentId("urn:component:test:pip:123");
    final String path = "bla";
    restExposeHelper.registerPipComponentIdToPath(path, componentId);
    restExposeHelper.registerPipComponentIdToPath(path, componentId);
  }

  @Test(expected = ConflictingResourceException.class)
  public void duplicateRegistrationOfPxpToPathResultsInException() throws ConflictingResourceException {
    final ComponentId componentId = new ComponentId("urn:component:test:pxp:123");
    final String path = "bla";
    restExposeHelper.registerPxpComponentIdToPath(path, componentId);
    restExposeHelper.registerPxpComponentIdToPath(path, componentId);
  }

}
