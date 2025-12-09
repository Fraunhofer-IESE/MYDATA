# Release Notes

Diese Release Notes fassen die Entwicklung von **IND²UCE / MYDATA Control Technologies** auf **Major-Version-Ebene** zusammen.  

Für Migration Guides klicken Sie [hier](migrationGuide).


## 5.0
**Leitidee:** MYDATA wird Open Source.

## 4.x — MYDATA Control Technologies v4 (seit 2019)

**Leitidee:** Informationelle Selbstbestimmung in jeder Java-Anwendung (Cloud, lokal, hybrid) mit deutlich vereinfachter Integration. MYDATA 4 bringt die Vorteile des Cloud-Dienstes auch in lokale/offline Szenarien und überarbeitet SDK & Services grundlegend. 

- Mehrere Betriebsmodi: Deployment über API, Filesystem oder Cloud-Management; Evaluation lokal oder via Cloud-Decision-Service; optionales lokales Caching. 
- Vollständig überarbeitetes & vereinfachtes SDK / Interfaces** für leichtere Integration. 
- Komplette und konsistente REST API** inkl. Swagger-Zugriff. 
- Technische Modernisierung Dependency-Updates, Refactoring, Data-Model-Cleanup. 
- 4.1–4.3: neues Management-UI, Dockerization aller Services, Quota-/Lizenzrestriktionen entfernt, Security/Hardening & Updates bis Java-17-Kompatibilität. 



## 3.x — MYDATA v3 / Cloud-Ära (2017–2019)

**Leitidee:** Produktreife und Skalierung durch einen mandantenfähigen Cloud-Dienst plus SDK. MYDATA v3 wurde als hochskalierbarer AWS-Service konzipiert, um hunderttausende Policy-Entscheidungen pro Sekundenbereich zu tragen. 

- Zentraler, mandantenfähiger Dienst zur Erstellung, Verwaltung und Auswertung von Policies. 
- Umfassende Protokollierung/Auditability von Ereignissen und Entscheidungen (Verbesserung der Nachvollziehbarkeit und Policy-Optimierung). 
- Ereignis-Historie & zustandsbasierte Policies (seit 3.2): vergangene Zugriffe können in Entscheidungen einfließen. 
- Policy-Sprache komplett überarbeitet lesbarer und leichter zu nutzen. 
- Namenswechsel IND²UCE wird zu MYDATA Control Technologies (3.2.69) um die Trennung zwischen Forschung (IND²UCE) und Entwicklung (MYDATA) klarer zu machen. 



## 2.x — IND²UCE v2 / Verteilte Datennutzungskontrolle (2014–2016)

**Leitidee:** Von Einzel-Prototypen zu systemübergreifender Datennutzungskontrolle in dynamischen Verbünden.  
Komponenten konnten nun dynamisch in Ökosysteme eintreten / austreten; Fokus auf Austausch & Durchsetzung von Nutzungsbedingungen über Systemgrenzen hinweg. 

- Inter-organisational Usage Control: Policies werden zwischen Partnern/Systemen verhandelt und übertragen. 
- Dynamische Zusammenarbeit verteilter IND²UCE-Komponenten über verschiedene Systemebenen hinweg. 
- Protokolle & Formate für Aushandlung/Übertragung von Nutzungsberechtigungen sowie modellbasierte Policy-Generierung für unterschiedliche Zielsysteme. 
- Semantische Erweiterung der Policy-Sprache, um abstrakte Bedingungen unterschiedlich durchsetzbar zu machen. 


## 1.x — IND²UCE v1 / Grundlagenphase (2009–2013)

**Leitidee:** Fundament für Datennutzungskontrolle legen („Daten teilen, Kontrolle behalten“). Entwicklung der formalen Policy-Beschreibung und eines Durchsetzungs-Frameworks. 

- Erarbeitung von Anwendungsfällen für Datennutzungskontrolle. 
- Formale, maschinenlesbare Policy-Sprache basierend auf XACML und Obligation Specification Language (OSL). 
- Aufbau des IND²UCE-Rahmenwerks mit Komponenten zur Verwaltung, Auswertung und Durchsetzung von Policies; Referenzimplementierungen & Prototypen. 
- Breite **Demonstratoren/Prototypen in unterschiedlichen Zielsystemen zur praxisnahen Evaluierung. 
