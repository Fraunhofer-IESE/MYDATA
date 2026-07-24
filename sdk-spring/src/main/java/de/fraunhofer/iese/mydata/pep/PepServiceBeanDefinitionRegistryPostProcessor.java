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

import de.fraunhofer.iese.mydata.reactive.common.PepServiceDescription;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PostProcessor that creates and adds a BeanDefinition for each Interface that is
 * annotated with
 * {@link de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription}.
 * <p>
 * Like Spring Data Repository scanning this PostProcessor scans all the packages that are defined by
 * {@link EnablePolicyEnforcementPoint#basePackages()} or
 * {@link EnablePolicyEnforcementPoint#basePackageClasses()}. For each interface
 * found it creates an BeanDefinition. The Name of the interface is assigned as its bean-name. As
 * the interface has no implementing the beanfactory and factorymethod are set
 * at the BeanDefintion.
 * </p>
 * See {@link PepBeanFactory} for information about how the bean is created.
 */
public class PepServiceBeanDefinitionRegistryPostProcessor implements BeanFactoryPostProcessor {

  private final ClassPathScanningCandidateComponentProvider pepCandidateComponentProvider;

  /**
   * Constructor adds Anntoationfilter to the scanner which is reponsible to
   * find all Interfaces with {@link PepServiceDescription}.
   */
  public PepServiceBeanDefinitionRegistryPostProcessor(final PepCandidateComponentProvider pepCandidateComponentProvider) {
    this.pepCandidateComponentProvider = pepCandidateComponentProvider;
  }


  /**
   * Starting Point for Postprocessing see class level documentation.
   *
   * @param configurableListableBeanFactory
   * @throws BeansException
   * @see BeanFactoryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory)
   */
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) {

    final String[] beanNamesForAnnotation = configurableListableBeanFactory.getBeanNamesForAnnotation(EnablePolicyEnforcementPoint.class);
    Arrays.stream(beanNamesForAnnotation).forEach(beanName -> {
      try {
        this.handleEnablePolicyEnforcementPoint(configurableListableBeanFactory, beanName);
      } catch (ClassNotFoundException e) {
        throw new FatalBeanException("Cannot create Bean" + beanName, e);
      }
    });
  }

  private void handleEnablePolicyEnforcementPoint(ConfigurableListableBeanFactory configurableListableBeanFactory, String beanName) throws ClassNotFoundException {
    final BeanDefinition beanDefinition = configurableListableBeanFactory.getBeanDefinition(beanName);
    final String beanClassName = beanDefinition.getBeanClassName();

    final EnablePolicyEnforcementPoint annotation = Class.forName(beanClassName).getAnnotation(EnablePolicyEnforcementPoint.class);
    final String[] basePackages = this.getBasePackages(annotation);
    for (String basePackage : basePackages) {
      this.handlePackage((BeanDefinitionRegistry) configurableListableBeanFactory, basePackage);
    }
  }

  /**
   * Scans all Interfaces that contains PepInterfaceDescription in the given
   * package (or subpackages). And registers a bean for each.
   *
   * @param configurableListableBeanFactory For registration.
   * @param basePackage                     The packaage to scan for pep interfaces.
   * @throws ClassNotFoundException
   */
  private void handlePackage(BeanDefinitionRegistry configurableListableBeanFactory, String basePackage) throws ClassNotFoundException {
    final Set<BeanDefinition> candidateComponents = this.pepCandidateComponentProvider.findCandidateComponents(basePackage);
    for (BeanDefinition candidateComponent : candidateComponents) {
      this.handleBeanDefintion(candidateComponent);
      this.registerBean(configurableListableBeanFactory, candidateComponent);
    }
  }

  /**
   * Adss the beandefinition to the beanfactory.
   *
   * @param configurableListableBeanFactory BeanFactory
   * @param candidateComponent              Beandefintion to register.
   */
  private void registerBean(BeanDefinitionRegistry configurableListableBeanFactory, BeanDefinition candidateComponent) {
    configurableListableBeanFactory.registerBeanDefinition(Objects.requireNonNull(candidateComponent.getBeanClassName()), candidateComponent);
  }

  /**
   * Extends the beandefinition by setting the factory and factorymethod, as
   * {@link PepBeanFactory} is creating the implementation (Proxy) for the PEP
   * Interface.
   *
   * @param candidateComponent beandefinition to extend.
   * @throws ClassNotFoundException
   */
  private void handleBeanDefintion(BeanDefinition candidateComponent) throws ClassNotFoundException {
    // on next cold observable so only one object
    candidateComponent.setFactoryBeanName("pepFactory");
    candidateComponent.setFactoryMethodName("createPep");
    candidateComponent.getConstructorArgumentValues().addIndexedArgumentValue(0, Class.forName(candidateComponent.getBeanClassName()));

  }

  /**
   * As {@link EnablePolicyEnforcementPoint} provides to attributes to define
   * the packages, this helper Method takes the choosen.
   *
   * @param annotation Annotation that contains the package infos
   * @return List of packages that should be scanned.
   * @throws IllegalArgumentException if no package info is provided.
   */
  private String[] getBasePackages(EnablePolicyEnforcementPoint annotation) {
    if (annotation.basePackages().length > 0)
      return annotation.basePackages();
    else {
      if (annotation.basePackageClasses().length > 0) {
        return Arrays.stream(annotation.basePackageClasses()).map(clazz -> clazz.getPackage().getName()).collect(Collectors.toList()).toArray(new String[]{});
      } else {
        throw new IllegalArgumentException("Either provide basePackages or basePackageClasses" + annotation);
      }
    }

  }
}
