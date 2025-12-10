# ![](./assets/images/toolbox.svg) Policy Enforcement 

MYDATA Control Technologies intercepts events or data flows and enforces a security decision based on policies.
This process is highly customizable by different kinds of plugins to provide full flexibility for all use cases.

**Event Monitoring, Filtering and Masking:**
MYDATA Control Technologies monitors, filters or masks data usages and requests based on the active rule set. 
This is done by so-called "Policy Enforcement Points", which can modify (Json serialized) data on the fly.
Policy Enforcement Points are highly flexible and customizable.
Example modifications could be:

* Removing all customer addresses 
* Anonymizing person names
* Coarsing GPS locations
* Adding copyright notice to a text

**Execution of Actions:**
MYDATA Control Technologies executes (compensatory or additional) actions based on the active rule set. 
This execution is done by so-called "Policy Execution Points", which you can register in our system.
Policy Execution Points might for example

* send E-Mail notifications
* create log entries
* trigger a business process

**Connecting to External Information Sources:**
MYDATA Control Technologies integrates all kinds of information sources, e.g., location data, directory information.
This execution is done by so-called "Policy Information Points", which you can register in our system.
Policy Information Points can, for example be used to

* check a user role via LDAP
* check the user’s context (e.g., "traveling" or "in the office")
* check if the weather is nice in Berlin

To learn more about our SDK, click [here](howto_library.html). 

To learn more about Policy Enforcement Points, click [here](howto_pep.html).

To learn more about Policy Information Points, click [here](howto_pip.html). 

To learn more about Policy Execution Points, click [here](howto_pxp.html).