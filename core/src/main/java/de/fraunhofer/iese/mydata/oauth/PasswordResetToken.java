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

import de.fraunhofer.iese.mydata.user.User;
import de.fraunhofer.iese.mydata.util.RandomStringUtil;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;

@Entity(name = "temporary_credential")
@Getter
@Setter
public class PasswordResetToken {

  /**
   * token expiration time in minutes
   */
  private static final int EXPIRATION = 60;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String token;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_uuid")
  private User user;

  private Date expiryDate;

  public PasswordResetToken() {
    // for jpa
  }

  public PasswordResetToken(User user) {
    this.user = user;
    this.token = new RandomStringUtil(32, RandomStringUtil.ALPHANUMSPECIAL).generateString();
    this.expiryDate = this.calculateExpiryDate(EXPIRATION);
  }

  private Date calculateExpiryDate(final int expiryTimeInMinutes) {
    final Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(new Date().getTime());
    cal.add(Calendar.MINUTE, expiryTimeInMinutes);
    return new Date(cal.getTime().getTime());
  }

  public void updateToken(final String token) {
    this.token = token;
    this.expiryDate = this.calculateExpiryDate(EXPIRATION);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.expiryDate == null) ? 0 : this.expiryDate.hashCode());
    result = prime * result + ((this.token == null) ? 0 : this.token.hashCode());
    result = prime * result + ((this.user == null) ? 0 : this.user.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PasswordResetToken)) {
      return false;
    }
    final PasswordResetToken other = (PasswordResetToken) obj;
    if (this.expiryDate == null) {
      if (other.expiryDate != null) {
        return false;
      }
    } else if (!this.expiryDate.equals(other.expiryDate)) {
      return false;
    }
    if (this.token == null) {
      if (other.token != null) {
        return false;
      }
    } else if (!this.token.equals(other.token)) {
      return false;
    }
    if (this.user == null) {
      if (other.user != null) {
        return false;
      }
    } else if (!this.user.equals(other.user)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("Token [String=").append(this.token).append("]").append("[Expires ")
        .append(this.expiryDate).append("]");
    return builder.toString();
  }
}
