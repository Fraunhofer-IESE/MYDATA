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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iese.mydata.affiliation.validation.AddressGroup;
import de.fraunhofer.iese.mydata.affiliation.validation.ContactGroup;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.user.User;

import jakarta.validation.groups.Default;
import org.junit.jupiter.api.Test;

class AffiliationTest {

  @Test
  void whenEmptyThenFail() {
    final Affiliation aff = new Affiliation();
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(aff));
  }

  @Test
  void whenOnlyIdThenFail() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(aff));
  }

  @Test
  void whenInvalidIdThenFail() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:aff:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(aff));
  }

  @Test
  void whenMandatoryThenOk() throws Exception {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");

    MyDataEntity.validate(aff);
  }

  @Test
  void whenOnlyMandatoryAndAdressValidationGroupThenFail() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    Class x = AddressGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(aff, x, x1));
  }

  @Test
  void whenMandatoryAndAddressInfoAndAdressValidationGroupThenOk()
      throws Exception {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");

    MyDataEntity.validate(aff, AddressGroup.class, Default.class);
  }

  @Test
  void whenOnlyMandatoryAndContactValidationGroupThenFail() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    Class x = ContactGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(aff, x, x1));
  }

  @Test
  void whenMandatoryAndContactInfoAndContactValidationGroupThenOk()
      throws Exception {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");
    aff.setContactPerson("Hans Meier");
    aff.setContactPhone("0631-68001111");

    MyDataEntity.validate(aff, ContactGroup.class, Default.class);
  }

  @Test
  void whenInvalidUserThenFail() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");
    aff.setContactPerson("Hans Meier");
    aff.setContactPhone("0631-68001111");
    aff.addUser(new User());
    Class x = ContactGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(aff, x, x1));
  }

  @Test
  void whenInvalidSolutionThenFail() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");
    aff.setContactPerson("Hans Meier");
    aff.setContactPhone("0631-68001111");
    aff.addSolution(new Solution());
    Class x = ContactGroup.class;
    Class x1 = Default.class;
    assertThrows(InvalidEntityException.class, () ->

      MyDataEntity.validate(aff, x, x1));
  }

  @Test
  void whenNullSolutionThenThrowException() throws Exception {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");
    aff.setContactPerson("Hans Meier");
    aff.setContactPhone("0631-68001111");
    assertThrows(IllegalArgumentException.class, () ->

      aff.addSolution(null));
  }

  @Test
  void whenRemoveNullSolutionThenThrowException() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");
    aff.setContactPerson("Hans Meier");
    aff.setContactPhone("0631-68001111");
    assertThrows(IllegalArgumentException.class, () ->

      aff.removeSolution(null));
  }

  @Test
  void whenRemoveSolutionThenOk() {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");
    aff.setContactPerson("Hans Meier");
    aff.setContactPhone("0631-68001111");

    final Solution solution = new Solution(new SolutionId("urn:solution:test"));
    aff.addSolution(solution);

    assertTrue(aff.getSolutions().contains(solution));

    aff.removeSolution(solution);
    assertFalse(aff.getSolutions().contains(solution));
  }

  @Test
  void whenEqualThenEqualsAndHashCodeOk() throws Exception {
    final Affiliation aff = new Affiliation();
    aff.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff.setContactMail("test@example.com");
    aff.setLockStatus(false);
    aff.setName("Affiliation 1");
    aff.setCountry("Germany");
    aff.setCity("Kaiserslautern");
    aff.setZipCode(67663l);
    aff.setStreet("Fraunhofer-Platz 1");
    aff.setContactPerson("Hans Meier");
    aff.setContactPhone("0631-68001111");

    final Affiliation aff2 = new Affiliation();
    aff2.setAffiliationId(new AffiliationId("urn:affiliation:test"));
    aff2.setContactMail("test@example.com");
    aff2.setLockStatus(false);
    aff2.setName("Affiliation 1");
    aff2.setCountry("Germany");
    aff2.setCity("Kaiserslautern");
    aff2.setZipCode(67663l);
    aff2.setStreet("Fraunhofer-Platz 1");
    aff2.setContactPerson("Hans Meier");
    aff2.setContactPhone("0631-68001111");

    MyDataEntity.validate(aff, Default.class);
    MyDataEntity.validate(aff2, Default.class);

    assertEquals(aff, aff2);
    assertEquals(aff.hashCode(), aff2.hashCode());
  }
}
