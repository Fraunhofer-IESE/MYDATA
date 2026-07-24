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

package de.fraunhofer.iese.mydata.pmp.cache;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.timer.Timer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * Keeps a copy of a timer list
 */
public class TimerCache implements ITimerCache {

  /**
   * The logger
   */
  private static final Logger LOG = LoggerFactory.getLogger(TimerCache.class);

  private final transient Supplier<Instant> instantSupplier;

  /**
   * The file name to persist the cache
   */
  //  @Value("${mydata.cache.timer_file_path:tcache.json}")
  private transient String cachePath = "tcache.json";

  /**
   * The maximum duration for which the cache is valid
   */
  private transient Duration maxCacheAge;

  /**
   * The date when the cache was last updated or validated
   */
  private Long lastUpdate;

  /**
   * The cached policies
   */
  private Set<Timer> deployedTimerCache = null;

  public TimerCache() {
    this(Instant::now);
  }

  private TimerCache(Supplier<Instant> instantSupplier) {
    this.instantSupplier = Objects.requireNonNull(instantSupplier);
  }

  public TimerCache(Supplier<Instant> instantSupplier, String cachePath,
      @Nullable Duration maxCacheAge) {
    this(instantSupplier);
    this.cachePath = StringUtils.isNotBlank(cachePath) ? cachePath : this.cachePath;
    this.maxCacheAge = maxCacheAge;
    this.loadCache();
  }

  @Override
  public Optional<Set<Timer>> getTimers() {
    if (!this.isValid()) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.deployedTimerCache);
  }

  @Override
  public boolean isValid() {
    if (this.lastUpdate == null || this.deployedTimerCache == null) {
      LOG.trace("Cache is invalid");
      return false;
    }

    if (this.maxCacheAge != null) {
      final Instant now = this.instantSupplier.get();
      final long diffInMillis = (now.toEpochMilli() - this.lastUpdate);

      if (diffInMillis > this.maxCacheAge.getSeconds() * 1000) {
        LOG.debug("Cache is expired");
        this.invalidate();
        return false;
      }
    }

    return true;
  }

  @Override
  public void invalidate() {
    this.lastUpdate = null;
    this.deployedTimerCache = null;
    this.storeCache();
    LOG.debug("Cache invalidated");
  }

  @Override
  public void validate() {
    if (this.deployedTimerCache == null) {
      throw new IllegalStateException("Empty cache cannot be validated");
    }
    final Instant now = this.instantSupplier.get();
    this.lastUpdate = now.toEpochMilli();
    LOG.debug("Cache validated at {}", now);
  }

  @Override
  public boolean updateCache(Set<Timer> policies) {
    if (policies == null) {
      throw new IllegalArgumentException("Timer list must not be null");
    }
    this.deployedTimerCache = policies;
    final Instant now = this.instantSupplier.get();
    this.lastUpdate = now.toEpochMilli();
    final boolean success = this.storeCache();
    LOG.debug("Cache updated at {}", now);
    return success;
  }

  private void loadCache() {
    if (this.cachePath == null) {
      return;
    }
    try {
      final String fileContent = new String(Files.readAllBytes(Paths.get(this.cachePath)));
      final TimerCache c = MyDataEntity.getGson().fromJson(fileContent, TimerCache.class);
      if (c != null) {
        this.lastUpdate = c.getLastUpdateInternal();
        this.deployedTimerCache = c.getTimersInternal();
        return;
      }
    } catch (final IOException e) {
      LOG.debug("Could not read cache", e);
    }
    this.lastUpdate = null;
    this.deployedTimerCache = null;
  }

  private boolean storeCache() {
    if (this.cachePath == null) {
      return true;
    }

    try {
      Files.write(Paths.get(this.cachePath), MyDataEntity.getGson().toJson(this).getBytes());
    } catch (final IOException e) {
      LOG.warn("Could not write cache", e);
      return false;
    }
    return true;
  }

  private Long getLastUpdateInternal() {
    return this.lastUpdate;
  }

  private Set<Timer> getTimersInternal() {
    return this.deployedTimerCache;
  }

}
