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

package de.fraunhofer.iese.mydata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chattapa on 10/16/16.
 */
public class User {

  private String name;

  private Long[] phoneNo;

  private List<String> tags = new ArrayList<>();

  private Map<String, CreditCardInfo> accountDetails = new HashMap<>();

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long[] getPhoneNo() {
    return this.phoneNo;
  }

  public void setPhoneNo(Long[] phoneNo) {
    this.phoneNo = phoneNo;
  }

  public Map<String, CreditCardInfo> getAccountDetails() {
    return this.accountDetails;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public void setAccountDetails(Map<String, CreditCardInfo> accountDetails) {
    this.accountDetails = accountDetails;
  }

  public static class CreditCardInfo {

    private String pin;

    private String bankName;

    public CreditCardInfo(String pin, String bankName) {
      super();
      this.pin = pin;
      this.bankName = bankName;
    }

    public String getPin() {
      return this.pin;
    }

    public void setPin(String pin) {
      this.pin = pin;
    }

    public String getBankName() {
      return this.bankName;
    }

    public void setBankName(String bankName) {
      this.bankName = bankName;
    }

  }

}
