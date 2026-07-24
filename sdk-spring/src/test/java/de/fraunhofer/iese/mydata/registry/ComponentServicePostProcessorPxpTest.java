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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.OperationalMode;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

/**
 * Test for {@link ComponentServicePostProcessor}
 */
@RunWith(MockitoJUnitRunner.class)
public class ComponentServicePostProcessorPxpTest {

  private static final String BEAN_NAME_1 = "bean1";

  private final DummyPxpService service = new DummyPxpService();

  private ComponentServicePostProcessor testCandidate;

  @Mock
  private ApplicationContext context;

  @Mock
  private IMyDataEnvironment myDataEnvironment;

  @Mock
  private RestExposeHelper restExposeHelper;

  @Before
  public void prepareTestCandidate() {
    this.testCandidate = new ComponentServicePostProcessor(ComponentType.PXP, TestService.class,
        this.myDataEnvironment, "http://localhost:8081", "my-context", "my-prefix",
        this.restExposeHelper);
    when(this.myDataEnvironment.getSolutionId()).thenReturn(new SolutionId("urn:solution:test"));
  }

  @Test
  public void givenWeAreLocal_noBeanWithAnnotationExists() throws InvalidEntityException,
      ResourceUpdateException, ConflictingResourceException, IOException, NoSuchEntityException {
    when(this.context.getBeanNamesForAnnotation(TestService.class)).thenReturn(new String[] {});
    this.testCandidate.onApplicationEvent(new ContextRefreshedEvent(this.context));
    Mockito.verify(this.myDataEnvironment, times(0)).registerLocalPxp(any(ComponentId.class),
        any());
  }

  private void returnOneServiceBean(Object toReturn) {
    when(this.context.getBeanNamesForAnnotation(TestService.class)).thenReturn(new String[] {
        BEAN_NAME_1
    });
    when(this.context.getBean(BEAN_NAME_1)).thenReturn(toReturn);
  }

  @Test
  public void givenWeAreLocal_whenServiceHasActionMethods_registrationShouldHappen()
      throws Exception {
    doReturn(OperationalMode.LOCAL).when(this.myDataEnvironment).getOperationalMode();
    this.returnOneServiceBean(this.service);
    this.testCandidate.onApplicationEvent(new ContextRefreshedEvent(this.context));
    verify(this.myDataEnvironment).registerLocalPxp(new ComponentId("urn:component:test:pxp:1234"),
        this.service);
  }

  @Test
  public void givenWeAreCloud_whenServiceHasActionMethods_registrationShouldHappen()
      throws Exception {
    doReturn(OperationalMode.CLOUD).when(this.myDataEnvironment).getOperationalMode();
    this.returnOneServiceBean(this.service);
    this.testCandidate.onApplicationEvent(new ContextRefreshedEvent(this.context));
    verify(this.myDataEnvironment, times(0)).registerLocalPxp(
        ArgumentMatchers.any(ComponentId.class), ArgumentMatchers.any(Object.class));
    verify(this.myDataEnvironment, times(1)).registerManagedPxp(
        new ComponentId("urn:component:test:pxp:1234"), this.service, Collections.singletonList(URI
            .create("http://localhost:8081/my-context/my-prefix/pxp/urn:component:test:pxp:1234")));
    verify(this.restExposeHelper, times(1)).registerPxpComponentIdToPath(
        "urn:component:test:pxp:1234", new ComponentId("urn:component:test:pxp:1234"));
  }

}
