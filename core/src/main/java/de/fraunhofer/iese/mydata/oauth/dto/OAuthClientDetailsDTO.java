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

package de.fraunhofer.iese.mydata.oauth.dto;

import de.fraunhofer.iese.mydata.user.MyDataRole;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "clientId", "clientSecret", "authorizedGrantType", "authority"
})
@Data
public class OAuthClientDetailsDTO {
  @JsonProperty("clientId")
  private String clientId;

  @JsonProperty("clientSecret")
  private String clientSecret;

  @JsonProperty("authority")
  private MyDataRole authority;

  @JsonProperty("authorizedGrantType")
  private String authorizedGrantType;

  public OAuthClientDetailsDTO() {
    // empty Constructor
  }

  public void setAuthority(String s) {
    for (final MyDataRole role : MyDataRole.values()) {
      if (role.toString().equalsIgnoreCase(s)) {
        this.authority = role;
        return;
      }
    }
    throw new IllegalArgumentException("the string is not a valid role");
  }
}
