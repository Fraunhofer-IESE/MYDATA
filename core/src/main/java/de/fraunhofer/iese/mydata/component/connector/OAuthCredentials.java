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

package de.fraunhofer.iese.mydata.component.connector;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.net.URI;

/**
 * DTO to provide the infos to authenticate via OAuth.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OAuthCredentials extends MyDataEntity implements Authentication, Serializable {

  private static final long serialVersionUID = 4216021726394998556L;

  @Valid
  @NotNull
  private ClientId clientId;

  @NotBlank
  @ToString.Exclude
  private String clientSecret;

  @NotNull
  private URI accessTokenURI;

  public OAuthCredentials(ClientId clientId, String clientSecret, URI accessTokenURI) {
    this.clientId = clientId;
    this.accessTokenURI = accessTokenURI;
    this.clientSecret = clientSecret;
  }

  @Override
  public Object getPrincipal() {
    return this.getClientId();
  }

  @Override
  public Object getCredentials() {
    return this.getClientSecret();
  }

}
