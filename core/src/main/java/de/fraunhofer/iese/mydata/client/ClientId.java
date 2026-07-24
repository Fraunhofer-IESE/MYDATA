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

package de.fraunhofer.iese.mydata.client;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class ClientId extends MyDataEntity implements Serializable {

  private static final long serialVersionUID = 2358845878407023748L;

  /**
   * The URN pattern for an ActionId. Must be of the format
   * &quot;urn:client:&lt;solution&gt;:&lt;identifier&gt;&quot; or
   * &quot;urn:token:&lt;solution&gt;:&lt;identifier&gt;&quot; where
   * <ul>
   * <li>solution is the solution (e.g., system, domain) where the identifier occurs</li>
   * <li>component_id is the unique component_id of the identifier</li>
   * </ul>
   */
  @NotNull
  @Pattern(regexp = "^urn:(client|token)(:[a-z0-9-_\\.]+){2}$")
  @Column(name = "client_id")
  private String urn;

  /**
   * default constructor
   */
  public ClientId() {
    super();
  }

  /**
   * @param id : the urn
   */
  public ClientId(String id) {
    this.urn = id;
  }

  @Override
  public String toString() {
    return this.urn;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (this.getUrn() == null || obj == null || !(obj instanceof ClientId)) {
      return false;
    }

    final ClientId that = (ClientId) obj;
    return Objects.equals(this.urn, that.getUrn());
  }

  /**
   * @param  clientId
   * @return                        the identifier part of the urn
   * @throws InvalidEntityException
   */
  public static String getClientIdentifier(ClientId clientId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(clientId);
    return clientId.getUrn().split(":")[3];
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.urn);
    return builder.toHashCode();
  }
}
