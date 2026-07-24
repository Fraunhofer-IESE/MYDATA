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

package de.fraunhofer.iese.mydata.connectors.rest;

/**
 * The Class Constants.
 */
public final class Constants {
  
  /**
   * The Constant SCHEME.
   */
  public static final String SCHEME = "http";

  /**
   * The Constant HOST.
   */
  public static final String HOST = "127.0.0.1";

  /**
   * The Constant PORT.
   */
  public static final int PORT = 80;

  /**
   * The Constant NAME.
   */
  public static final String NAME = "ws/";

  /**
   * The Constant BASE_URL.
   */
  public static final String BASE_URL = SCHEME + "://" + HOST + ":" + PORT + "/" + NAME;

  /**
   * Instantiates a new constants.
   */
  private Constants() {
  }
}
