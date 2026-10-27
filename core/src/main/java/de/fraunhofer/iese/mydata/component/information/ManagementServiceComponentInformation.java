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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.validation.MyDataComponentInformation;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(indexes = {
    @Index(columnList = "component_id", unique = true)
})
@Getter
@Setter
@MyDataComponentInformation(componentType = ComponentType.MS)
public class ManagementServiceComponentInformation extends MyDataEntity {

  @NotNull
  @Valid
  @EmbeddedId
  protected ComponentId componentId;

  /**
   * A list of {@link URI}s that can be used to communicate with the component.
   */
  @NotEmpty
  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "url")
  @OrderColumn
  protected List<URI> urls;

  /**
   * Default constructor for JPA.
   */
  protected ManagementServiceComponentInformation() {
    // required by JPA
  }

  /**
   * Constructs a {@link ManagementServiceComponentInformation} without URLs or ComponentInterface.
   *
   * @param id the id
   */
  public ManagementServiceComponentInformation(ComponentId id) {
    this(id, null);
  }

  /**
   * Constructs a {@link ManagementServiceComponentInformation}, including its location (
   * {@link URI}) and its usage ({@link MethodInterfaceDescription}).
   *
   * @param id   the id
   * @param urls a list of {@link URI}s that can be used to communicate with the
   *               {@link ManagementServiceComponentInformation}
   */
  public ManagementServiceComponentInformation(@NonNull ComponentId id, List<URI> urls) {
    this.componentId = id;
    this.urls = urls;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ManagementServiceComponentInformation)) {
      return false;
    }

    final ManagementServiceComponentInformation that = (ManagementServiceComponentInformation) o;

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
  public void addUrl(@NonNull URI url) {
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
   * @param urls : the list of urls to be set. It can not be null.
   */
  public void setUrls(@NonNull List<URI> urls) {
    this.getUrls();
    this.urls.clear();
    this.urls.addAll(urls);
  }
}
