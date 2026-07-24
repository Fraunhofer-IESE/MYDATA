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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.io.IOException;

/**
 * Resolve lazy entity fields
 * Source: https://stackoverflow.com/a/13525550/9170927
 */
public class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      return (HibernateProxy.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new HibernateProxyTypeAdapter(gson) : null);
    }
  };
  private final Gson context;

  private HibernateProxyTypeAdapter(Gson context) {
    this.context = context;
  }

  @Override
  public HibernateProxy read(JsonReader in) {
    throw new UnsupportedOperationException("Not supported");
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void write(JsonWriter out, HibernateProxy value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    // Retrieve the original (not proxy) class
    Class<?> baseType = Hibernate.getClass(value);
    // Get the TypeAdapter of the original class, to delegate the serialization
    TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));
    // Get a filled instance of the original class
    Object unproxiedValue = value.getHibernateLazyInitializer()
        .getImplementation();
    // Serialize the value
    delegate.write(out, unproxiedValue);
  }
}
