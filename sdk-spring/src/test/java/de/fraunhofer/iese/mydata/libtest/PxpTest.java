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
public class PxpTest {
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
  public void pxpTest1() throws InterruptedException {
    final String pxpTest1Text = "pxpTest1Text";
    Assert.assertNotNull(this.myDataEnvironment);
    Assert.assertNotNull(this.libTestPep);
    final PolicyId policyId = new PolicyId("urn:policy:test:pxp-test-1");
    final Policy pxpTestPolicy1 = new Policy(TestPolicies.PXP_TEST_POLICY_1);

    try {
      if (null != this.myDataEnvironment.getPmp().getPolicy(policyId)) {
        if (this.myDataEnvironment.getPmp().getPolicy(policyId).isDeployed()) {
          this.myDataEnvironment.getPmp().revokePolicy(policyId);
        }
        this.myDataEnvironment.getPmp().deletePolicy(policyId);
      }
    } catch (IOException | NoSuchEntityException | InvalidEntityException | ResourceUpdateException
        | ConflictingResourceException e1) {
    }
    try {
      final PolicyId pxpTestPolicy1Id = this.myDataEnvironment.getPmp().addPolicy(pxpTestPolicy1);
      Assert.assertEquals(policyId, pxpTestPolicy1Id);
      this.myDataEnvironment.getPmp().deployPolicy(pxpTestPolicy1Id);
      try {
        final Event event = MyDataUtil.checkedBlockingGet(this.libTestPep.pxpTest1(pxpTest1Text));
        final String pxpTest1ReturnedText = event.getParameterValue("text", String.class);
        Assert.assertEquals(pxpTest1Text, pxpTest1ReturnedText);
      } catch (InhibitException | EvaluationUndecidableException e) {
        Assert.fail();
      }

    } catch (IOException | ConflictingResourceException | ResourceUpdateException
        | InvalidEntityException | NoSuchEntityException e) {
      Assert.fail();
    }
    System.out.println("before sleep");
    Thread.sleep(1000);
  }

  @Test
  public void pxpTest2() throws InterruptedException {
    final String pxpTest2Text = "pxpTest2Text";
    Assert.assertNotNull(this.myDataEnvironment);
    Assert.assertNotNull(this.libTestPep);
    final PolicyId policyId = new PolicyId("urn:policy:test:pxp-test-2");
    final Policy pxpTestPolicy2 = new Policy(TestPolicies.PXP_TEST_POLICY_2);
    try {
      if (null != this.myDataEnvironment.getPmp().getPolicy(policyId)) {
        if (this.myDataEnvironment.getPmp().getPolicy(policyId).isDeployed()) {
          this.myDataEnvironment.getPmp().revokePolicy(policyId);
        }
        this.myDataEnvironment.getPmp().deletePolicy(policyId);
      }

    } catch (IOException | ConflictingResourceException | ResourceUpdateException
        | InvalidEntityException | NoSuchEntityException e) {
    }
    try {
      final PolicyId pxpTestPolicy2Id = this.myDataEnvironment.getPmp().addPolicy(pxpTestPolicy2);
      Assert.assertEquals(policyId, pxpTestPolicy2Id);
      this.myDataEnvironment.getPmp().deployPolicy(pxpTestPolicy2Id);
      try {
        final Event event = MyDataUtil.checkedBlockingGet(this.libTestPep.pxpTest2(pxpTest2Text));
        final String pxpTest2ReturnedText = event.getParameterValue("text", String.class);
        Assert.assertNotEquals(pxpTest2Text, pxpTest2ReturnedText);
      } catch (InhibitException | EvaluationUndecidableException e) {
        Assert.assertTrue(e instanceof InhibitException);
      }
    } catch (IOException | ConflictingResourceException | ResourceUpdateException
        | InvalidEntityException | NoSuchEntityException e) {
      Assert.fail();
    }
    System.out.println("before sleep");
    Thread.sleep(1000);

  }
}
