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

package de.fraunhofer.iese.mydata.pep;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.exception.InitializationException;
import de.fraunhofer.iese.mydata.pep.pepinterfaces.SingletonAction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;

/**
 * Test for {@link PepBeanFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PepBeanFactoryTest {

  private static final Logger LOG = LoggerFactory.getLogger(PepBeanFactoryTest.class);

  private PepBeanFactory testCandidate;

  private IMyDataEnvironment myDataEnvironment;

  @Before
  public void setupTest() {
    this.myDataEnvironment = Mockito.mock(IMyDataEnvironment.class);
    this.testCandidate = new PepBeanFactory(this.myDataEnvironment);
  }

  @Test
  public void createColdPep() throws InitializationException {
    when(this.myDataEnvironment.constructAndRegisterCustomPep("singleton", SingletonAction.class))
        .thenReturn(Mockito.mock(SingletonAction.class));
    assertNotNull(this.testCandidate.createPep(SingletonAction.class));
    verify(this.myDataEnvironment).constructAndRegisterCustomPep("singleton",
        SingletonAction.class);
  }

  @Test(expected = BeanCreationException.class)
  public void createColdPepFail() throws InitializationException {
    when(this.myDataEnvironment.constructAndRegisterCustomPep("singleton", SingletonAction.class))
        .thenThrow(InitializationException.class);
    this.testCandidate.createPep(SingletonAction.class);
  }
}
