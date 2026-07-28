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

package de.fraunhofer.iese.mydata.component.information;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.validation.MyDataComponentInformation;
import de.fraunhofer.iese.mydata.solution.Solution;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(indexes = {
    @Index(columnList = "component_id", unique = true)
})
@Getter
@Setter
@MyDataComponentInformation(componentType = ComponentType.PIP)
public class PipComponentInformation extends MyDataEntity {

  @Valid
  @NotNull
  @EmbeddedId
  protected ComponentId componentId;

  /**
   * A list of {@link URI}s that can be used to communicate with the Component.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "url")
  @OrderColumn
  protected List<URI> urls;

  // optional as components in the library do not need a solution..
  @Hide
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  private Solution solution;

  /**
   * A description of the methods provided by the {@link PipComponentInformation} (i.e., its
   * Interface).
   */
  @OneToMany(orphanRemoval = true, mappedBy = "pipComponentInformation", cascade = CascadeType.ALL)
  @Fetch(FetchMode.SUBSELECT)
  private List<@Valid MethodInterfaceDescription> methodInterfaceDescriptions;

  /**
   * Default constructor for JPA.
   */
  protected PipComponentInformation() {
    // required by JPA
  }

  /**
   * Constructs a {@link PipComponentInformation} without URLs or ComponentInterface.
   *
   * @param id the unique ID of the {@link PipComponentInformation}.
   */
  public PipComponentInformation(ComponentId id) {
    this(id, Collections.emptyList());
  }

  /**
   * Constructs a {@link PipComponentInformation}, including its location ( {@link URI}) and its
   * usage ({@link MethodInterfaceDescription}).
   *
   * @param id                          the unique ID of the {@link PipComponentInformation}
   * @param urls                        a list of {@link URI}s that can be used to communicate with
   *                                      the {@link PipComponentInformation}
   * @param methodInterfaceDescriptions the component interfaces
   */
  public PipComponentInformation(@NonNull ComponentId id, List<URI> urls,
      List<MethodInterfaceDescription> methodInterfaceDescriptions) {
    this.componentId = id;
    this.urls = new ArrayList<>(urls);
    this.methodInterfaceDescriptions = new ArrayList<>(methodInterfaceDescriptions);
    for (final MethodInterfaceDescription mid : methodInterfaceDescriptions) {
      mid.setPipComponentInformation(this);
    }
  }

  /**
   * Constructs a {@link PipComponentInformation}, including its location ( {@link URI}) and its
   * usage ({@link MethodInterfaceDescription}).
   *
   * @param id                 the unique ID of the {@link PipComponentInformation}
   * @param urls               a list of {@link URI}s that can be used to communicate with the
   *                             {@link PipComponentInformation}
   * @param componentInterface a list of methods provided by the {@link PipComponentInformation}
   *                             (i.e., its Interface)
   */
  public PipComponentInformation(ComponentId id, List<URI> urls,
      MethodInterfaceDescription... componentInterface) {
    this(id, urls, Arrays.asList(componentInterface));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof PipComponentInformation)) {
      return false;
    }

    final PipComponentInformation that = (PipComponentInformation) o;

    return new EqualsBuilder().append(this.getComponentId(), that.getComponentId()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getComponentId()).toHashCode();
  }

  /**
   * Adds a URL to the list of {@link URI}s that can be used to communicate with the component.
   *
   * @param url the url to add
   */
  public void addUrl(URI url) {
    if (null == url) {
      return;
    }
    if (this.urls == null) {
      this.urls = new ArrayList<>();
    }
    this.urls.add(url);
  }

  /**
   * Provides a list of URLs of a certain protocol/scheme.
   *
   * @param  protocols the list of protocols to be used linked by an &quot;or&quot; function
   * @return           a list of {@link URI}s that can be used to communicate with the component
   */
  public List<URI> getUrlsForProtocol(String... protocols) {
    if (protocols.length == 0) {
      return this.getUrls();
    }
    final List<URI> result = new ArrayList<>();
    for (final URI url : this.urls) {
      for (final String protocol : protocols) {
        if (protocol.equals(url.getScheme())) {
          result.add(url);
          break;
        }
      }
    }
    return result;
  }

  /**
   * @return the list of method interface descriptions for this PIP
   */
  public List<MethodInterfaceDescription> getMethodInterfaceDescriptions() {
    if (this.methodInterfaceDescriptions == null) {
      this.methodInterfaceDescriptions = new ArrayList<>();
    }
    return this.methodInterfaceDescriptions;
  }

  /**
   * @return the list of URLs for this PIP
   */
  public List<URI> getUrls() {
    if (this.urls == null) {
      this.urls = new ArrayList<>();
    }
    return this.urls;
  }

  /**
   * @param urls : the list of urls to be set for this PIP. It can not be null.
   */
  public void setUrls(@NonNull List<URI> urls) {
    this.getUrls();
    this.urls.clear();
    this.urls.addAll(urls);
  }

  /**
   * @param methodInterfaceDescriptions
   */
  public void setMethodInterfaceDescriptions(
      @NonNull List<MethodInterfaceDescription> methodInterfaceDescriptions) {
    this.getMethodInterfaceDescriptions();
    this.methodInterfaceDescriptions.clear();
    this.methodInterfaceDescriptions.addAll(methodInterfaceDescriptions);
    for (final MethodInterfaceDescription mid : methodInterfaceDescriptions) {
      mid.setPipComponentInformation(this);
    }
  }

}
