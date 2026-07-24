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

import de.fraunhofer.iese.mydata.pep.testdata.model.Company;
import de.fraunhofer.iese.mydata.pep.testdata.model.Person;
import de.fraunhofer.iese.mydata.pep.testdata.model.PhoneNumber;
import de.fraunhofer.iese.mydata.pep.testdata.model.PhoneType;
import de.fraunhofer.iese.mydata.pep.testdata.model.Project;
import de.fraunhofer.iese.mydata.pep.testdata.model.Role;
import de.fraunhofer.iese.mydata.pep.testdata.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A factory for creating Data objects.
 */
public class DataFactory {

  /**
   * Gets the company.
   *
   * @return the company
   */
  public static Company getCompany() {
    return new Company("Bau GmbH & Co. KG", null);
  }

  public static Company getCompanyWithProjects() {
    final Company company = getCompany();
    company.setEmployees(new ArrayList<>(getUsers().values()));
    return company;
  }

  /**
   * Gets the projects.
   *
   * @return the projects
   */
  public static List<Project> getProjects() {
    try {

      final HashMap<String, Person> users = getUsers();

      final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

      final List<Project> projects = new ArrayList<>();

      Project p = new Project("sp1", "Swimming Pool", users.get("pz"), 1200200.98f);
      Calendar.getInstance().set(2015, 4, 21);
      p.addTask(new Task("sp1b", "Baggern", sdf.parse("21.05.2016"), "Grube ausheben",
          users.get("hm"), 34, 7));
      p.addTask(new Task("sp1f", "Fliesen legen", sdf.parse("21.05.2016"), "Fliesen legen",
          users.get("hm"), 34, 7));
      projects.add(p);
      p = new Project("sauna1", "Sauna", users.get("pz"), 200200.44f);
      p.addTask(new Task("sauna1h", "Heizung", sdf.parse("11.06.2016"), "Heizung einbauen",
          users.get("kk"), 34, 7.1f));
      p.addTask(new Task("sauna1i", "Innenausstattung", sdf.parse("11.06.2016"), "Holz montieren",
          users.get("kk"), 34, 71.f));
      projects.add(p);

      return projects;
    } catch (final ParseException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets the users.
   *
   * @return the users
   */
  public static HashMap<String, Person> getUsers() {
    final HashMap<String, Person> persons = new HashMap<>();
    final Person csm = new Person("pz", "Peter", "Zwegat", Role.CSM);
    final ArrayList<PhoneNumber> numbers = new ArrayList<>();
    numbers.add(new PhoneNumber(PhoneType.BUSINESS, "1234123"));
    numbers.add(new PhoneNumber(PhoneType.PRIVATE, "9879798"));
    csm.setPhoneNumber(numbers);

    final Person foreman = new Person("hm", "Hans", "Müller", Role.FOREMAN);
    final Person foreman2 = new Person("kk", "Klaus", "Kleber", Role.FOREMAN);

    persons.put(csm.getUserId(), csm);
    persons.put(foreman.getUserId(), foreman);
    persons.put(foreman2.getUserId(), foreman2);

    return persons;

  }

  public static User getUser() {
    final User user = new User();
    user.setName("Denis Feth");
    user.setPhoneNo(new Long[] {
        4915234767022L, 919748087957L
    });
    user.setAccountDetails(new HashMap<>());
    user.getAccountDetails().put("1234-5678-9101-1121",
        new User.CreditCardInfo("1111", "STADPARKASSE"));
    user.getAccountDetails().put("3141-5161-7181-9202",
        new User.CreditCardInfo("2222", "Commercez"));
    return user;
  }

  public static Product getProductWithPriceInInt() {
    final Product product = new Product();
    product.setName("Denis Feth");
    product.setPrice("57");
    return product;
  }

  public static Product getProductWithPriceInFloat() {
    final Product product = new Product();
    product.setName("Denis Feth");
    product.setPrice("57.5");
    return product;
  }
}
