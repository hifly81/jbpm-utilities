# BPMS extensions


A sample project showing how to create new REST APIs for Kie Server.


## Compile

```bash
mvn clean package
```

## Deployment

copy <br><i>target/bpms-extensions-0.0.1-SNAPSHOT.jar</i> in<br> <i>{BPM_HOME}/standalone/deployments/kie-server.war/WEB-INF/lib</i>

Restart BPM server.


## Usage

Two new REST APIs will be available after restarting BPM server:<br>

  <i><b>Delete Timer</b></i>: this will delete all active timers for input process instance<br>
  Method: DELETE<br>
  Input: <br>
  --> container-id: GAV, kie container where process lives<br>
  --> process-instance-id: id of running process<br>
  Url:<br>
  http://bpm-server-host/kie-server/services/rest/ext/server/containers/container-id/processes/instances/process-instance-id/timer

 <i><b>Trigger Timer</b></i>: this will trigger all active timers for input process instance<br>
 Method: POST<br>
 Input: <br>
 --> container-id: GAV, kie container where process lives<br>
 --> process-instance-id: id of running process<br>
 Url:<br>
http://bpm-server-host/kie-server/services/rest/ext/server/containers/container-id/processes/instances/process-instance-id/timer/trigger
