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

package de.fraunhofer.iese.mydata.user;

import de.fraunhofer.iese.mydata.affiliation.Affiliation;
import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.solution.Solution;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * MyData-user compatible with spring-security.
 */
@Entity
@Table(name = "mydatauser", indexes = {
    @Index(columnList = "user_uuid", unique = true), @Index(columnList = "username")
})

@Getter
@Setter
public class User extends MyDataEntity {

  @Id
  @Column(name = "user_uuid")
  @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
  protected String userUUID = UUID.randomUUID().toString();

  @NotBlank
  @Column(name = "username", unique = true)
  protected String username;

  /**
   * Email is not kept unique intentionally
   */
  @NotBlank
  @Column
  @Email
  protected String email;

  @NotBlank
  @Column
  protected String firstName;

  @NotBlank
  @Column
  protected String lastName;

  @NotBlank
  @Column(length = 200)
  @Hide
  protected String encryptedPassword;

  @Column
  protected boolean accountLocked;

  /**
   * Password wont be stored in database, used for sending passwords via UI to backend
   */
  @Transient
  protected String password;

  @NotNull
  @Valid
  @Enumerated(value = EnumType.STRING)
  protected MyDataRole role;

  @Column
  private Date lastLogin;

  @Column
  private int failedAttempt = 0;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "affiliation_id")
  private Affiliation affiliation;

  @ManyToMany(mappedBy = "users", cascade = {
      CascadeType.PERSIST, CascadeType.MERGE
  })
  @Hide
  private Set<Solution> solutions;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final User that = (User) o;

    if (this.userUUID != null ? !this.userUUID.equals(that.userUUID) : that.userUUID != null) {
      return false;
    }
    return this.username != null ? this.username.equals(that.username) : that.username == null;
  }

  @Override
  public int hashCode() {
    int result = this.userUUID != null ? this.userUUID.hashCode() : 0;
    result = 31 * result + (this.username != null ? this.username.hashCode() : 0);
    return result;
  }

  public boolean isSuperAdmin() {
    return this.role == MyDataRole.SUPER_ADMIN;
  }

  public boolean isAdmin() {
    return this.role == MyDataRole.ADMINISTRATOR;
  }

  public boolean isDev() {
    return this.role == MyDataRole.SOLUTION_DEVELOPER;
  }

  public void setAccountLocked(final boolean accountLocked) {
    this.accountLocked = accountLocked;
  }

  public boolean isAccountNonLocked() {
    return !this.isAccountLocked();
  }

  public boolean isAccountNonExpired() {
    return true;
  }

  public Set<Solution> getSolutions() {
    if (this.solutions == null) {
      this.solutions = new HashSet<>();
    }
    return this.solutions;
  }

  /**
   * needed for JPA/Hibernate if the solutions set is null, a new set is initialized, else, it
   * re-uses the previous object then updates the current list (set) of solutions for this
   * affiliation
   *
   * @param solutions the new list(set) of solutions for this affiliation
   */
  public void setSolutions(Set<Solution> solutions) {
    if (solutions == null) {
      throw new IllegalArgumentException("Solutions can not be null");
    }
    this.getSolutions();
    this.solutions.clear();
    this.solutions.addAll(solutions);
  }

  public void addSolution(Solution solution) {
    if (solution != null) {
      this.getSolutions().add(solution);
    } else {
      throw new IllegalArgumentException("Solution can not be null");
    }
  }

  public void removeSolution(Solution solution) {
    if (solution != null) {
      this.getSolutions().remove(solution);
    } else {
      throw new IllegalArgumentException("Solution can not be null");
    }
  }
}
