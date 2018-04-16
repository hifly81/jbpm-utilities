# jBPM kjar inclusion

This sample project shows how a master project can include another kjar and its assets.<br>
A BPMN process inside the master project uses a subprocess defined in the included kjar.


## Compile
for sample-proc

```bash
mvn clean install && mvn deploy
```

for master-proc

```bash
mvn clean install && mvn deploy
```
kjars will be uploaded to the maven repository in the business central.

## Deployment

Inside the business central define a new kie container for master-proc kjar 
