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

import de.fraunhofer.iese.mydata.affiliation.validation.AddressGroup;
import de.fraunhofer.iese.mydata.affiliation.validation.ContactGroup;
import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
public class Affiliation extends MyDataEntity {

  @NotNull
  @Valid
  @EmbeddedId
  private AffiliationId affiliationId;

  @NotBlank
  private String name;

  @NotNull
  private Boolean lockStatus = false;

  @NotBlank(groups = ContactGroup.class)
  private String contactPerson;

  @NotBlank(groups = ContactGroup.class)
  @Email(groups = ContactGroup.class)
  private String contactMail;

  @NotBlank(groups = ContactGroup.class)
  private String contactPhone;

  @NotBlank(groups = AddressGroup.class)
  private String street;

  @NotNull(groups = AddressGroup.class)
  @Min(value = 0, groups = AddressGroup.class)
  @Max(value = 99999, groups = AddressGroup.class)
  private Long zipCode = 0L;

  @NotBlank(groups = AddressGroup.class)
  private String city;

  @NotBlank(groups = AddressGroup.class)
  private String country;

  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "affiliation", cascade = {
      CascadeType.ALL
  })
  private Set<@Valid Solution> solutions;

  @Hide
  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "affiliation", cascade = {
      CascadeType.PERSIST, CascadeType.MERGE
  })
  private Set<@Valid User> users;

  /**
   * Default constructor for JPA
   */
  public Affiliation() {
    // required by JPA
  }

  /**
   * assign an affiliationId to the current affiliation
   *
   * @param affId
   */
  public Affiliation(AffiliationId affId) {
    this.affiliationId = affId;
  }

  /**
   * @return true if the affiliation is locked, false if still ok
   */
  public boolean getLockStatus() {
    if (this.lockStatus == null) {
      return false;
    } else {
      return this.lockStatus;
    }
  }

  /**
   * @param lo : lock or unlock the current affiliation
   */
  public void setLockStatus(Boolean lo) {
    this.lockStatus = lo;
  }

  /**
   * needed for JPA/Hibernate a solution, if not null, is added to the current list of solutions for
   * this affiliation
   *
   * @param solution
   */
  public void addSolution(@NonNull Solution solution) {
    this.getSolutions().add(solution);
    solution.setAffiliation(this);

  }

  /**
   * needed for JPA/Hibernate a solution, if not null, is removed from the current list of solutions
   * for this affiliation
   *
   * @param solution
   */
  public void removeSolution(@NonNull Solution solution) {
    this.getSolutions().remove(solution);
    solution.setAffiliation(null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Affiliation)) {
      return false;
    }

    final Affiliation that = (Affiliation) o;
    return Objects.equals(this.getAffiliationId(), that.getAffiliationId());
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.affiliationId);
    return builder.toHashCode();
  }

  /**
   * needed for JPA/Hibernate if the solutions set is null, a new set is initialized
   *
   * @return the current list (set) of solutions for this affiliation
   */
  public Set<Solution> getSolutions() {
    if (this.solutions == null) {
      this.solutions = new HashSet<>();
    }
    return this.solutions;
  }

  /**
   * needed for JPA/Hibernate if the users set is null, a new set is initialized
   *
   * @return the current list (set) of users for this affiliation
   */
  public Set<User> getUsers() {
    if (this.users == null) {
      this.users = new HashSet<>();
    }
    return this.users;
  }

  /**
   * needed for JPA/Hibernate if the solutions set is null, a new set is initialized, else, it
   * re-uses the previous object then updates the current list (set) of solutions for this
   * affiliation
   *
   * @param solutions the new list(set) of solutions for this affiliation
   */
  public void setSolutions(@NonNull Set<Solution> solutions) {
    this.getSolutions();
    this.solutions.clear();
    this.solutions.addAll(solutions);
  }

  /**
   * needed for JPA/Hibernate if the users set is null, a new set is initialized, else, it re-uses
   * the previous object then updates the current list (set) of users for this affiliation
   *
   * @param users the new list(set) of users for this affiliation
   */
  public void setUsers(@NonNull Set<User> users) {
    this.getUsers();
    this.users.clear();
    this.users.addAll(users);
  }

  /**
   * needed for JPA/Hibernate a user, if not null, is added to the current list of users for this
   * affiliation
   *
   * @param user
   */
  public void addUser(@NonNull User user) {
    this.getUsers().add(user);
    user.setAffiliation(this);
  }

  /**
   * needed for JPA/Hibernate a user, if not null, is removed from the current list of users for
   * this affiliation
   *
   * @param user
   */
  public void removeUser(@NonNull User user) {
    this.getUsers().remove(user);
    user.setAffiliation(null);
  }
}
