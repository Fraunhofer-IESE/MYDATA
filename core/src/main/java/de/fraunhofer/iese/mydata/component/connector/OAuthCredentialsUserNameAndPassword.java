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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;

@EqualsAndHashCode(callSuper = true)
@Data
public class OAuthCredentialsUserNameAndPassword extends OAuthCredentials {

  private static final long serialVersionUID = -993260371037335856L;

  private String username;

  @ToString.Exclude
  private String password;

  public OAuthCredentialsUserNameAndPassword(ClientId clientId, String clientSecret,
      URI accessTokenURI, String username, String password) {
    super(clientId, clientSecret, accessTokenURI);
    this.setUsername(username);
    this.setPassword(password);
  }

  public OAuthCredentialsUserNameAndPassword(OAuthCredentials oAuthCredentials, String username,
      String password) {
    this(oAuthCredentials.getClientId(), oAuthCredentials.getClientSecret(),
        oAuthCredentials.getAccessTokenURI(), username, password);
  }

}
