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

package de.fraunhofer.iese.mydata.affiliation;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class AffiliationId extends MyDataEntity implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(AffiliationId.class);

  private static final long serialVersionUID = -8521193954976023156L;

  private static final String URN_PREFIX = "urn:affiliation:";

  /***
   * pattern for solution ^urn:affiliation:[a-z0-9-_\.]+$
   */
  @NotNull
  @Pattern(regexp = "^" + URN_PREFIX + "[a-z0-9-_\\.]+$")
  @Column(name = "affiliation_id")
  private String urn;

  /**
   * default constructor
   */
  public AffiliationId() {
    super();
  }

  /**
   * @param urnOrIdentifier will be set to the urn if starts with {@value #URN_PREFIX}, else the
   *                          prefix will be add and set as urn
   */
  public AffiliationId(@NonNull String urnOrIdentifier) {
    this.urn = this.transfromUrnOrIdentifierToUrn(urnOrIdentifier);
  }

  /**
   * @param urnOrIdentifier will be set to the urn if starts with {@value #URN_PREFIX}, else the
   *                          prefix will be add and set as urn
   */
  public void setUrn(@NonNull String urnOrIdentifier) {
    this.urn = this.transfromUrnOrIdentifierToUrn(urnOrIdentifier);
  }

  private String transfromUrnOrIdentifierToUrn(String urnOrIdentifier) {
    if (urnOrIdentifier.startsWith(URN_PREFIX)) {
      return urnOrIdentifier.toLowerCase();
    } else {
      LOG.debug("id {} has no URN_PREFIX: {}, it will be added", urnOrIdentifier, URN_PREFIX);
      return (URN_PREFIX + urnOrIdentifier).toLowerCase();
    }
  }

  /**
   * @return                        the identifier part (without urn:affiliation) of this
   *                                affiliationid
   * @throws InvalidEntityException in case of an invalid affiliationid
   */
  @Transient
  public String getIdentifier() throws InvalidEntityException {
    MyDataEntity.validate(this);
    return this.getUrn().split(URN_PREFIX)[1];
  }

  @Transient
  public void setIdentifier(String identifier) throws InvalidEntityException {
    this.urn = URN_PREFIX + identifier;
    MyDataEntity.validate(this);
  }

  @Override
  public String toString() {
    return this.urn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof AffiliationId)) {
      return false;
    }

    final AffiliationId that = (AffiliationId) o;

    return new EqualsBuilder().append(this.getUrn(), that.getUrn()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getUrn()).toHashCode();
  }
}
