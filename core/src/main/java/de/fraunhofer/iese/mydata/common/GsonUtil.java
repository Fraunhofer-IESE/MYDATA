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

package de.fraunhofer.iese.mydata.common;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.common.serdes.ClientVersionSerDes;
import de.fraunhofer.iese.mydata.common.serdes.ComponentIdHealthStatusMapSerDes;
import de.fraunhofer.iese.mydata.common.serdes.DataObjectSerDes;
import de.fraunhofer.iese.mydata.common.serdes.PolicyVersionSerDes;
import de.fraunhofer.iese.mydata.common.serdes.TimerVersionSerDes;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.timer.TimerId;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

class GsonUtil {
  private static final ExclusionStrategy HIDE_EXCLUSION = new ExclusionStrategy() {
    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
      return false;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
      final Hide hide = fieldAttributes.getAnnotation(Hide.class);
      return hide != null;
    }
  };

  private GsonUtil() {
  }

  static Gson createDefaultGson() {
    return new GsonBuilder().addSerializationExclusionStrategy(HIDE_EXCLUSION)
        .registerTypeAdapter(DataObject.class, new DataObjectSerDes.DataObjectJsonDeserializer())
        .registerTypeAdapter(DataObject.class, new DataObjectSerDes.DataObjectJsonSerializer())
        .registerTypeAdapter(new TypeToken<Map<PolicyId, Long>>() {
        }.getType(), new PolicyVersionSerDes.PolicyVersionDeserializer())
        .registerTypeAdapter(new TypeToken<Map<PolicyId, Long>>() {
        }.getType(), new PolicyVersionSerDes.PolicyVersionSerializer())
        .registerTypeAdapter(new TypeToken<Map<TimerId, Long>>() {
        }.getType(), new TimerVersionSerDes.TimerVersionDeserializer())
        .registerTypeAdapter(new TypeToken<Map<TimerId, Long>>() {
        }.getType(), new TimerVersionSerDes.TimerVersionSerializer())
        .registerTypeAdapter(new TypeToken<Map<ClientId, Long>>() {
        }.getType(), new ClientVersionSerDes.TimerVersionSerializer())
        .registerTypeAdapter(new TypeToken<Map<ClientId, Long>>() {
        }.getType(), new ClientVersionSerDes.TimerVersionDeserializer())
        .registerTypeAdapter(new TypeToken<Map<ComponentId, HealthStatus>>() {
        }.getType(), new ComponentIdHealthStatusMapSerDes.ComponentIdHealthStatusMapSerializer())
        .registerTypeAdapter(new TypeToken<Map<ComponentId, HealthStatus>>() {
        }.getType(), new ComponentIdHealthStatusMapSerDes.ComponentIdHealthStatusMapDeserializer())
        .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).create();
  }
}
