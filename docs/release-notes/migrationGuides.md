# ![](../assets/images/code-commit.svg) Migration Guides

## Migration from MYDATA Control Technologies 3.x to 4.0

### Prerequisites
* MYDATA Control Technologies 4.0 requires at least Spring 5 (e.g., 5.2.5.RELEASE).
Make sure, that the Spring dependencies in your Project are at least Spring 5.
Example packages: org.springframework.spring-beans, org.springframework.spring-webmvc
* MYDATA Control Technologies 4.0 requires at least Spring Boot 2.2.4.RELEASE.
Example packages: org.springframework.boot.spring-boot-test
* MYDATA Control Technologies 4.0 requires at least Gson 2.8.6 (package com.google.code.gson.gson).

### Step by Step Migration
- Update the version property in the `properties` section of your `pom.xml` file.

```xml
<mydata.version>4.0.0</mydata.version>
```

- Update the package versions and names in your `pom.xml` from ind2uce to mydata:

```xml
<dependency>
	<groupId>de.fraunhofer.iese.mydata</groupId>
	<artifactId>core</artifactId>
	<version>${mydata.version}</version>
</dependency>
<dependency>
	<groupId>de.fraunhofer.iese.mydata</groupId>
	<artifactId>sdk</artifactId>
	<version>${mydata.version}</version>
</dependency>
```

- `Alive check url` is not longer necessary and developers should not use it any longer and remove it throughout the code and properties.

- Change the PMP url by removing the `/ws` at the end, so that just the server name and port remains.

- Replace all old imports (package names changed from ind2uce to mydata). Commonly used imports are now:

```java
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.util.MyDataUtil;
```

- When replacing the old imports, consider replacing the PEP calls too. With MYDATA Control Technologies 4.0 there are a few helper (like [MyDataUtil](../api-sdk/de/fraunhofer/iese/mydata/util/MyDataUtil) that enable an easier handling of PEPs (see also [Using a custom PEP](../sdk/howto_library.html#_enforce_data_using_a_custom_reactive_pep)):

```java
MyPep myPep # ...;
User u # new User("John Doe");
final User enforcedUser;
try {
  final Event event # MyDataUtil.checkedBlockingGet(myPep.enforceUser(user));
  enforcedUser # (User) enforcedEvent.getValueForName("user");
} catch (IOException e) {
  throw new AccessPermissionDeniedException("There is a problem with the communication channel to the PDP. This will result in inhibition.", e);
} catch (InhibitException e) {
  final String msg # e.getMessage();
  throw new AccessPermissionDeniedException("PDP decided to inhibit the access" + (msg !# null ? ": " + msg : "."), e);
} catch (EvaluationUndecidableException e) {
  throw new AccessPermissionDeniedException("PDP reported that the evaluation is undecidable. This will result in inhibition.", e);
} catch (RuntimeException e) { // catch any other RuntimeExceptions
  LOG.error("Exception occured", e);
  throw new AccessPermissionDeniedException("Internal error, have a look into the log files. For now: This will result in inhibition.");
}
System.out.println(enforcedUser.getName());
```

- In your PEP definition, remove old annotations like `@PEPServiceDescription` or `@ProvidedModifiers`. Replace them with `@Modifiers` in case you want to scan all your packages for available modifiers. `@Modifiers(packageNames # {"de.fraunhofer.iese.mydata.pep.modifiers"})` will limit the search scope and provides a better performance. Relevant imports in PEPs are:

```java
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.reactive.common.EventParameter;
import de.fraunhofer.iese.mydata.reactive.common.EventSpecification;
import de.fraunhofer.iese.mydata.reactive.common.Modifiers;
```

- Also in your PEP definition, remove the no longer valid `scope` parameter from the `@EventSpecification`

- Where ever you instantiate and use the `Event` class, remove the usage of the `scope` from the constructor.

- MYDATA Control Technologies 4.0 replaced the `scope` by `solutionId`. For consistency reasons, you can rename existing variable names.

> ℹ️
> The `solutionId`, `componentId` and `clientId` needs to be written in lowercase.

- Replace the initialization of the MYDATA Control Technologies environment according to your needs. In MYDATA Control Technologies 4.0, there are several modes available that are initialized differently. Please read the manual pages to learn all about it: [Working with the library](../sdk/howto_library.html#_working_with_the_library)

- For non Spring Boot applications, the registration of a custom PEP is different. Learn more about it in the manual: link:../sdk/howto_library.html#_enforce_data_using_a_custom_reactive_pep[Register a custom PEP]
