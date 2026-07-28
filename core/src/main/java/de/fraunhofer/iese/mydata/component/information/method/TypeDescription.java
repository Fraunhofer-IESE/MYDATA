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

package de.fraunhofer.iese.mydata.component.information.method;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Describes a JSON Type
 */
@Entity
@Getter
@Setter
public class TypeDescription extends MyDataEntity {

  public static final String SPECIAL_FIELD_FOR_CONTENT_INFORMATION = "*";

  private static final Logger LOG = LoggerFactory.getLogger(TypeDescription.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The type name.
   */
  @NotBlank
  private String typeName;

  /**
   * The json type.
   */
  @NotNull
  private JsonType jsonType;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, String> fieldTypeNames = new HashMap<>();

  @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @MapKey(name = "typeName")
  private Map<String, @Valid TypeDescription> referencedTypeDescriptions;

  @Transient
  @Hide
  private TypeDescription parent;

  /**
   * basic constructor needed for JPA
   */
  public TypeDescription() {
    // Needed for JPA
  }

  /**
   * Adds the fields.
   *
   * @param fieldName the key
   * @param typeName  the value
   */
  public void addTypeNameForField(String fieldName, String typeName) {
    this.fieldTypeNames.put(fieldName, typeName);
  }

  private Map<String, TypeDescription> determineReferencedTypeDescriptions() {
    if (this.referencedTypeDescriptions != null && this.referencedTypeDescriptions.size() > 0) {
      return this.referencedTypeDescriptions;
    } else if (this.parent != null) {
      return this.parent.determineReferencedTypeDescriptions();
    } else {
      return Collections.emptyMap();
    }
  }

  /**
   * @return the class for the current type
   */
  public Optional<Class<?>> getTypeClass() {
    try {
      return Optional.of(Class.forName(this.typeName));
    } catch (final ClassNotFoundException e) {
      LOG.debug("Class {} not found", this.typeName, e);
      return Optional.empty();
    }
  }

  /**
   * @return the list of registried field names
   */
  public Set<String> getFieldNames() {
    return Collections.unmodifiableSet(this.fieldTypeNames.keySet());
  }

  /**
   * @param  fieldName
   * @return           true if there is such a field name
   */
  public boolean hasField(String fieldName) {
    return this.fieldTypeNames.containsKey(fieldName);
  }

  /**
   * @param  fieldName
   * @return           the TypeDescription corresponding to a field name
   */
  public Optional<TypeDescription> getTypeDescriptionForField(String fieldName) {
    final TypeDescription thisVar = this;
    return this.getTypeNameForField(fieldName).map(s -> {
      final TypeDescription typeDescription = thisVar.determineReferencedTypeDescriptions().get(s);
      if (typeDescription != null) {
        typeDescription.parent = thisVar;
      }
      return typeDescription;
    });
  }

  /**
   * @param  fieldName
   * @return           the string type name for a field
   */
  public Optional<String> getTypeNameForField(String fieldName) {
    return Optional.ofNullable(this.fieldTypeNames.get(fieldName));
  }
}
