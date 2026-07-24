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

package de.fraunhofer.iese.mydata.autoconfig;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.MyDataEnvironmentManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Test for config.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
public class AutoconfigWithoutEventHistoryTest {

  @Autowired
  private EntityManagerFactory emf1;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private PlatformTransactionManager transactionManager1;

  @Autowired
  private IMyDataEnvironment defaultMyDataEnvironment;

  @BeforeClass
  public static void testSetup() {
    MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments();
  }

  @AfterClass
  public static void testTeardown() {
    MyDataEnvironmentManager.disableOverwritingOfExistingMyDataEnvironments();
  }

  @Test
  public void checkAutowiredMyDataEnvironment() {
    Assert.assertEquals(MyDataEnvironmentManager.DEFAULT_ENVIRONMENT_ID,
        this.defaultMyDataEnvironment.getEnvironmentId());
    Assert.assertNotNull(this.defaultMyDataEnvironment.getPmp());
  }

  @Test
  public void checkDefaultMyDataEnvironment() {
    final IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
    Assert.assertNotNull(myDataEnvironment);
    Assert.assertEquals(MyDataEnvironmentManager.DEFAULT_ENVIRONMENT_ID,
        myDataEnvironment.getEnvironmentId());
    Assert.assertNotNull(myDataEnvironment.getPmp());
  }

  @Test
  public void testDataSourceAvailable() {
    Assert.assertNotNull(this.entityManager);
    Assert.assertNotNull(this.emf1);
    Assert.assertNotNull(this.transactionManager1);
  }
}
