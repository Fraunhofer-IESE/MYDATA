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

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "mydataEntityManagerFactory", transactionManagerRef = "mydataTransactionManager", basePackages = {
    "de.fraunhofer.iese.mydata"
})
public class MyDataDbConfiguration {

  @Bean(name = "mydataDataSourceProperties", defaultCandidate = false)
  @ConfigurationProperties("mydata.datasource")
  DataSourceProperties mydataDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(value = "mydataJpaProperties", defaultCandidate = false)
  @ConfigurationProperties("mydata.jpa")
  JpaProperties mydataJpaProperties() {
    return new JpaProperties();
  }

  @Bean(name = "mydataDataSource", defaultCandidate = false)
  @ConfigurationProperties("mydata.datasource.configuration")
  HikariDataSource mydataDataSource(
      @Qualifier("mydataDataSourceProperties") DataSourceProperties mydataDataSourceProperties) {
    return mydataDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class)
        .build();
  }

  @Bean(name = "mydataEntityManagerFactory", defaultCandidate = false)
  LocalContainerEntityManagerFactoryBean mydataEntityManagerFactory(
      @Qualifier("mydataDataSource") DataSource mydataDataSource,
      @Qualifier("mydataJpaProperties") JpaProperties mydataJpaProperties) {
    final EntityManagerFactoryBuilder builder = this
        .createEntityManagerFactoryBuilder(mydataJpaProperties);
    return builder.dataSource(mydataDataSource).packages("de.fraunhofer.iese.mydata")
        .persistenceUnit("mydata").build();
  }

  private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(
      JpaProperties jpaProperties) {
    final JpaVendorAdapter jpaVendorAdapter = this.createJpaVendorAdapter(jpaProperties);
    final Function<DataSource, Map<String, ?>> jpaPropertiesFactory = (dataSource) -> this
        .createJpaProperties(dataSource, jpaProperties.getProperties());
    return new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaPropertiesFactory, null);
  }

  private JpaVendorAdapter createJpaVendorAdapter(JpaProperties jpaProperties) {
    final HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
    adapter.setShowSql(jpaProperties.isShowSql());
    adapter.setDatabase(jpaProperties.getDatabase());
    adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
    adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
    return adapter;
  }

  private Map<String, ?> createJpaProperties(DataSource dataSource,
      Map<String, ?> existingProperties) {
    final Map<String, ?> jpaProperties = new LinkedHashMap<>(existingProperties);
    return jpaProperties;
  }

  @Bean(name = "mydataTransactionManager", defaultCandidate = false)
  PlatformTransactionManager mydataTransactionManager(
      @Qualifier("mydataEntityManagerFactory") EntityManagerFactory mydataEntityManagerFactory) {
    return new JpaTransactionManager(mydataEntityManagerFactory);
  }

}
