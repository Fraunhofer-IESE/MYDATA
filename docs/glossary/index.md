# Glossary

## A



  
### Access Control
Access control is the selective restriction of access to resource (cf. [Wikipedia](https://en.wikipedia.org/wiki/Access_control)). This resource can be, for example, a physical place or a digital data file. Note that access control decisions are typically binary, i.e., access is granted or denied.

### Action
An action labels a certain type of [information](#information--data-flow) or control flow in a system (e.g., "open-file", "delete-file", or "create-screenshot"). 
Abstract actions manifest in concrete [events](#event) (e.g., "'delete-file' readme.txt at 12.03.2018 3:32pm"). For more details, please refer to our [language documentation](../language/#the-event-condition-action-schema).

### Affiliation
MYDATA Control Technologies supports multiple tenants (typically companies). The basic separation of tenants is implemented on the level of affiliations. So, if two tenant have MYDATA Control Technologies accounts, each of them will be treated as separate affiliations and not see any data of the other tenant.

### Anonymization
An anonymization removes personally identifiable information from a data set in an irreversible way. Anonymization is one special type of [modification](#modification) (cf. [Wikipedia](https://en.wikipedia.org/wiki/Data_anonymization)).


### Authorization Decision
The decision the [decision service](#decision-service) makes with respect to an event occurrence based on the deployed [policies](#policy). An decision is enforced by the [PEP](#policy-enforcement-point-pep) (cf. our [language documentation](../language/#then_else)) that intercepted the event. An authorization decision forces an event to be [allowed](#decision), [inhibited](#inhibition), or [modified](#modification).

### API
Application Programming Interface (cf. [Wikipedia](https://en.wikipedia.org/wiki/Application_programming_interface))

### API Token
A key that deals as an authentication credential to use a [REST](#rest) [API](#api) protected by [oAuth](#oauth) (cf. the [oAuth documentation](https://oauth.net/2/bearer-tokens/))


## C


 
### Cardinal
Cardinal aspects inside the conditions of [mechanisms](#mechanism) (as part of a [policies](#policy)) allow the enforcement of [usage rights](#usage-control) based on the number of uses in the past. It enables you to specify policies like "Movie 'The Incredibles' must be watched at most 3 times."). Cardinal aspects can be combined with temporal aspects to restrict the number of uses within time frames. An example would be: “Only 30 songs may be streamed within 24 hours with free account.”

### Condition
A boolean expression that is part of a [mechanism](#mechanism) within a [policy](#policy) (cf. our [language documentation](../language/#if_elseif)). The fulfillment is the baseline for the enforcement of an [Authorization Decision](#decision).

### Component
Components are [PEPs](#policy-enforcement-point-pep), [PXPs](#policy-execution-point-pxp), or [PIPs](#policy-information-point-pip) that can be registered in a [solution](#solution) of MYDATA Control Technologies.

## D


  
### Data Types
See our [language documentation](../language/#data-types) for a list of supported data types.

### Decision
Synonym for [Authorization Decision](#authorization-decision).

### Decision Enforcer
The decision enforcer is the main component of a [PEP](#policy-enforcement-point-pep) that takes an [authorization decision](#authorization-decision) and enforces it by means of [modification](#modification) or [inhibition](#inhibition) of a certain event. Our default decision enforcer works with [JsonPath](#jsonpath) - however you can extend or replace this default implementation.

### Decision Service
The decision service is the rule engine (Policy Decision Point PDP) that evaluates the [policies](#policy) based on an [event](#event) coming from a [PEP](#policy-enforcement-point-pep). The decision services produces one authorization decision per incoming event and may trigger [execute actions](#execute-action) in addition.


## E
  
### End Point
A method of a [REST](#rest) [API](#api).

### Event
An event is an occurance of an action [intercepted](#interception) or [monitored](#monitoring) by a [PEP](#policy-enforcement-point-pep). It is the tiggering part for the [ECA Scheme](../language/#event-condition-action) - the underlying schema of our policies.

An event consists of an action ID which defines the type of the event (e.g., "open-file"), the concrete timestamp the event occurred and a key-value list of event attributes.
 
### Event-Condition-Action
The underlying schema of our policies (cf. our [language documentation](../language/#event-condition-action))

### Event History
A database of historic events processed by the decision engine (cf. our [language documentation](../language/#working_with_the_event_history)). Each event that is relevant for the evaluation of time-based or cardinality-based conditions within mechanisms is stored in the event history for future policy evaluations. For privacy reasons, all attribute values are hashed.

### Execute Action
An execute action is a system action triggered by the PDPdecision service through a policy evaluation and executed by a [Policy Execution Point](#policy-execution-point-pxp).


## F

### Filtering
Filtering is one special type of [modification](#modification), where particular data is removed from an [event](#event).

### Function
Functions represent mappings from input to output variables and are referenced by [operators](#operator) in our policies.
Functions can simple boolean functions (like and, or, not), arithmetic functions (like plus, minus) and others (like count, valueChanged). Learn more [here](../language/#_operators).


## I


  
### Information / Data flow
Data flow talks about the instances and flows of a certain data item inside a system and within systems. For MYDATA Control Technologies, data flow tracking is particularly relevant, as security requirements (implemented by [policies](#policy)) are typically not defined on data (e.g., Invoices), rather than on concrete instances (Invoice_CustomerA_July.pdf). As one particular data item might be stored in different instances (e.g., files, screens), this flow between instances need to be tracked in order to achieve a comprehensive protection of the data.

### Inhibition
If an [event](#event) is inhibited, the PEP prevents the further execution of the event (e.g., access is denied, data flow is stopped).  An inhibition is decided by the decision service based on the active policies and communicated to the PEP within an authorization decision.

### Interception
Ican see the event including its attributes and has the ability to prevent or modify its further flow. This means that monitored events can be allowed, inhibited and modified  a PEP based on a [policy](#policy) evaluation of the decision service.


## J


  
### JsonPath
JsonPath is one of the basic technologies we are using to implement the [modifications](#modification) inside a [PEP](#policy-enforcement-point-pep). It is a tool for analyzing, transforming and selectively extracting data from Json documents. Please refer to [the JsonPath project site](https://github.com/json-path/JsonPath) for the full documentation.



## M


  
### Masking
Masking is one special type of [modification](#modification) (cf. [Wikipedia](https://en.wikipedia.org/wiki/Data_masking)). When data is masked, parts of the data (e.g., one or more attributes of an event) are modified. An example for a data masking is the replacement of the middle digits of an IBAN (“DE786709XXXXXXXXXX4525”).

### Management Service
Our management service provides a web-based user interface for the management of Plugins ([PEPs](#policy-enforcement-point-pep), [PXPs](#policy-execution-point-pxp), [PIPs](#policy-information-point-pip)), [policies](#policy), [users](#user), [solutions](#solution) and [affiliations](#affiliation) (depending on your [role](#roles)). 

Click [here](../ui) for the full documentation.

### Mechanism
A mechanism is one [ECA rule ](#event-condition-action) inside a [policy](#policy).


### Modification
An [event](#event) is basically a named key-value (event attributes) list. 
For example the event "open-file" might contain the name of the user opening the file, and the file itself. 
Classical [access](#access-control) control only allows binary decisions on the event - i.e., the event can be allowed or inhibited. 
MYDATA Control Technologies additionally allows the values of the event to be modified. This means that event attributes can be replaced, deleted, or altered. 
For example, all personal related information must be removed from the file before access is granted (in this case the file is not changed itself, but only the reading data stream).

### Modifier

A modifier is a plugin of a [PEP](#policy-enforcement-point-pep) that extends the PEP’s capabilities of the [modification](#modification) of [event](#event) attributes. 
Modifiers are registered by the PEP and are than available in the [policy](#policy) specification. 
Example modifiers are "delete" (delete certain parts of an event) and "replace" (replace certain parts of the event).
PEPs can be extended by additional modifiers via plugins.

### Monitoring
If an [event](#event) is monitored (and not intercepted), the [PEP](#policy-enforcement-point-pep) can only see the occurrence of an event, but not prevent its further flow. This means that monitored events can only be allowed - a modification or inhibition by a [policy](#policy) is not possible. However, the policy evaluation of a monitored event can enforce the execution of compensating actions in a [PXP](#policy-execution-point-pxp).



## O


  
### oAuth
oAuth 2.0 is a protocol we use for authentication of [REST requests](#rest) to our [management service](#management-service) and [decision service](#decision-service). Learn more on [the oAuth project site](https://oauth.net/2/) and our developer documentation of the [management service](../rest-api/pmp/) and [decision service](../rest-api/pdp/).


### Operator
An operator is a reference to a [function](#function) inside our [policy language](../language). 



## P
  
### Plan
The usage of our [decision service](#decision-service) is limited. You can choose between different pricing models (plans), according to your needs. In particular, the number of request per month is limited (quota). The range goes from 225,000 requests per month (in the "Free" plan) until 7,500,000 requests per month (In the "Gold" plan). If you need more, the "Platinum" plan offers a negotiable pricing.

### Policy
A policy is a set of [mechanisms](#mechanism) (rules) that technically implement security requirement(s). They are used to configure our [decision service](#decision-service) at run-time and are the baseline for the enforcement by [PEPs](#policy-enforcement-point-pep) and [PXPs](#policy-execution-point-pxp). Policies are atomic. This means that a policy can be deployed (all contained mechanisms are active) or revoked (all contained mechanisms are inactive). Our policy format is XML and based on our [policy language](../language).

### Policy Editor
A policy editor is the user interface for the specification of a [policy](#policy). MYDATA Control Technologies provides an XML-based policy editor for developers. It supports specification by auto-completion, hints and a beginner mode for novice developers. More information can be found [here](../ui/#policies-policy-editor).

### Policy Enforcement Point (PEP)
The PEP is a [component](#component) which [monitors](#monitoring) or intercepts [events](#event) (e.g., a access or usage request to a resource), makes decision requests to the [decision service](#decision-service) for obtaining [decisions](#decision) and enforces the received decision. In terms of MYDATA Control Technologies, a PEP can allow, inhibit or modify events. PEPs can be dynamically registered and used using our SDK.

### Policy Execution Point (PXP) 
The PXP is a [component](#component) that can execute actions based on policy evaluations.. For example, a PXP can be used to send e-mail notifications, write log entries or delete data. In the XACML reference architecture, the PXP is part of the PEP. However, as the type of enforcement and the abstraction layer might significantly differ, we decided to make this explicit. PXPs can be dynamically registered and used using our SDK.

### Policy Information Point (PIP) 
The PIP is a [component](#component) that acts as a source of attribute values. This means that the purpose of a PIP is to provide any kind of information that is needed for the policy evaluation - but is not already contained in the [event](#event) itself. For example, PIPs can be used to connect MYDATA Control Technologies to a directory service in order to check user roles, or to resolve information about the current weather. PIPs can be dynamically registered and used using our SDK.


## Q


  
### Quota
The usage of our [decision service](#decision-service) is limited depending on the [plan](#plan) you have purchased. In particular, the number of request (decisions) per month is limited (quota). If you exceed your quota (i.e., you consumed more requests than you purchased), further request will be blocked (free plan) or charged (payed plans).


## R
  
### Reactive (Rx) PEP
A [PEP](#policy-enforcement-point-pep) implementing the [reactive programming](https://en.wikipedia.org/wiki/Reactive_programming) paradigm - i.e., working asynchronously.

### REST
Representational State Transfer ([cf. Wikipedia](https://de.wikipedia.org/wiki/Representational_State_Transfer))

### Roles
Our [management service](#management-service) supports three kinds of [user](#user) [roles](#roles) that regulate their permissions.

* Solution Developer: A Solution Developer only has access to the [solutions](#solution) he has been assigned to by an administrator. He has access to the dashboard, components and policies of assigned solutions.

* Affiliation Administrator: An Affiliation Adminstrator has access to all [solutions](#solution) of his [affiliation](#affiliation). He thus has access to the dashboard, components and policies of all solutions of his affiliation. Additionally, he can create and assign users for his affiliation. 
This role fully subsumes the Solution Developer.

* MYDATA Control Technologies Administrator: A MYDATA Control Technologies Administrator has all read and write access to all [affiliations](#affiliation), [solutions](#solution) and users. 
If hosted on premise, the MYDATA Control Technologies Administrator is the central administrator of the overall MYDATA Control Technologies system. 
This role fully subsumes the Affiliation Administrator and Solution Developer.

### Software Development Kit (SDK)
Our SDK is a set of tools and libraries that allow you to integrate and use MYDATA Control Technologies in your software. Primarily, it includes two Java Libraries - one for plain Java, one for [Spring](https://spring.io/). 
The SDK allows you to implement [PEPs](#policy-enforcement-point-pep) (including the modifiers), [PIPs](#policy-information-point-pip), and [PXPs](#policy-execution-point-pxp) and takes care about the [component](#component) registration with our service.


## S


  
### Solution
A solution is an application or service of an [affiliation](#affiliation) that is protected by MYDATA Control Technologies. You register components and specify policies exclusively for one solution. Component and policy identifiers contain a solution identifier.


## T


  
### Temporal
Cardinal aspects inside the [conditions](#event-condition-action) of [mechanisms](#mechanism) (as part of a [policy](#policy)) allow the enforcement of [usage rights](#usage-control) based on past events.  It enables you to specify policies like "if event A happened within the last 5 days" or "event B happened every day for the last 5 years").

### Timer
Policy evaluations may be triggered by two reasons. The first one is the decision request by a PEP after an event interception. The second is the triggering based on a timer. In some use cases, it is necessary to evaluate a policy periodically and independently of an event. In order to do this, you can specify timers that "simulate" an event to a given time based on the cron syntax. These events are treated like "normal" events by PEPs and thus do not break the [ECA](#event-condition-action) scheme. Obviously, only execute actions can be enforced in such a policy, but no authorization decision.



## U


  
### Usage Control
In addition to [Access Control](#access-control), usage control regulates what must (not) happen to data after access has been granted.

### User
A human person using our [management service](#management-service) (has one of the defined [roles](#roles)).
   

