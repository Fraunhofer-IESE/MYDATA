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

package de.fraunhofer.iese.mydata.common.serdes;

import de.fraunhofer.iese.mydata.timer.TimerId;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TimerVersionSerDes {

  public static class TimerVersionSerializer implements JsonSerializer<Map<TimerId, Long>> {

    @Override
    public JsonElement serialize(Map<TimerId, Long> src, Type typeOfSrc,
        JsonSerializationContext context) {
      final JsonObject jsonObject = new JsonObject();
      for (final Map.Entry<TimerId, Long> entry : src.entrySet()) {
        jsonObject.addProperty(entry.getKey().getUrn(), entry.getValue());
      }
      return jsonObject;
    }
  }

  public static class TimerVersionDeserializer implements JsonDeserializer<Map<TimerId, Long>> {

    @Override
    public Map<TimerId, Long> deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      final JsonObject mapObject = json.getAsJsonObject();
      final Map<TimerId, Long> timerIdLongMap = new HashMap<>();
      for (final Map.Entry<String, JsonElement> entry : mapObject.entrySet()) {
        final TimerId timerId = new TimerId(entry.getKey());
        assert entry.getValue().isJsonPrimitive();
        final Long modificationTime = entry.getValue().getAsLong();
        timerIdLongMap.put(timerId, modificationTime);
      }
      return timerIdLongMap;
    }
  }
}
