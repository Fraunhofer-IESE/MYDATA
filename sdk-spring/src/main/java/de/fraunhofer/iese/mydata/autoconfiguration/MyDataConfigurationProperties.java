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

package de.fraunhofer.iese.mydata.autoconfiguration;

import de.fraunhofer.iese.mydata.OperationalMode;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration options for MYDATA Library, used in some places via Autowiring or Spring EL there
 * are constructs like
 * #{beanFactory.getBean(T(de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties)).component.path}
 * in the code
 */
@ConfigurationProperties("mydata")
@Validated
@Data
public class MyDataConfigurationProperties {

  @NotNull
  private OperationalMode operationalMode = OperationalMode.LOCAL;

  @NotBlank
  private String timezone = "Europe/Berlin";

  /**
   * sensitive point, currently used to determine whether REST-Connectors should be activated
   */
  private String externalServerUrl;

  @Valid
  private Pmp pmp = new Pmp();

  @Valid
  private Pdp pdp = new Pdp();

  @Valid
  private Sync sync = new Sync();

  @Valid
  private SolutionId solution = new SolutionId("urn:solution:default");

  /**
   * sensitive point, currently contains information about the "path" for Component-REST-Connectors
   */
  @Valid
  private Component component = new Component();

  public void setSolution(String solutionIdAsString) {
    this.solution = new SolutionId(solutionIdAsString);
  }

  @Data
  public static class Sync {

    @Valid
    private CloudSync cloudSync = new CloudSync();

    @Valid
    private FileSync fileSync;

    @Data
    public static class CloudSync {

      @Valid
      @NotNull
      private Cache cache = new Cache();

      private String schedule = "0 0/5 * * * ?";

      private boolean masterClient = false;

      private String maxAge;

      @Data
      public static class Cache {
        private boolean enabled = false;

        @NotBlank
        private String policyFilePath = "pcache.json";

        @NotBlank
        private String timerFilePath = "tcache.json";
      }
    }

    @Data
    public static class FileSync {
      @NotBlank
      private String path;
    }
  }

  @Data
  public static class Pdp {
    @Min(1)
    private int numThreads = 4;

    private boolean enableWhitelistMode = false;
  }

  @Data
  public static class Pmp {
    @Valid
    private Cloud cloud;

    @Data
    public static class Cloud {
      @NotBlank
      @URL
      private String url;

      @NotBlank
      private String clientId;

      @NotBlank
      @ToString.Exclude
      private String clientSecret;
    }
  }

  @Data
  public static class Component {
    /**
     * sensitive point, currently used to determine the prefix of REST-Controllers URL-Mapping...
     */
    private String path;
  }

}
