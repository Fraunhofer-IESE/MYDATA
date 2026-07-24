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

import java.util.List;

/**
 * The Class Person.
 */
public class Person {

  /**
   * The user id.
   */
  private String userId;

  /**
   * The role.
   */
  private Role role;

  /**
   * The first name.
   */
  private String firstName;

  /**
   * The last name.
   */
  private String lastName;

  /**
   * The phone number.
   */
  private List<PhoneNumber> phoneNumber;

  /**
   * The addresses.
   */
  private List<Address> addresses;

  /**
   * Instantiates a new person.
   *
   * @param userId    the user id
   * @param firstName the first name
   * @param lastName  the last name
   * @param role      the role
   */
  public Person(String userId, String firstName, String lastName, Role role) {
    super();
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.role = role;
  }

  /**
   * Gets the role.
   *
   * @return the role
   */
  public Role getRole() {
    return this.role;
  }

  /**
   * Sets the role.
   *
   * @param role the new role
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Gets the first name.
   *
   * @return the first name
   */
  public String getFirstName() {
    return this.firstName;
  }

  /**
   * Sets the first name.
   *
   * @param firstName the new first name
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the last name.
   *
   * @return the last name
   */
  public String getLastName() {
    return this.lastName;
  }

  /**
   * Sets the last name.
   *
   * @param lastName the new last name
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Gets the phone number.
   *
   * @return the phone number
   */
  public List<PhoneNumber> getPhoneNumber() {
    return this.phoneNumber;
  }

  /**
   * Sets the phone number.
   *
   * @param phoneNumber the new phone number
   */
  public void setPhoneNumber(List<PhoneNumber> phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Gets the addresses.
   *
   * @return the addresses
   */
  public List<Address> getAddresses() {
    return this.addresses;
  }

  /**
   * Sets the addresses.
   *
   * @param addresses the new addresses
   */
  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  /**
   * Gets the user id.
   *
   * @return the userId
   */
  public String getUserId() {
    return this.userId;
  }

  /**
   * Sets the user id.
   *
   * @param userId the userId to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

}
