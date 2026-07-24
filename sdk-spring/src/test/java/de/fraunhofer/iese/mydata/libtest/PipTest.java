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

package de.fraunhofer.iese.mydata.libtest;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.MyDataEnvironmentManager;
import de.fraunhofer.iese.mydata.eventhistory.EnableEventHistory;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.util.MyDataUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ActiveProfiles("test")
@EnableEventHistory
@Transactional
@ComponentScan
@DirtiesContext
public class PipTest {
  // TODO maybe we want to relocate this test

  @Autowired
  private IMyDataEnvironment myDataEnvironment;

  @Autowired
  private LibTestPep libTestPep;

  @BeforeClass
  public static void testSetup() {
    MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments();
  }

  @AfterClass
  public static void testTeardown() {
    MyDataEnvironmentManager.disableOverwritingOfExistingMyDataEnvironments();
  }

  @Test
  public void pipTest1() {
    final String pipTest1Text = "pipTest1Text";
    Assert.assertNotNull(this.myDataEnvironment);
    Assert.assertNotNull(this.libTestPep);
    final PolicyId policyId = new PolicyId("urn:policy:test:pip-test-1");
    final Policy pipTestPolicy1 = new Policy(TestPolicies.PIP_TEST_POLICY_1);
    try {
      final PolicyId pipTestPolicy1Id = this.myDataEnvironment.getPmp().addPolicy(pipTestPolicy1);
      Assert.assertEquals(policyId, pipTestPolicy1Id);
      this.myDataEnvironment.getPmp().deployPolicy(pipTestPolicy1Id);
      try {
        final Event event = MyDataUtil.checkedBlockingGet(this.libTestPep.pipTest1(pipTest1Text));
        final String pipTest1ReturnedText = event.getParameterValue("text", String.class);
        Assert.assertEquals(pipTest1Text, pipTest1ReturnedText);
      } catch (InhibitException | EvaluationUndecidableException e) {
        Assert.fail();
      }

    } catch (IOException | ConflictingResourceException | ResourceUpdateException
        | InvalidEntityException | NoSuchEntityException e) {
      Assert.fail();
    }

  }

  @Test
  public void pipTest2() {
    final String pipTest2Text = "pipTest2Text";
    Assert.assertNotNull(this.myDataEnvironment);
    Assert.assertNotNull(this.libTestPep);
    final PolicyId policyId = new PolicyId("urn:policy:test:pip-test-2");
    final Policy pipTestPolicy2 = new Policy(TestPolicies.PIP_TEST_POLICY_2);
    try {
      final PolicyId pipTestPolicy2Id = this.myDataEnvironment.getPmp().addPolicy(pipTestPolicy2);
      Assert.assertEquals(policyId, pipTestPolicy2Id);
      this.myDataEnvironment.getPmp().deployPolicy(pipTestPolicy2Id);
      try {
        final Event event = MyDataUtil.checkedBlockingGet(this.libTestPep.pipTest2(pipTest2Text));
        final String pipTest2ReturnedText = event.getParameterValue("text", String.class);
        Assert.assertNotEquals(pipTest2Text, pipTest2ReturnedText);
      } catch (InhibitException | EvaluationUndecidableException e) {
        Assert.assertTrue(e instanceof InhibitException);
      }

    } catch (IOException | ConflictingResourceException | ResourceUpdateException
        | InvalidEntityException | NoSuchEntityException e) {
      Assert.fail();
    }

  }

}
