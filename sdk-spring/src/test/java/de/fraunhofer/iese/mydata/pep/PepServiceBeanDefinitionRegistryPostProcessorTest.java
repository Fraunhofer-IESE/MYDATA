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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;

@RunWith(SpringRunner.class)
public class PepServiceBeanDefinitionRegistryPostProcessorTest {

  public static final String CONFIGURATION1_BEAN = "configuration1";

  private PepServiceBeanDefinitionRegistryPostProcessor testCandidate;

  private PepCandidateComponentProvider pepCandidateComponentProvider;

  private ConfigurableListableBeanFactory context;

  @Before
  public void setupTest() {
    this.context = Mockito.mock(ConfigurableListableBeanFactory.class, Mockito.withSettings().extraInterfaces(BeanDefinitionRegistry.class));
    this.pepCandidateComponentProvider = Mockito.mock(PepCandidateComponentProvider.class);
    this.testCandidate = new PepServiceBeanDefinitionRegistryPostProcessor(pepCandidateComponentProvider);
  }

  @Test
  public void oneBeanFound_ShouldAddOneBeanDefintion() {
    assertSearchForAnnotationAndReturnOneBeanName();
    BeanDefinition beanDefinition = assertScanAndReturn1Bean("some");
    testCandidate.postProcessBeanFactory(context);
    assertEquals("pepFactory", beanDefinition.getFactoryBeanName());
    assertEquals("createPep", beanDefinition.getFactoryMethodName());
    verify((BeanDefinitionRegistry) context).registerBeanDefinition(String.class.getName(), beanDefinition);
  }

  @Test(expected = FatalBeanException.class)
  public void classNotFound_ShouldWrappedToFatalBeanException() {
    assertSearchForAnnotationAndReturnOneBeanName();
    assertScanAndReturnBeanWithNotExistingClass();
    testCandidate.postProcessBeanFactory(context);

  }

  @Test
  public void basePackageClasses_ShouldDiscoverTheirPackages() {
    assertSearchForAnnotationAndReturnTestConfigurationBasePackageClasses();
    BeanDefinition beanDefinition = assertScanAndReturn1Bean(TestConfigurationBasePackageClasses.class.getPackage().getName());
    testCandidate.postProcessBeanFactory(context);
    assertEquals("pepFactory", beanDefinition.getFactoryBeanName());
    assertEquals("createPep", beanDefinition.getFactoryMethodName());
    verify((BeanDefinitionRegistry) context).registerBeanDefinition(String.class.getName(), beanDefinition);
  }

  @Test(expected = IllegalArgumentException.class)
  public void noPackageToScan_ThrowsException() {
    assertSearchForAnnotationAndReturnTestConfigurationWithoutPackages();
    testCandidate.postProcessBeanFactory(context);
  }


  private BeanDefinition assertScanAndReturn1Bean(String basePackage) {
    final BeanDefinition genericBeanDefinition = new GenericBeanDefinition();
    genericBeanDefinition.setBeanClassName(String.class.getName());
    final Set<BeanDefinition> beanDefinitions = Collections.singleton(genericBeanDefinition);
    when(pepCandidateComponentProvider.findCandidateComponents(basePackage)).thenReturn(beanDefinitions);
    return genericBeanDefinition;
  }


  private BeanDefinition assertScanAndReturnBeanWithNotExistingClass() {
    final BeanDefinition genericBeanDefinition = new GenericBeanDefinition();
    genericBeanDefinition.setBeanClassName("SomeclassName");
    final Set<BeanDefinition> beanDefinitions = Collections.singleton(genericBeanDefinition);
    when(pepCandidateComponentProvider.findCandidateComponents("some")).thenReturn(beanDefinitions);
    return genericBeanDefinition;
  }

  private void assertSearchForAnnotationAndReturnOneBeanName() {
    when(context.getBeanNamesForAnnotation(EnablePolicyEnforcementPoint.class)).thenReturn(new String[]{CONFIGURATION1_BEAN});
    BeanDefinition definition = new GenericBeanDefinition();
    definition.setBeanClassName(TestConfiguration.class.getName());
    when(context.getBeanDefinition(CONFIGURATION1_BEAN)).thenReturn(definition);
  }


  private void assertSearchForAnnotationAndReturnTestConfigurationBasePackageClasses() {
    when(context.getBeanNamesForAnnotation(EnablePolicyEnforcementPoint.class)).thenReturn(new String[]{CONFIGURATION1_BEAN});
    BeanDefinition definition = new GenericBeanDefinition();
    definition.setBeanClassName(TestConfigurationBasePackageClasses.class.getName());
    when(context.getBeanDefinition(CONFIGURATION1_BEAN)).thenReturn(definition);
  }

  private void assertSearchForAnnotationAndReturnTestConfigurationWithoutPackages() {
    when(context.getBeanNamesForAnnotation(EnablePolicyEnforcementPoint.class)).thenReturn(new String[]{CONFIGURATION1_BEAN});
    BeanDefinition definition = new GenericBeanDefinition();
    definition.setBeanClassName(TestConfigurationWithoutPackageInfo.class.getName());
    when(context.getBeanDefinition(CONFIGURATION1_BEAN)).thenReturn(definition);
  }

}
