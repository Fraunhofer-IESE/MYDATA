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

/**
 * The Class Address.
 */
public class Address {

  /**
   * The type.
   */
  private AddressType type;

  /**
   * The name.
   */
  private String name;

  /**
   * The street.
   */
  private String street;

  /**
   * The number.
   */
  private String number;

  /**
   * The zip code.
   */
  private String zipCode;

  /**
   * The city.
   */
  private String city;

  /**
   * The country.
   */
  private String country;

  /**
   * Instantiates a new address.
   *
   * @param name    the name
   * @param street  the street
   * @param number  the number
   * @param zipCode the zip code
   * @param city    the city
   * @param country the country
   * @param type    the type
   */
  public Address(String name, String street, String number, String zipCode, String city,
      String country, AddressType type) {
    super();
    this.name = name;
    this.street = street;
    this.number = number;
    this.zipCode = zipCode;
    this.city = city;
    this.country = country;
    this.type = type;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public AddressType getType() {
    return this.type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(AddressType type) {
    this.type = type;
  }

  /**
   * Gets the street.
   *
   * @return the street
   */
  public String getStreet() {
    return this.street;
  }

  /**
   * Sets the street.
   *
   * @param street the new street
   */
  public void setStreet(String street) {
    this.street = street;
  }

  /**
   * Gets the number.
   *
   * @return the number
   */
  public String getNumber() {
    return this.number;
  }

  /**
   * Sets the number.
   *
   * @param number the new number
   */
  public void setNumber(String number) {
    this.number = number;
  }

  /**
   * Gets the zip code.
   *
   * @return the zip code
   */
  public String getZipCode() {
    return this.zipCode;
  }

  /**
   * Sets the zip code.
   *
   * @param zipCode the new zip code
   */
  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  /**
   * Gets the city.
   *
   * @return the city
   */
  public String getCity() {
    return this.city;
  }

  /**
   * Sets the city.
   *
   * @param city the new city
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Gets the country.
   *
   * @return the country
   */
  public String getCountry() {
    return this.country;
  }

  /**
   * Sets the country.
   *
   * @param country the new country
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

}
