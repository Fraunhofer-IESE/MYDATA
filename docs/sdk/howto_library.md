# ![](../assets/images/toolbox.svg) The MYDATA Library

## Overview

MYDATA Control Technologies Library brings MYDATA Control Technologies to your Java application.
Regardless of whether you want to use it locally inside a single application, on-premise or with our cloud services.
MYDATA Control Technologies supports four operational modes and provides you a strong API to easily integrate MYDATA Control Technologies in your system.

## Operational Modes

The four operational modes:

- **Local**: <a name="local-mode"></a>
In this mode, PMP, PDP and PEPs reside in the same Java application.
PIPs and PXPs can be part of this Java application ("**_local components_**") or reside outside the Java application.
In the latter case, they will have to be accessible via HTTP
and will be registered in the Library as "**_unmanaged components_**".

> **⚠️ WARNING**
> Policies, timers and components are not persisted in this mode.
> You will have to add and deploy your policies on each application startup.
> You will also have to provide and register the components on every startup.

- **Local with File-Sync**:
This mode is quite similar to the [local mode](#local-mode).
But in this mode, the set of deployed policies and timers is managed via the filesystem.
They are persisted as XML formatted files in a configurable directory and thus will survive an application restart.
Nevertheless, components still need to be provided and registered on every startup.

> **ℹ️ NOTE**
> File-Sync relies on custom file extensions to distinguish policies and timers.
> Use `.mdpx` extension for policies and `.mdtx` extension for timers.

- **Local with Cloud-Sync**:
This mode is quite similar to the [local mode](#local-mode).
But in this mode, the set of deployed policies and timers is managed by a Cloud-PMP (**"Management Service"**).
You can add, deploy, revoke and delete the policies and timers there and your application instances (we call them **"library-clients"**) will periodically synchronize with the Cloud-PMP.
Nevertheless, components still need to be provided and registered on every startup as the whole processing resides in your application instance.

> **ℹ️ NOTE**
> To provide rich information about the available PEP/PIP/PXPs, exactly one library-client can be promoted as **"master-library-client"**.
> The master-library-client will automatically push any component registration to the Cloud-PMP.
> This information can be used - for example - by a policy editor to support you specifying policies by suggesting available elements (e.g. events, actions, information sources, available modifiers).

- **Cloud**:
In this mode, the Library uses Cloud-PMP and Cloud-PDP.
This means: Policy evaluation and decision making is done by remote components.
One may ask what benefits come in when choosing this operational mode:
  * persistent policies, timers and component registrations
  * support for multi-tenancy (using Affiliations and Solutions)
  * easier access to the log files
  * scale the PDP according to your needs
  * your application instances will need less resources
  * you can profit from a unified EventHistory (in contrast: when using Local-PDP, each one will have its own "local" database and counting)

  But these benefits do not come at no cost:
  * need for a steady connection to the cloud
  * increase in network traffic and response time
  * probably sensitive data will be transferred to remote locations (need to secure that communication and trust the remote server)
  * PIPs and PXPs need to be accessible for the Cloud-PMP and Cloud-PDP (e.g. via HTTP) and thus need to be exposed appropriately.

  In Cloud mode it makes no sense to use "local components" - indeed, they are only accessible inside your application and the cloud can not communicate with them.
  When you want to expose a PIP or PXP, you can use our mechanism for **"managed components"** (see [Component Registration](#registration-of-components-eg-pips-and-pxps)).

> **ℹ️ NOTE**
> You do not necessarily need to bundle all the PIPs and PXPs with your application.
> Subsequent registrations of the same component will override the existing registration.
> Just make sure, that they are available and registered at runtime.
> When you want to scale your PIP/PXPs, you will have to provide a load balancer and register its address to the Cloud-PMP (instead of the address of a concrete instance).

## The IMyDataEnvironment Interface

`IMyDataEnvironment` encapsulates the MYDATA Control Technologies components and supports the previously introduced operation modes.
It is a central part of the MYDATA Control Technologies Library and enables developers to implement PEPs, PIPs and PXPs.
To do so, it provides access to the PMP and some really practical methods to ease the use.

You can find the corresponding API-Documentation here:
[IMyDataEnvironment](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html)

The following sections describe how to use it.

## About sdk and sdk.spring Maven Artifacts

MYDATA Control Technologies Library can be used with or without Spring.
If you use Spring, you will benefit from numerous features and simplified functions.
For example:

- **Autoconfiguration** and **Initialization** of the Library.
- **Autoregistration** and **Instantiation** of PEPs
- **Autoregistration** and **Instantiation** of PIPs and PXPs.
- **Exposure of PIPs/PXPs** as remote components when using cloud mode.
- **Event History** feature to enhance the supported expressiveness of policies.

### Configuration without Spring

If you choose to use it without Spring, please adapt the following configurations:

Version Property to add in the `properties` Section of your `pom.xml`:
```xml
<mydata.version>4.0.0</mydata.version>
```

Dependency to add in the `dependencies` Section
```xml
<dependency>
    <groupId>de.fraunhofer.iese.mydata</groupId>
    <artifactId>sdk</artifactId>
    <version>${mydata.version}</version>
</dependency>
```

Additionally, the `pdp` dependency has to be added if your application uses one of our local modes:

```xml
<dependency>
    <groupId>de.fraunhofer.iese.mydata</groupId>
    <artifactId>pdp</artifactId>
    <version>${mydata.version}</version>
</dependency>
```
This artifact contains the components necessary for local policy evaluation and decision-making.

The `connectors.rest` dependency has to be added if your application uses the cloud-sync mode, cloud mode, or if it needs remote components accessible via HTTP:

```xml
<dependency>
    <groupId>de.fraunhofer.iese.mydata</groupId>
    <artifactId>connectors.rest</artifactId>
    <version>${mydata.version}</version>
</dependency>
```

This artifact contains the connectors required to communicate with MYDATA Control Technologies components via HTTP.

### Configuration with Spring

To use the MYDATA Control Technologies Library in combination with Spring, simply add this dependency (additional to the items from the previous section) to your `pom.xml`:

```xml
<dependency>
    <groupId>de.fraunhofer.iese.mydata</groupId>
    <artifactId>sdk.spring</artifactId>
    <version>${mydata.version}</version>
</dependency>
```

> **ℹ️ NOTE**
> If you are using the library in cloud mode and want to benefit from automatic exposure of PIP/PXP components, you also have to add a dependency for `spring-boot-starter-web`.

## Working with the Library

### Using the Library without Spring

If you decided to use the MYDATA Control Technologies Library without Spring, you have to configure and initialize the
`IMyDataEnvironment` by yourself:

#### Manual Instantiation of the `IMyDataEnvironment`

Let’s assume that you want to use the MYDATA Control Technologies Library in [**local mode**](#operationalmodes):

**Initialization of the IMyDataEnvironment in local mode**
```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager
    .constructDefaultEnvironment()
    .initializeLocal(
        new SolutionId("urn:solution:my-solution"), // The solutionId
        "Europe/Berlin", // Timezone
        4, // Number of Threads for Policy Evaluation by the PDP
        false, // whether whitelistMode should be enabled
        null // instance of IEventRepository to support Event History
    );
```

That’s it, you successfully initialized the MYDATA Control Technologies Library.

> **ℹ️ NOTE**
> Please note that we supply `null` as a parameter to the initialize method as the Event History is currently only available in combination with Spring.
> For further information on how to enable this feature with our `sdk.spring`, please have a look at: [Event History](#event-history).

In order to initialize the MYDATA Control Technologies Library with another operational mode, just choose the appropriate initialize method and provide the required parameters.
A more detailed documentation about the available initializer methods and the required arguments can be found in the corresponding
[Javadoc](../api-sdk/de/fraunhofer/iese/mydata/internal/IMyDataEnvironmentInitializer.html).

Some more examples:

**Initialization of the IMyDataEnvironment in local with file-sync mode**
```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager
    .constructDefaultEnvironment()
    .initializeLocalWithFileSync(
        new SolutionId("urn:solution:my-solution"), // The solutionId
        "Europe/Berlin", // Timezone
        "data", // file-sync path
        4, // Number of Threads for Policy Evaluation by the PDP
        false, // whether whitelistMode should be enabled
        null // instance of IEventRepository to support Event History
    );
```

**Initialization of the IMyDataEnvironment in local with cloud-sync mode**
```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager
    .constructDefaultEnvironment()
    .initializeLocalWithCloudSync(
        new SolutionId("urn:solution:my-solution"), // The solutionId
        URI.create("https://management.mydata-control.de"), // Cloud-PMP
        new OAuthCredentials(
            new ClientId("urn:client:my-solution:my-client"),
            "my-client-secret",
            URI.create("https://management.mydata-control.de")
        ),
        "Europe/Berlin", // Timezone
        true, // enable cache
        "policyCachePath.json", // policy cache file
        "timerCachePath.json", // timer cache file
        "PT30M", // maxPolicyAge: 30 minutes
        "0 0/5 * * * ?", // syncSchedule: every 5 minutes
        true, // this client is a master-library-client
        4, // Number of Threads for Policy Evaluation by the PDP
        false, // whether whitelistMode should be enabled
        null // instance of IEventRepository to support Event History
    );
```

**Initialization of the IMyDataEnvironment in cloud mode**
```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager
    .constructDefaultEnvironment()
    .initializeCloud(
        new SolutionId("urn:solution:my-solution"), // The solutionId
        URI.create("https://management.mydata-control.de"), // Cloud-PMP
        new OAuthCredentials(
            new ClientId("urn:client:my-solution:my-client"),
            "my-client-secret",
            URI.create("https://management.mydata-control.de")
        ),
    );
```

#### Retrieval of the initialized `IMyDataEnvironment` instance

You can retrieve the initialized `IMyDataEnvironment` instance from anywhere in the Code via `MyDataEnvironmentManager.getDefaultEnvironment()`:

```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
```


> **ℹ️ NOTE**
> Be aware of the fact that trying to retrieve the instance without previously initializing it will result in an `IllegalStateException`.

#### Implementing Components (e.g. PIPs and PXPs)

In this section will be shown, how to implement MYDATA Control Technologies components (PIPs and PXPs).

##### Policy Information Point (PIP)

To learn more about Policy Information Points, click [here](howto_pip.html).

**Implementation of a PIP**
```java
public class AuthorityPip {

  @ActionDescription
  public String getAuthority(
      @ActionParameterDescription(name = "username", mandatory = true) String username
  ) {
    // implement your logic here
    return "guest"; // and return the information
  }
}
```

##### Policy Execution Point (PXP)

To learn more about Policy Execution Points, click [here](howto_pxp.html).

**Implementation of a PXP**
```java
public class MailPxp {

  public MailPxp() {
  }

  @ActionDescription
  public boolean sendPlainMail(
      @ActionParameterDescription(name = "recipient", mandatory = true) final String recipient,
      @ActionParameterDescription(name = "subject", mandatory = true) final String subject,
      @ActionParameterDescription(name = "message", mandatory = true) final String message
  ) {
    // Implementation omitted
    return true; // indicate success
  }
}
```

#### Registration of Components (e.g. PIPs and PXPs)

You will have to pay special attention when registering PIP/PXP components because they need to be accessible to the PDP. Currently we distinguish three modes in which a service component can be registered to the `IMyDataEnvironment`:

- **local:** component instance will be registered to the MyDataEnvironment, will be accessible to local PDP and component information will be published to PMP; no need to provide further information like URL.
-> [`registerLocalPip(...,...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#registerLocalPip(java.lang.String,java.lang.Object)) and [`registerLocalPxp(...,...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#registerLocalPxp(java.lang.String,java.lang.Object))
- **managed:** component instance will be registered to the MyDataEnvironment, can programmatically be retrieved via
[`getManagedPip(...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#getManagedPip(de.fraunhofer.iese.mydata.component.ComponentId)) / [`getManagedPxp(...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#getManagedPxp(de.fraunhofer.iese.mydata.component.ComponentId)) and the corresponding component information is published to the PMP.
-> [`registerManagedPip(...,...,...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#registerManagedPip(java.lang.String,java.lang.Object,java.util.List)) and [`registerManagedPxp(...,...,...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#registerManagedPxp(java.lang.String,java.lang.Object,java.util.List))
**You are in charge to make the component accessible for the PDP under the specified URL.**
- **unmanaged:** component will not be managed by the MyDataEnvironment, only the component information is published to the PMP.
-> [`registerUnmanagedPip(...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#registerUnmanagedPip(de.fraunhofer.iese.mydata.component.information.PipComponentInformation)) and [`registerUnmanagedPxp(...)`](../api-sdk/de/fraunhofer/iese/mydata/IMyDataEnvironment.html#registerUnmanagedPxp(de.fraunhofer.iese.mydata.component.information.PxpComponentInformation))
**You are in charge to make the component accessible for the PDP.**

> **⚠️ WARNING**
> In case of _managed_ and _unmanaged_ component registrations:
> You are in charge to make the component accessible for the PDP.


**Simple example: Registration of a local PIP**
```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
try {
  final ComponentId componentId = myDataEnvironment.registerLocalPip("authoritypip", new AuthorityPip());
} catch (InvalidEntityException | ResourceUpdateException | ConflictingResourceException | IOException e) {
  // place proper exception handling here
  LOG.error(e.getMessage(), e);
}
```

The PIP action can be referenced as `urn:info:my-solution:getAuthority` in the policies.
The solution identifier is determined by the configuration of the IMyDataEnvironment.
Analogous the ComponentId of the PIP is composed of the solution of the IMyDataEnvironment and the supplied componentName.
In this example, the resulting componentId will be `urn:component:my-solution:pip:authoritypip`.

#### Deploying Policies

You can deploy policies programmatically by using the PMP:

```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
IBasicManagementService pmp = myDataEnvironment.getPmp();
final String mydataPolicyString = "<policy ...>...</policy>";
try {
  Policy policy = new Policy(mydataPolicyString)
  PolicyId policyId = pmp.addPolicy(policy);
  pmp.deployPolicy(policyId);
} catch (IOException | ResourceUpdateException | InvalidEntityException | ConflictingResourceException | NoSuchEntityException e) {
  // place proper exception handling here
  LOG.error(e.getMessage(), e);
}
```

To learn more about our policy language, have a look at: [MYDATA Control Technologies Policy Language](https://developer.mydata-control.de/language/)

#### Enforce Data with a Policy Enforcement Point (PEP)

To enforce the policies you can use a PEP. A PEP roughly works as follows:

- You call the PEP with the data to enforce.
- PEP asks PDP for an `AuthorizationDecision` that contains information on how to proceed with the data according to the currently deployed policies.
- PEP applies the `AuthorizationDecision` (the contained instructions) to the data.
- PEP returns modified data or throws an Exception (e.g., in case of inhibition decision).

To learn more about MYDATA Control Technologies PEPs, click [here](howto_pep.html).

##### Enforce Data using a generic PEP

The `IMyDataEnvironment` instance comes with a generic default PEP that you can use to enforce `Event` objects.

```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
IPolicyEnforcementPoint pep = myDataEnvironment.getPep();
User u = new User("John Doe");
Event event = new EventBuilder("my-solution", "my-action").withParameter("user", u, User.class).getEvent();
try {
  pep.enforce(event);
  User enforcedUserObject = (User) event.getValueForName("user");
  System.out.println(enforcedUserObject.getName());
} catch (InhibitException | EvaluationUndecidableException | IOException e) {
  // place proper exception handling here
  System.out.println("Access denied");
}
```

> **ℹ️ NOTE**
> There are several Exceptions that can occur when enforcing the data.
> For example an `InhibitException` will be thrown to signal the decision to inhibit the access.
> Make sure you handle [these Exceptions](../api-core/de/fraunhofer/iese/mydata/component/interfaces/IPolicyEnforcementPoint.html#enforce(de.fraunhofer.iese.mydata.policy.event.Event)) appropriately.

##### Enforce Data using a custom reactive PEP

MYDATA Control Technologies Library supports custom reactive (i.e., "home-made") PEPs.
You just need to declare its interface with the according annotations:

**Declare a custom reactive PEP**
```java
@Modifiers // add all available modifiers
public interface MyPep {
  @EventSpecification(action = "my-action")
  Observable<Event> enforceUser(@EventParameter(name = "user") User user);
}
```

> **ℹ️ NOTE**
> Be aware of the fact that every parameter needs to be annotated with `@EventParameter`.

**Registration and Instantiation of a custom reactive PEP**
```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
MyPep myPep;
try{
  myPep = myDataEnvironment.constructAndRegisterCustomPep("my-pep", MyPep.class);
} catch (InitializationException e) {
  // proper exception handling
  // rethrow or initialize myPep with a valid fallback
}
```

> **ℹ️ NOTE**
> Call the IMyDataEnvironment::constructAndRegisterCustomPep at most once per componentId / componentName / interface.

**Using the custom reactive PEP (potentially unsafe)**
```java
MyPep myPep = ...;
User u = new User("John Doe");
final User enforcedUser;
try {
  final Observable<Event> eventObservable = myPep.enforceUser(u); // enforce
  final Event enforcedEvent = eventObservable.toBlocking().first(); // may throw RuntimeException
  enforcedUser = (User) enforcedEvent.getValueForName("user");
} catch (RuntimeException e) {
  if (e.getCause() instanceof InhibitException) {
    final String msg = e.getCause().getMessage();
    throw new AccessPermissionDeniedException("PDP decided to inhibit the access" + (msg != null ? ": " + msg : "."), e.getCause());
  } else if (e.getCause() instanceof EvaluationUndecidableException) {
    throw new AccessPermissionDeniedException("PDP reported that the evaluation is undecidable. This will result in inhibition.", e.getCause());
  } else if (e.getCause() instanceof IOException) {
    throw new AccessPermissionDeniedException("There is a problem with the communication channel to the PDP. This will result in inhibition.", e.getCause());
  } else {
    LOG.error("Unexpected Exception occured", e);
    throw new AccessPermissionDeniedException("Internal error, have a look into the log files. For now: This will result in inhibition.");
  }
}
System.out.println(enforcedUser.getName());
```

> **⚠️ WARNING**
> The checked Exceptions a "normal PEP" declares to throw will become unchecked RuntimeExceptions when leaving the "reactive world" by blocking for a result.
> Your IDE will not support you in catching all relevant Exceptions.
> To overcome this drawback we provide a helper function ([`MyDataUtil::checkedBlockingGet`](../api-sdk/de/fraunhofer/iese/mydata/util/MyDataUtil.html#checkedBlockingGet(rx.Observable))) that enables you to block safely for a result.
> To do so, it has several _throws declarations_ to assist you in appropriately declaring the catch clauses.

**Using the custom reactive PEP (safe)**
```java
MyPep myPep = ...;
User u = new User("John Doe");
final User enforcedUser;
try {
  final Event event = MyDataUtil.checkedBlockingGet(myPep.enforceUser(user));
  enforcedUser = (User) enforcedEvent.getValueForName("user");
} catch (IOException e) {
  throw new AccessPermissionDeniedException("There is a problem with the communication channel to the PDP. This will result in inhibition.", e);
} catch (InhibitException e) {
  final String msg = e.getMessage();
  throw new AccessPermissionDeniedException("PDP decided to inhibit the access" + (msg != null ? ": " + msg : "."), e);
} catch (EvaluationUndecidableException e) {
  throw new AccessPermissionDeniedException("PDP reported that the evaluation is undecidable. This will result in inhibition.", e);
} catch (RuntimeException e) { // catch any other RuntimeExceptions
  LOG.error("Exception occured", e);
  throw new AccessPermissionDeniedException("Internal error, have a look into the log files. For now: This will result in inhibition.");
}
System.out.println(enforcedUser.getName());
```

You may wonder why we provide reactive PEPs when blocking for their result.
Sometimes we have to do this to support legacy APIs that do not support Observables and need the enforced data to proceed.
When you use reactive programming in your application and you do not have to block for the results, everything is fine.
Just keep an eye on appropriate error handling.

### Using the Library with Spring

In order to use MYDATA Control Technologies Library with Spring, you need to include the appropriate [maven dependency](#about-sdk-and-sdkspring-maven-artifacts).

#### Configuration of the Library (application.yml)

You can configure the settings such as the operational mode via the `.yml` configuration file.

**Available Configuration Properties (application.yml)**

|Property |Relevant for Op-Modes |Default Value |Possible Values |Description
| -- | -- | -- | -- | -- |
|mydata.operational-mode|*|local|local, local-with-file-sync, local-with-cloud-sync, cloud |Determines the operational mode of the library|
|mydata.solution|*|urn:solution:default|urn:solution:*|Determines the SolutionId of the default MyDataEnvironment|
|mydata.timezone|local, local-with-file-sync, local-with-cloud-sync|Europe/Berlin|ZoneId-Strings|Determines the timezone of the solution of the default MyDataEnvironment|
|mydata.pdp.num-threads|local, local-with-file-sync, local-with-cloud-sync|4|integer, >=1|How many threads to use in the PDP|
|mydata.pdp.enable-whitelist-mode|local, local-with-file-sync, local-with-cloud-sync|false|true / false|Whether to enable our whitelist mode|
|mydata.sync.file-sync.path|local-with-file-sync||path to a folder|Where to look for policies and timers|
|mydata.pmp.cloud.url|local-with-cloud-sync, cloud||concrete URL (without trailing slash), e.g.:https://management.mydata-control.de|Endpoint of the Cloud-PMP|
|mydata.pmp.cloud.client-id|local-with-cloud-sync, cloud||urn:client:*|ClientId|
|mydata.pmp.cloud.client-secret|local-with-cloud-sync, cloud||*|Corresponding Client Secret|
|mydata.sync.cloud-sync.cache.enabled|local-with-cloud-sync|false|true / false|Whether to enable cache|
|mydata.sync.cloud-sync.cache.policy-file-path|local-with-cloud-sync|pcache.json|Path for json-File|Where to store the policy cache|
|mydata.sync.cloud-sync.cache.timer-file-path|local-with-cloud-sync|tcache.json|Path for json-File|Where to store the timer cache|
|mydata.sync.cloud-sync.schedule|local-with-cloud-sync|0 0/5 * * * ?|https://www.freeformatter.com/cron-expression-generator-quartz.html|Schedule/Interval for CloudSynchronizer|
|mydata.sync.cloud-sync.master-client|local-with-cloud-sync|false|true / false|Whether this library-client is a master-library-client|
|mydata.sync.cloud-sync.max-age|local-with-cloud-sync||Duration accoring to ISO8601, e.g.: PT30M|How long policies and timers received from Cloud-PMP are valid.|
|mydata.external-server-url|cloud||http://my-application.exmaple|Determines how the application is accessible from the outside (used for URL generation when auto-configuring PIP/PXP)|
|mydata.component.path|cloud|||Used to determine the prefix of PIP/PXP-REST-Controllers|
|mydata.datasource.*|||analogous to spring.datasource.*|Configuration for MYDATA Control Technologies Library Persistence (used for the Event History feature)|
|mydata.jpa.*|||analogous to spring.jpa.*|Configuration for MYDATA Control Technologies Library Persistence (used for the Event History feature)|


Let’s assume that you want to use the Library in local mode and use the Spring Data JPA feature.
Your configuration file may look like this:

**Example `application.yml` for local mode**
```yml
spring:
  datasource:
    url: jdbc:h2:file:~/my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    hibernate:
      ddl-auto: update
      hbm2ddl:
        auto: update

mydata:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    properties:
      hibernate:
        ddl-auto: update
        hbm2ddl:
          auto: update
  datasource:
    jdbc-url: jdbc:h2:file:~/mydata-for-my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  operational-mode: local # choose your operational mode
  timezone: Europe/Berlin # determine the timezone
  solution: urn:solution:my-solution # name your solution
  pdp:
    enable-whitelist-mode: false # enable whiteliste-mode?
    num-threads: 4 # how many threads should the PDP use?
```

MYDATA Control Technologies Library uses a separate database that will not interfere with your application.

More examples:

**Example `application.yml` for local with file-sync mode**
```yml
spring:
  datasource:
    url: jdbc:h2:file:~/my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    hibernate:
      ddl-auto: update
      hbm2ddl:
        auto: update

mydata:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    properties:
      hibernate:
        ddl-auto: update
        hbm2ddl:
          auto: update
  datasource:
    jdbc-url: jdbc:h2:file:~/mydata-for-my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  operational-mode: local-with-file-sync
  sync:
    file-sync:
      path: data # where are the policies and timers stored?
  timezone: Europe/Berlin
  solution: urn:solution:my-solution
  pdp:
    enable-whitelist-mode: false
    num-threads: 4
```

**Example `application.yml` for local with cloud-sync mode**
```yml
spring:
  datasource:
    url: jdbc:h2:file:~/my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    hibernate:
      ddl-auto: update
      hbm2ddl:
        auto: update

mydata:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    properties:
      hibernate:
        ddl-auto: update
        hbm2ddl:
          auto: update
  datasource:
    jdbc-url: jdbc:h2:file:~/mydata-for-my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  operational-mode: local-with-cloud-sync
  sync:
    cloud-sync:
      cache:
        enabled: true # maintain a cache to consult when there is a communication problem with the Cloud-PMP
        policy-file-path: pcache.json # where to cache the policies
        timer-file-path: tcache.json # where to cache the timers
      schedule: 0 0/5 * * * ? # cron expression as sync schedule, here: every 5 minutes
      master-client: true # whether this instance is a master-library-client
      max-age: PT30M # Duration, how long a received set of policies and timers is valid (ISO 8601 duration format)
  timezone: Europe/Berlin
  solution: urn:solution:my-solution
  pmp:
    cloud:
      url: https://management.mydata-control.de # address of the Cloud-PMP
      client-id: urn:client:my-solution:my-client # my client-id
      client-secret: my-client-secret # my client-secret
  pdp:
    enable-whitelist-mode: false
    num-threads: 4
```

**Example `application.yml` for cloud mode**
```yml
spring:
  datasource:
    url: jdbc:h2:file:~/my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    hibernate:
      ddl-auto: update
      hbm2ddl:
        auto: update

mydata:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    properties:
      hibernate:
        ddl-auto: update
        hbm2ddl:
          auto: update
  datasource:
    jdbc-url: jdbc:h2:file:~/mydata-for-my-app;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    username: sa
    password:
    platform: h2
    driver-class-name: org.h2.Driver
  operational-mode: cloud
  solution: urn:solution:my-solution
  external-server-url: https://my-application.my-example.de # address under which this application instance can be reached
  component:
    path: mydata-api # path to use for the exposure of PIP/PXP components, result: https://my-application.my-example.de/mydata-api/...
  pmp:
    cloud:
      url: https://management.mydata-control.de
      client-id: urn:client:my-solution:my-client
      client-secret: my-client-secret
```


#### Autoconfiguration and Initialization of the Library with Spring Boot

The initialization of the MYDATA Control Technologies Library is quite easy with Spring Boot as it happens automatically, based on the settings from the configuration file:

.Spring Boot Application
```java
@SpringBootApplication
public class MyApplication {
  public static void main(String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

That’s it.
You can access the initialized `IMyDataEnvironment` instance using Spring’s dependency injection mechanism with
[`@Autowired`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/annotation/Autowired.html):

```java
@Autowired
private IMyDataEnvironment myDataEnvironment;
```

#### Enable more features

To enable more features of the Library, just add the corresponding annotations next to `@SpringBootApplication`.

##### Event History

To enable the Event History feature, add the `@EnableEventHistory` annotation:

**Application with Event History feature enabled**
```java
@SpringBootApplication
@EnableEventHistory
public class MyApplication {
  public static void main(String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

> **ℹ️ NOTE**
> Event History feature uses a database.
> Make sure to add a dependency to `org.springframework.boot:spring-boot-starter-data-jpa` and [configure the database connection](#configuration-of-the-library-applicationyml).
> Further, the use of Event History feature is only meaningful in combination with using one of our local modes and therefore requires the `pdp` dependency (see [Dependencies](#about-sdk-and-sdkspring-maven-artifacts)).

##### Autoregistration and Instantiation of PEPs

Similarly to the autowirable `IMyDataEnvironment` instance, your PEPs can be exposed as beans.
You just need to apply the `@EnablePolicyEnforcementPoint` annotation and provide information where the PEPs should be looked for:

**Application with autoregistration and instantiation of custom PEPs**
```java
@SpringBootApplication
@EnablePolicyEnforcementPoint(basePackages = "com.acme.myapp.pep")
public class MyApplication {
  public static void main(String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

Inside those packages, the mechanism will look for Interfaces annotated with `@PepServiceDescription`:

**Annotate your PEPs so they can be found**
```java
@PepServiceDescription(componentName = "my-pep")
@Modifiers // add all available modifiers
public interface MyPep {
  @EventSpecification(action = "my-action")
  Observable<Event> enforceUser(@EventParameter(name = "user") User user);
}
```

##### Autoregistration and Instantiation of PIPs

**Application with autoregistration and instantiation of PIPs**
```java
@SpringBootApplication
@EnablePolicyInformationPoint
public class MyApplication {
  public static void main(String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

It will look for Classes annotated with `@PipService`:

**Annotation for you PIPs so we can find them**
```java
@PipService(componentName = "authoritypip")
public class AuthorityPip {
  @ActionDescription
  public String getAuthority(
      @ActionParameterDescription(name = "username", mandatory = true) String username
  ) {
    // implement your logic here
    return "guest"; // and return the information
  }
}
```

##### Autoregistration and Instantiation of PXPs

**Application with autoregistration and instantiation of PXPs**
```java
@SpringBootApplication
@EnablePolicyExecutionPoint
public class MyApplication {
  public static void main(String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }
}
```

It will look for Classes annotated with `@PxpService`:

**Annotation for you PIPs so they can be found**
```java
@PxpService(componentName = "mailpxp")
public class MailPxp {
  public MailPxp() {
  }
  @ActionDescription
  public boolean sendPlainMail(
      @ActionParameterDescription(name = "recipient", mandatory = true) final String recipient,
      @ActionParameterDescription(name = "subject", mandatory = true) final String subject,
      @ActionParameterDescription(name = "message", mandatory = true) final String message
  ) {
    // Implementation omitted
    return true; // indicate success
  }
}
```

#### Full-fledged Setup

**Application that enables every feature**
```java
@SpringBootApplication
@EnableEventHistory
@EnablePolicyEnforcementPoint(basePackages = "com.acme.myapp.pep")
@EnablePolicyInformationPoint
@EnablePolicyExecutionPoint
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

## Use the Library

### Example

**Spring Boot Application with local component and hard coded policy**
```java
@SpringBootApplication
@EnablePolicyEnforcementPoint(basePackages = "com.acme.myapp.pep")
@EnablePolicyInformationPoint
@EnablePolicyExecutionPoint
public class MyApplication {
  private static final Logger LOG = LoggerFactory.getLogger(MyApplication.class);

  @Autowired
  private IMyDataEnvironment myDataEnvironment;

  public static void main(String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }

  // TODO this is a hard coded example, just for demonstration purpose, DO NOT USE THIS IN PRODUCTION
  @PostConstruct
  public void init() {
    LOG.info("INIT:begin");
    try {
      // TODO this is a hardcoded policy for demonstration purpose
      myDataEnvironment.getPmp().deployPolicy(myDataEnvironment.getPmp().addPolicy(new Policy(
          "<policy id='urn:policy:my-solution:pxpdemo' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:date='http://www.mydata-control.de/4.0/date' xmlns:time='http://www.mydata-control.de/4.0/time' xmlns:day='http://www.mydata-control.de/4.0/day'>\n" +
              "  <mechanism event='urn:action:my-solution:my-action'>\n" +
              "    <if>\n" +
              "      <constant:true/>\n" +
              "      <then>\n" +
              "        <allow/>\n" +
              "        <execute action='urn:action:my-solution:sendPlainMail'>\n" +
              "          <parameter:string name='recipient' value='recipient@example.com'/>\n" +
              "          <parameter:string name='subject' value='Very important incident'/>\n" +
              "          <parameter:string name='message' value='Something important happened.'/>\n" +
              "        </execute>\n" +
              "      </then>\n" +
              "    </if>\n" +
              "  </mechanism>\n" +
              "</policy>")));
    } catch (IOException | ResourceUpdateException | InvalidEntityException | ConflictingResourceException e) {
      LOG.error(e.getMessage(), e);
    }
    LOG.info("INIT:end");
  }
}
```

**Business Service making use of the PEP**
```java
@Service
public class MyBusinessService {

  private final static Logger LOG = LoggerFactory.getLogger(MyBusinessService.class);
  private final UserRepository userRepository;
  private final MyPep myPep;

  @Autowired
  public MyBusinessService(UserRepository userRepository, MyPep myPep) {
    this.userRepository = userRepository;
    this.myPep = myPep;
  }

  public User getUser(String userId) throws AccessPermissionDeniedException {
    User userFromDb = userRepository.getById(userId);
    return enforceUser(userFromDb);
  }

  private User enforceUser(User user) throws AccessPermissionDeniedException {
    try {
      final Event event = MyDataUtil.checkedBlockingGet(myPep.enforceUser(user));
      return event.getParameterValue("user", User.class);
    } catch (IOException e) {
      throw new AccessPermissionDeniedException("There is a problem with the communication channel to the PDP. This will result in inhibition.", e);
    } catch (InhibitException e) {
      final String msg = e.getMessage();
      throw new AccessPermissionDeniedException("PDP decided to inhibit the access" + (msg != null ? ": " + msg : "."), e);
    } catch (EvaluationUndecidableException e) {
      throw new AccessPermissionDeniedException("PDP reported that the evaluation is undecidable. This will result in inhibition.", e);
    } catch (RuntimeException e) { // catch any other RuntimeExceptions
      LOG.error("Exception occured", e);
      throw new AccessPermissionDeniedException("Internal error, have a look into the log files. For now: This will result in inhibition.");
    }
  }
}
```