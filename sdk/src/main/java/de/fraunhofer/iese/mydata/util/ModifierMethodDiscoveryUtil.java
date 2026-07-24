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

package de.fraunhofer.iese.mydata.util;

import de.fraunhofer.iese.mydata.pep.common.ModifierMethod;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModifierMethodDiscoveryUtil {

  private static final class AllModifierMethodImplementationsHolder {
    private static final Set<Class<? extends ModifierMethod>> instance;

    static {
      final ConfigurationBuilder config = new ConfigurationBuilder();
      config.setUrls(ClasspathHelper.forClassLoader()).addUrls(ClasspathHelper.forJavaClassPath());
      config.setScanners(Scanners.SubTypes);
      final Reflections reflections = new Reflections(config);
      final Set<Class<? extends ModifierMethod>> classes = new HashSet<>(reflections.getSubTypesOf(ModifierMethod.class));
      classes.removeIf(c -> c.isInterface() || java.lang.reflect.Modifier.isAbstract(c.getModifiers()));
      instance = Collections.unmodifiableSet(classes);
    }
  }

  private ModifierMethodDiscoveryUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static Set<Class<? extends ModifierMethod>> findAll() {
    return AllModifierMethodImplementationsHolder.instance;
  }

  public static Set<Class<? extends ModifierMethod>> findInPackages(final Set<String> packageNames) {
    if (packageNames.isEmpty()) {
      return Collections.emptySet();
    } else {
      return AllModifierMethodImplementationsHolder.instance.stream()
          .filter(c -> packageNames.stream().anyMatch(pkg -> c.getPackageName().startsWith(pkg)))
          .collect(Collectors.toSet());
    }
  }
}
