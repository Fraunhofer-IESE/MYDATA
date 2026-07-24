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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DataObjectSerDes {

  public static class DataObjectJsonSerializer implements JsonSerializer<DataObject<?>> {

    @Override
    public JsonElement serialize(DataObject<?> src, Type typeOfSrc,
        JsonSerializationContext context) {
      final JsonObject jsonObject = new JsonObject();
      JsonElement jsonsrc = MyDataEntity.getGson().toJsonTree(src.getValue());
      if (jsonsrc == null || jsonsrc instanceof JsonNull) {
        jsonsrc = MyDataEntity.getGson().toJsonTree(src.getValue(), src.getType());
      }
      jsonObject.add("value", jsonsrc);
      jsonObject.addProperty("type", src.getTypeName());
      jsonObject.addProperty("isComplex", src.isComplex());
      return jsonObject;
    }
  }

  public static class DataObjectJsonDeserializer implements JsonDeserializer<DataObject<?>> {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectJsonDeserializer.class);

    private static final List<String> unknownClasses = new ArrayList<>();

    private DataObject<?> createObjectFromUnkownClass(final JsonElement value, final String type,
        final boolean isComplex) {
      String result;

      if (!isComplex || Object.class.getCanonicalName().equals(type)) {
        result = value.toString();
      } else {
        result = value.getAsString();
      }

      @SuppressWarnings({
          "unchecked", "rawtypes"
      })
      final DataObject o = new DataObject(result);
      o.setType(type);
      o.setComplex(true);
      return o;
    }

    @Override
    public DataObject<?> deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      final JsonElement value = json.getAsJsonObject().get("value");
      final String type = json.getAsJsonObject().get("type").getAsString();
      final boolean isComplex = json.getAsJsonObject().get("isComplex").getAsBoolean();

      if (unknownClasses.contains(type)) {
        return this.createObjectFromUnkownClass(value, type, isComplex);
      }
      try {
        final Object o = MyDataEntity.getGson().fromJson(value, Class.forName(type));

        return new DataObject<>(o, Class.forName(type));

      } catch (final ClassNotFoundException e) {
        LOG.debug("Deserialization failed. Adding " + type + " to the list of unknown classes", e);
        unknownClasses.add(type);

        return this.createObjectFromUnkownClass(value, type, isComplex);
      }
    }
  }
}
