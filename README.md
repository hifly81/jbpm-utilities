# Red Hat BPM/BRMS utilities

Utilities for Red Hat BPM/BRMS products.

Two branches are available:
 - bpms6.4.x (code for BPM 6.4.x)
 - bpms7.0.x (code for BPM 7.0.x)
master branch is aligned to branch bpms6.4.x

Some components available in this project:
 - rest client for BPM APIs
 - jms client for BPM APIs
 - advanced queries
 - custom work item handlers
 - custom event listeners
 - custom task event listeners
 - eap log handler for bpm events
 - bpm extensions to the REST APIs (only for BPM 6.4.x)
 - bpm kjar inclusion
 - bpm and rules
 


## Compile and execute
In order to compile the projects you need apache maven v3.x

You can compile and create the artifact archives executing commands 
starting from the root folder.

Compile:

```bash
mvn clean compile
```

Create artifacts and execute tests (target folder):
```bash
mvn clean install
```
