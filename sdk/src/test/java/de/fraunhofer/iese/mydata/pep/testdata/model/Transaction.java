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

package de.fraunhofer.iese.mydata.pep.testdata.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Transaction {

  private String type;

  private Integer id;

  private Integer transactionPatternId;

  private Amount amount;

  private String bookingDate;

  private String valueDate;

  private String creditor;

  private String creditorBankCode;

  private String creditorAccountNumber;

  private String debtor;

  private Object debtorBankCode;

  private String debtorAccountNumber;

  private String purpose;

  private String cleanPurpose;

  private Boolean prebooked;

  public String getRoundedAmountValue() {
    try {
      return BigDecimal.valueOf(this.amount.getValue() / 100d).setScale(2, RoundingMode.HALF_EVEN).toString();
    } catch (final Exception e) {
      return "0";
    }
  }

  public boolean isCredit() {
    return (this.amount.getValue() > 0);
  }

  public boolean isDebit() {
    return (this.amount.getValue() < 0);
  }

  public String getFrom(String currentAccount) {
    if (this.debtor.equalsIgnoreCase(this.creditor)) {
      return this.debtor + "(same as creditor)";
    }
    if (this.debtorAccountNumber.equalsIgnoreCase(currentAccount) || currentAccount.contains(this.debtorAccountNumber)) {
      return this.debtor;
    }
    if (this.isCredit()) {
      return this.debtor;
    }

    return this.creditor;
  }
}
