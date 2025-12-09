# Release Notes

These release notes summarize the development of **IND²UCE / MYDATA Control Technologies** at the **major version level**.

For migration guides, click [here](migrationGuides).


## 5.0
**Guiding idea:** MYDATA becomes open source.

## 4.x — MYDATA Control Technologies v4 (since 2019)

**Guiding idea:** Informational self‑determination in any Java application (cloud, local, hybrid) with significantly simplified integration. MYDATA 4 brings the advantages of the cloud service to local/offline scenarios as well and fundamentally revises the SDK & services.

- Multiple operation modes: deployment via API, filesystem, or cloud management; evaluation locally or via cloud decision service; optional local caching.
- Completely revised & simplified SDK / interfaces for easier integration.
- Complete and consistent REST API incl. Swagger access.
- Technical modernization: dependency updates, refactoring, data‑model cleanup.
- 4.1–4.3: new management UI, dockerization of all services, quota/license restrictions removed, security/hardening & updates up to Java‑17 compatibility.


## 3.x — MYDATA v3 / Cloud era (2017–2019)

**Guiding idea:** Product maturity and scaling through a multi‑tenant cloud service plus SDK. MYDATA v3 was designed as a highly scalable AWS service to handle hundreds of thousands of policy decisions per second.

- Central, multi‑tenant service for creating, managing, and evaluating policies.
- Comprehensive logging/auditability of events and decisions (improves traceability and policy optimization).
- Event history & state‑based policies (since 3.2): past accesses can influence decisions.
- Policy language completely revised to be more readable and easier to use.
- Rename: IND²UCE becomes MYDATA Control Technologies (3.2.69) to clarify the separation between research (IND²UCE) and development (MYDATA).


## 2.x — IND²UCE v2 / Distributed data usage control (2014–2016)

**Guiding idea:** From individual prototypes to cross‑system data usage control in dynamic federations.  
Components could now dynamically join/leave ecosystems; focus on exchanging & enforcing usage conditions across system boundaries.

- Inter‑organizational usage control: policies are negotiated and transferred between partners/systems.
- Dynamic collaboration of distributed IND²UCE components across different system layers.
- Protocols & formats for negotiating/transferring usage rights, plus model‑based policy generation for different target systems.
- Semantic extension of the policy language to make abstract conditions enforceable in different ways.


## 1.x — IND²UCE v1 / Foundation phase (2009–2013)

**Guiding idea:** Laying the foundation for data usage control (“share data, keep control”). Development of formal policy descriptions and an enforcement framework.

- Development of use cases for data usage control.
- Formal, machine‑readable policy language based on XACML and Obligation Specification Language (OSL).
- Building the IND²UCE framework with components for managing, evaluating, and enforcing policies; reference implementations & prototypes.
- Broad set of demonstrators/prototypes in different target systems for practical evaluation.
