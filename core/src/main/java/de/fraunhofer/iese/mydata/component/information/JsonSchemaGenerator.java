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

package de.fraunhofer.iese.mydata.component.information;

import de.fraunhofer.iese.mydata.component.information.method.JsonType;
import de.fraunhofer.iese.mydata.component.information.method.TypeDescription;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

//TODO rename? TypeDescriptionGenerator?
public class JsonSchemaGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaGenerator.class);

  private JsonSchemaGenerator() {
  }

  /**
   * @param  type
   * @param  typeClass
   * @return           a typedescription object
   */
  public static TypeDescription generateTypeDescription(final Type type, final Class<?> typeClass) {
    final Map<String, TypeDescription> typeNameAndTypeDescriptionMap = new HashMap<>();
    final TypeDescription selfTypeDescription = JsonSchemaGenerator
        .addToTypeNameAndTypeDescriptionMap(typeNameAndTypeDescriptionMap, type, typeClass);

    final TypeDescription typeDescriptionContainingReferencedTypeDescriptions = new TypeDescription();
    typeDescriptionContainingReferencedTypeDescriptions
        .setTypeName(selfTypeDescription.getTypeName());
    typeDescriptionContainingReferencedTypeDescriptions
        .setJsonType(selfTypeDescription.getJsonType());
    typeDescriptionContainingReferencedTypeDescriptions.getFieldTypeNames()
        .putAll(selfTypeDescription.getFieldTypeNames());
    typeDescriptionContainingReferencedTypeDescriptions
        .setReferencedTypeDescriptions(typeNameAndTypeDescriptionMap);
    return typeDescriptionContainingReferencedTypeDescriptions;
  }

  private static TypeDescription addComplexObjectType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Type type, Class<?> typeClass) {
    final TypeDescription typeDescription = new TypeDescription();
    typeDescription.setJsonType(JsonType.OBJECT);
    final String typeName = type.getTypeName();
    typeDescription.setTypeName(typeName);
    typeNameAndTypeDescriptionMap.put(typeName, typeDescription);

    final Field[] allComplexObjectFields = JsonSchemaGenerator.getAllFields(typeClass);

    JsonSchemaGenerator.addFieldDescriptionOfComplexType(typeNameAndTypeDescriptionMap,
        typeDescription, allComplexObjectFields);
    return typeDescription;
  }

  private static void addFieldDescriptionOfComplexType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, TypeDescription typeDescription,
      Field[] complexObjectFields) {
    for (final Field complexObjectField : complexObjectFields) {
      JsonSchemaGenerator.addDescriptionForField(typeNameAndTypeDescriptionMap, typeDescription,
          complexObjectField);
    }
  }

  private static void addDescriptionForField(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, TypeDescription typeDescription,
      Field complexObjectField) {
    final Type objectFieldType = complexObjectField.getGenericType();
    final Class<?> objectFieldClass = complexObjectField.getType();
    if (JsonSchemaGenerator.isMyDataPrimitive(objectFieldClass)) {
      typeDescription.addTypeNameForField(complexObjectField.getName(),
          addPrimitiveType(typeNameAndTypeDescriptionMap, objectFieldClass).getTypeName());
    } else if (JsonSchemaGenerator.isIterable(objectFieldClass)) {
      typeDescription.addTypeNameForField(complexObjectField.getName(),
          addIterableType(typeNameAndTypeDescriptionMap, objectFieldType).getTypeName());
    } else if (objectFieldClass.isArray()) {
      typeDescription.addTypeNameForField(complexObjectField.getName(),
          addArrayType(typeNameAndTypeDescriptionMap, objectFieldClass).getTypeName());
    } else if (JsonSchemaGenerator.isMap(objectFieldClass)) {
      typeDescription.addTypeNameForField(complexObjectField.getName(),
          addMapType(typeNameAndTypeDescriptionMap, objectFieldType).getTypeName());
    } else {
      // nested object
      if (!typeNameAndTypeDescriptionMap.containsKey(objectFieldType.getTypeName())) {
        JsonSchemaGenerator.addToTypeNameAndTypeDescriptionMap(typeNameAndTypeDescriptionMap,
            objectFieldType, objectFieldClass);
      }
      typeDescription.addTypeNameForField(complexObjectField.getName(),
          objectFieldType.getTypeName());
    }

  }

  private static TypeDescription getPrimitiveTypeDescription(final Class<?> typeClass) {
    final TypeDescription td = new TypeDescription();
    td.setTypeName(typeClass.getTypeName());
    td.setJsonType(JsonType.PRIMITIVE);
    return td;
  }

  private static TypeDescription addPrimitiveType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Class<?> typeClass) {
    final TypeDescription td = JsonSchemaGenerator.getPrimitiveTypeDescription(typeClass);
    typeNameAndTypeDescriptionMap.put(td.getTypeName(), td);
    return td;
  }

  private static TypeDescription getMapType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Type type) {
    final TypeDescription mapTypeDescription = new TypeDescription();
    mapTypeDescription.setJsonType(JsonType.OBJECT);

    Type genericType = Object.class;
    String genericTypeName = Object.class.getName();
    Class<?> genericTypeClass = Object.class;
    if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      if (actualTypeArguments != null && actualTypeArguments.length > 1) {
        final Type lGenericType = actualTypeArguments[1];
        final String lGenericTypeName = lGenericType.getTypeName();
        final Class<?> lGenericTypeClass;
        try {
          if (lGenericType instanceof ParameterizedType) {
            lGenericTypeClass = Class
                .forName(((ParameterizedType) lGenericType).getRawType().getTypeName());
          } else {
            lGenericTypeClass = Class.forName(lGenericTypeName);
          }
          // found
          genericType = lGenericType;
          genericTypeName = lGenericTypeName;
          genericTypeClass = lGenericTypeClass;
        } catch (final ClassNotFoundException e) {
          LOG.warn("Exception in JsonSchemaGenerator.getMapType, fallback by assuming type Object",
              e);
        }
      }
    } else {
      LOG.warn(
          "Unable determine the generic type of the given map, fallback by assuming type Object");
    }

    mapTypeDescription.setTypeName(type.getTypeName());
    mapTypeDescription.addTypeNameForField(TypeDescription.SPECIAL_FIELD_FOR_CONTENT_INFORMATION,
        genericType.getTypeName());
    if (/* !TypeDescription.isMyDataPrimitive(genericTypeClass) && */ !typeNameAndTypeDescriptionMap
        .containsKey(genericTypeName)) {
      JsonSchemaGenerator.addToTypeNameAndTypeDescriptionMap(typeNameAndTypeDescriptionMap,
          genericType, genericTypeClass);
    }
    return mapTypeDescription;
  }

  private static TypeDescription addMapType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Type type) {
    final TypeDescription mapTypeDescription = getMapType(typeNameAndTypeDescriptionMap, type);
    typeNameAndTypeDescriptionMap.put(mapTypeDescription.getTypeName(), mapTypeDescription);
    return mapTypeDescription;
  }

  @SuppressWarnings("rawtypes")
  private static TypeDescription getArrayType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Class typeClass) {
    final TypeDescription arrayTypeDescription = new TypeDescription();
    arrayTypeDescription.setJsonType(JsonType.ARRAY);
    final Class<?> componentClass = typeClass.getComponentType();
    final String componentName = componentClass.getName();
    arrayTypeDescription.setTypeName(typeClass.getTypeName());
    arrayTypeDescription.addTypeNameForField(TypeDescription.SPECIAL_FIELD_FOR_CONTENT_INFORMATION,
        componentName);
    if (/* !TypeDescription.isMyDataPrimitive(componentClass)&& */!typeNameAndTypeDescriptionMap
        .containsKey(componentName)) {
      JsonSchemaGenerator.addToTypeNameAndTypeDescriptionMap(typeNameAndTypeDescriptionMap,
          componentClass, componentClass);
    }
    return arrayTypeDescription;
  }

  @SuppressWarnings("rawtypes")
  private static TypeDescription addArrayType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Class typeClass) {
    final TypeDescription arrayTypeDescription = getArrayType(typeNameAndTypeDescriptionMap,
        typeClass);
    typeNameAndTypeDescriptionMap.put(typeClass.getTypeName(), arrayTypeDescription);
    return arrayTypeDescription;
  }

  private static TypeDescription getIterableType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Type type) {
    final TypeDescription iterableTypeDescription = new TypeDescription();
    iterableTypeDescription.setJsonType(JsonType.ARRAY);

    Type genericType = Object.class;
    String genericTypeName = Object.class.getName();
    Class<?> genericTypeClass = Object.class;
    if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      if (actualTypeArguments != null && actualTypeArguments.length > 0) {
        final Type lGenericType = actualTypeArguments[0];
        final String lGenericTypeName = lGenericType.getTypeName();
        final Class<?> lGenericTypeClass;
        try {
          if (lGenericType instanceof ParameterizedType) {
            lGenericTypeClass = Class
                .forName(((ParameterizedType) lGenericType).getRawType().getTypeName());
          } else {
            lGenericTypeClass = Class.forName(lGenericTypeName);
          }
          // found
          genericType = lGenericType;
          genericTypeName = lGenericTypeName;
          genericTypeClass = lGenericTypeClass;
        } catch (final ClassNotFoundException e) {
          LOG.warn(
              "Exception in JsonSchemaGenerator.getIterableType, fallback by assuming type Object",
              e);
        }
      }
    } else {
      LOG.warn(
          "Unable determine the generic type of the given iterable, fallback by assuming type Object");
    }

    iterableTypeDescription.setTypeName(type.getTypeName());
    iterableTypeDescription.addTypeNameForField(
        TypeDescription.SPECIAL_FIELD_FOR_CONTENT_INFORMATION, genericTypeName);
    if (/* !TypeDescription.isMyDataPrimitive(genericTypeClass)&& */!typeNameAndTypeDescriptionMap
        .containsKey(genericTypeName)) {
      JsonSchemaGenerator.addToTypeNameAndTypeDescriptionMap(typeNameAndTypeDescriptionMap,
          genericType, genericTypeClass);
    }

    return iterableTypeDescription;
  }

  private static TypeDescription addIterableType(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Type type) {
    final TypeDescription iterableTypeDescription = getIterableType(typeNameAndTypeDescriptionMap,
        type);
    typeNameAndTypeDescriptionMap.put(type.getTypeName(), iterableTypeDescription);
    return iterableTypeDescription;
  }

  private static TypeDescription addToTypeNameAndTypeDescriptionMap(
      Map<String, TypeDescription> typeNameAndTypeDescriptionMap, Type type, Class<?> typeClass) {
    if (JsonSchemaGenerator.isMyDataPrimitive(typeClass)) {
      return JsonSchemaGenerator.addPrimitiveType(typeNameAndTypeDescriptionMap, typeClass);
    } else if (JsonSchemaGenerator.isIterable(typeClass)) {
      return JsonSchemaGenerator.addIterableType(typeNameAndTypeDescriptionMap, type);
    } else if (typeClass.isArray()) {
      return JsonSchemaGenerator.addArrayType(typeNameAndTypeDescriptionMap, typeClass);
    } else if (JsonSchemaGenerator.isMap(typeClass)) {
      return JsonSchemaGenerator.addMapType(typeNameAndTypeDescriptionMap, type);
    } else {
      // complex object
      return JsonSchemaGenerator.addComplexObjectType(typeNameAndTypeDescriptionMap, type,
          typeClass);
    }
  }

  private static boolean isMyDataPrimitive(Class<?> aClass) {
    return aClass.isPrimitive() || ClassUtils.wrapperToPrimitive(aClass) != null || aClass.isEnum()
        || aClass == String.class || aClass == Date.class;
  }

  private static boolean isIterable(Class<?> aClass) {
    return Iterable.class.isAssignableFrom(aClass);
  }

  private static boolean isMap(Class<?> aClass) {
    return Map.class.isAssignableFrom(aClass);
  }

  /**
   * Returns all the fields of the class provided including the fields of all its ancestors.
   *
   * @param  anyClass Class to get fields for.
   * @return          List of fields.
   */
  static Field[] getAllFields(final Class<?> anyClass) {
    final Map<String, Field> stringFieldMap = new TreeMap<>();
    Class<?> currentClass = anyClass;
    Field[] allField = anyClass.getDeclaredFields();
    for (final Field field : allField) {
      stringFieldMap.put(field.getName(), field);
    }
    while (currentClass.getSuperclass() != null && currentClass.getSuperclass() != Object.class) {
      allField = currentClass.getSuperclass().getDeclaredFields();
      for (final Field field : allField) {
        stringFieldMap.put(field.getName(), field);
      }
      currentClass = currentClass.getSuperclass();
    }
    return stringFieldMap.values().toArray(new Field[0]);
  }

}
