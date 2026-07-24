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

package de.fraunhofer.iese.mydata.oauth;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

@Entity
@Table(name = "oauth_client_details", indexes = {
    @Index(columnList = "client_id", unique = true)
})
@Getter
@Setter
public class OauthClientDetails extends MyDataEntity {

  @NotNull
  @Valid
  @EmbeddedId
  @Column(name = "client_id")
  private ClientId clientId;

  @NotBlank
  @Column(name = "client_secret")
  private String clientSecret;

  @Column
  private String scope;

  @Column
  private String resourceIds;

  @NotBlank
  @Column
  private String authorizedGrantTypes;

  @NotBlank
  @Column
  private String authorities;

  @Column
  private Integer accessTokenValidity;

  @Column
  private Integer refreshTokenValidity;

  @Column
  private String additionalInformation;

  @Column
  private String webServerRedirectUri;

  @Column
  private String autoapprove;

  /**
   * Default constructor for JPA
   */
  public OauthClientDetails() {
    // required by JPA
  }

  /**
   * Generates a new client with a token id and a secret
   *
   * @param tokenId
   * @param secret
   */
  public OauthClientDetails(ClientId tokenId, String secret) {
    this.clientId = tokenId;
    this.clientSecret = secret;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final OauthClientDetails that = (OauthClientDetails) o;

    return Objects.equals(this.getClientId(), that.getClientId());
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.clientId);
    return builder.toHashCode();
  }

}
