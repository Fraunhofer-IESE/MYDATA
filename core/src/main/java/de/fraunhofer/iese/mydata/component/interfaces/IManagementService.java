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

package de.fraunhofer.iese.mydata.component.interfaces;

import de.fraunhofer.iese.mydata.affiliation.IAffiliationService;
import de.fraunhofer.iese.mydata.client.ILibraryClientService;
import de.fraunhofer.iese.mydata.solution.ISolutionService;
import de.fraunhofer.iese.mydata.solution.ITimezoneService;
import de.fraunhofer.iese.mydata.user.ITokenService;
import de.fraunhofer.iese.mydata.user.IUserService;

/**
 * Server Interface of the Policy Management Point. It is mainly responsible to manage solutions and
 * Client PMPs.
 */
public interface IManagementService extends IBasicManagementService, ISolutionService, IUserService,
    IAffiliationService, ILibraryClientService, ITimezoneService, ITokenService {

}
