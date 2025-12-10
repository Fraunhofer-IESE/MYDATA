# ![](./assets/images/toolbox.svg) Developing your own Policy Enforcement Point

## Overview

For any MY DATA Control Technologies-enabled system, a so-call "Policy Enforcement Point" (PEP) has to be integrated into the system.
Its main purpose is to intercept or monitor events within the system, fetch a policy decision from the Policy Decision Point (PDP), and to modify the event according to the provided decision.

> **ℹ️ NOTE**
> Whether an event can can be intercepted or monitored depends on the system.
> The main difference is that some events can be seen by a PEP (i.e., monitored), but it cannot be prevented that the event is further executed.

In a nutshell, PEPs work as follows:

1. At some point in the application, the PEP receives an event (e.g., message, event on a bus) or is programmatically called.
2. The PEP transforms it into the [Event](../api-core/de/fraunhofer/iese/mydata/policy/event/Event.html) structure, which consists of an event name, a timestamp and a key-value list of parameters (primitives or Java objects)
3. The PEP serializes the event and sends it to the PDP
4. The PEP receives an [AuthorizationDecision](../api-core/de/fraunhofer/iese/mydata/policy/decision/AuthorizationDecision.html) with instructions based on the currently deployed policies
5. The PEP modifies the event according to the instructions.
If the event has to be inhibited, the PEP throws an [InhibitException](../api-core/de/fraunhofer/iese/mydata/policy/exception/InhibitException.html).


**Basic PEP-PDP Communication**
![](./images/PEP.png)


## PEP Structure

A PEP is composed of two basic subcomponents: **Decision Enforcers** and **Modifiers**.
By default, our SDK offers a Decision Enforcer that works on JSON and a set of generic modifiers.

### Decision Enforcers

The Decision Enforcers task consists in enforcing a decision for a given event.
The `JsonPathDecisionEnforcer` is a concrete implementation of a `DecisionEnforcer` coming with our SDK.
The `JsonPathDecisionEnforcer` enforces the authorization decision on parameters using JsonPath.
Decision Enforcer also knows which modifier methods are registered.

### Modifiers

One of the key features of MY DATA Control Technologies is the ability to modify events.
It is implemented by so-called **Modifiers** that are used for changing the value of specific parameters of the event.
By default, several generic modifiers are available, e.g.:

- **Delete**: Delete an event parameter or parts of a complex parameter using JsonPath
- **Replace**: Replaces an event parameter or parts of a complex parameter using JsonPath
- **Anagram**: Rearranges the letters of an event parameter or parts of a complex parameter using JsonPath
- **Append**: Appends prefix or suffix to an event parameter or parts of a complex parameter using JsonPath

Custom modifiers can be developed by implementing the [ModifierMethod](../api-sdk/de/fraunhofer/iese/mydata/pep/common/ModifierMethod.html) interface.
The following example shows the AnagramModifier:

```java
public class AnagramModifierMethod implements ModifierMethod {
    public AnagramModifierMethod() {
    }

    public DocumentContext doModification(DocumentContext documentContext, String expression, ParameterList parameterList) {
        Object percentage = parameterList.getParameterValueForName("percentage");
        int percentageParam = percentage != null?Integer.parseInt(String.valueOf(percentage)):0;
        return this.anagram(documentContext, expression, percentageParam);
    }

    @ActionDescription(description = 
    	  "Jumbles up the letters of the word so string does not make any sense",
        pepSupportedType = String.class
    )
    public DocumentContext Anagram(DocumentContext documentContext, String expression, @ActionParameterDescription(name = "percentage", description = "percentage of String to be modified (value between 0 and 100)", mandatory = true) int percentage) {
        final MapFunction blur = (o, configuration) -> this.scramble(o.toString(), percentage);
        return documentContext.map(expression, blur);
    }

    public String getDisplayName() {
        return "anagram";
    }

    private String scramble(String inputString, int count) {
        Random random = new Random();
        char[] a = inputString.toCharArray();

        final int limit = Math.round((a.length * (Math.min(count, 100) / 100f)));

		  for (int i = 0; i < Math.min(a.length - 1, limit); i++) {
        	  final int j = random.nextInt(Math.min(a.length - 1, limit));
        	  final char temp = a[i];
      		  a[i] = a[j];
      		  a[j] = temp;
    	  }
    	  final String result = new String(a);

    	  return result;
    }
}
```

Explanation of the above example:

* `doModification` is the method that is called to modify an object using the following parameters
  * `DocumentContext` is the compiled version of the object from _JsonPath_ in JSON format
  * `parameterList` contains parameters that are specified in the policy
  * `expression` is the _JsonPath_ expression that is specified in the policy
* `getDisplayName` denotes the name of modifier that is visible during policy specification
* `@ActionDescription` is used by the PEP to describe what this modifier method would do.
Its attribute `description` specifies what it does and `pepSupportedType` specifies which data types are supported by this modifier
* `@ActionParameterDescription` specifies details of parameters that can be specified in the policy and passed in `AuthorizationDecision`.
If the `name` attribute is not specified, then it takes the name of the parameter.


## Specification & Registration

During initialization, the PEP needs to know which Decision Enforcer, interface descriptor and modifiers are used.
Using that information, the PEP has to be registered with the Policy Management Point (ideally via the [IMyDataEnvironment](howto_library.html#component-registration) abstraction).
All information regarding modifiers, interfaces and object structures (passed to the PDP) are passed to the PMP.
This information is used for policy specification.
Using java reflection, all this information is automatically registered.


A `DocumentationAPI` interface must be specified for registering it with the PMP. A sample interface may look as follows:


```java
@Modifiers(classNames = {
    AppendModifierMethod.class, ReplaceModifierMethod.class, AnagramModifierMethod.class, DeleteModifierMethod.class
})
public interface RxPEPDocumentationAPI {

}
```



Annotation `@Modifiers` specifies the association with the event, which will be used to register the PEP at the PMP. `@Modifier` will be initialized if it has a default constructor.

There are several ways to specify the `@Modifiers` annotation:

- `@Modifiers(classNames = {AppendModifierMethod.class})`: It is used to specify the exact modifier classes.
- `@Modifiers(packageNames = {"de.fraunhofer.iese.mydata.pep.modifiers"})`: It is used to specify a list of packages.
All existing modifiers in these packages will be added.
- `@Modifiers`: It is used to add all available modifiers of all packages.

The DocumentationAPI interfaces can be used in conjunction with the MY DATA Control Technologies Library.
For more information, click
[here](howto_library.html#_enforce_data_using_a_custom_reactive_pep).

### ReactivePEP Registration

Here is an example for `RxPEP` generation and registration:

```java
IMyDataEnvironment myDataEnvironment = MyDataEnvironmentManager.getDefaultEnvironment();
RxPEPDocumentationAPI rxPepAPI = myDataEnvironment.constructAndRegisterCustomPep("reactivePEP", RxPEPDocumentationAPI.class);
```

The methods specified above will generate an implementation for the specified `DocumentationAPI` interface and register with the PMP.

## Events in ReactivePEP

Once the interfaces have been generated, the PEP needs to provide methods for policy enforcement.
Using RxPEP, the return type of an enforcement method is `Observable<Event>` and by subscribing to that `Observable<Event>` one can access the decision.

The DocumentationAPI for `RxPEP` looks as follows:

```java
@Modifiers(packageNames = {"de.fraunhofer.iese.mydata.pep.modifiers.basic"}, classNames = { AddModifierMethod.class })
public interface RxPEPDocumentationAPI {

  // arguments user and address will be used as parameter in event
  @EventSpecification(action = "show-user")
  Observable<Event> enforceUserShow(@EventParameter(name = "user") User user, @EventParameter(name = "address") Address address);

  // arguments user will be used as parameter in event
  @EventSpecification(action = "show-project")
  Observable<Event> enforceProjectShow(@EventParameter(name = "user") User user);
}
```

Explanation of the above annotations:

- Annotation `@EventSpecification` is used for naming any event.
It has one attribute: `action`.
Together with the solution identifier derived from the PEP’s componentId, they are used to uniquely identify any event within the given system boundary.
- Annotation `@EventParameter` is used for naming the object that is passed via the event.

Once these methods are declared, they can be accessed using the `rxPepAPI` object.
The following example shows how the `enforceProjectShow()` interface method can be used in the target system using a `rxPepAPI` object

```java
rxPepAPI.enforceProjectShow(getUser())
	.subscribe((event) -> {
		User user = (User) event.getValueForName("user");
		log.info("Modified user name is ", user.getName());
	}, (throwable) -> {
		log.info("Error occurred while processing enforceProjectShow" + throwable.getMessage());
	});
```

These calls are non-blocking.
A blocking call it can be issued with `toBlocking()` in the following manner:

```java
rxPepAPI.enforceProjectShow(getUser()).toBlocking()
	.subscribe((event) -> {
		User user = (User) event.getValueForName("user");
		log.info("Modified user name is ", user.getName());
	}, (throwable) -> {
		log.info("Error occurred while processing enforceProjectShow" + throwable.getMessage());
	});
```

## Developing a PEP with our Spring SDK

Detailed information on how to develop PEPs with our Spring SDK can be found [here](howto_library.html#using-the-library-with-spring).