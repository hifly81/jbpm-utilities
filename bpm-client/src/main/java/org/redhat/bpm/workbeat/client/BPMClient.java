package org.redhat.bpm.workbeat.client;

import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.redhat.bpm.workbeat.service.GatewaySettings;
import org.redhat.bpm.workbeat.service.KieService;
import org.redhat.bpm.workbeat.service.KieServiceFactory;
import org.redhat.bpm.workbeat.util.BPMN;
import org.redhat.bpm.workbeat.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BPMClient {

    private static final Logger LOG = LoggerFactory.getLogger(BPMClient.class);

    public static void main(String[] args) {

        System.out.println("JBPM external client demo\n  --> Choose a (Rest) or (JMS) client:");

        Scanner scanner = new Scanner(System.in);

        String clientMode = scanner.nextLine();

        System.out.println("Insert a Container ID:");

        String containerId = scanner.nextLine();

        KieService kieService = KieServiceFactory.create(clientMode, GatewaySettings.create(Constants.KIESERVER_USERNAME, Constants.KIESERVER_PASSWORD));
        kieService.startConverasation();
        //register advanced queries
        kieService.registerAdvancedQueries();

        while (true) {
            System.out.println("\nMenu Options\n");
            System.out.println("(1) - start processes");
            System.out.println("(2) - signal processes with an event");
            System.out.println("(3) - task detail");
            System.out.println("(4) - query - process variables");
            System.out.println("(5) - query - owned task by process variables and task variables");
            System.out.println("(6) - query - owned task by process variables and task variables - AND clause");
            System.out.println("(7) - quit");

            System.out.print("Please enter your selection:\t");
            int selection = scanner.nextInt();
            scanner.nextLine();

            if (selection == 1) {

                System.out.print("Enter a list of processes instance to start, separeted with a comma" +
                        "(1) for process-signaling,\n " +
                        "(2) for process-signaling-bis,\n " +
                        "(3) for process-signaling-multiple-events,\n " +
                        "(4) for process-signaling-topic-pub,\n " +
                        "(5) for process-signaling-multiple-events-sub,\n " +
                        "(6) for process-test-task\n" +
                        "(7) insert a process-id definition\n");
                String processesNames = scanner.nextLine();
                StringTokenizer st = new StringTokenizer(processesNames, ",");
                while (st.hasMoreElements()) {
                    Integer index = Integer.valueOf((String)st.nextElement());
                    BPMN bpmn = BPMN.fromIndex(index);
                    Long pid = -1l;
                    if(bpmn == BPMN.PROCESS_SIGNALING_TOPIC_PUB) {
                        HashMap<String, Object> parameters = new HashMap<>();
                        parameters.put("destinationName", "topic/bpmnProcessesTopic");
                        parameters.put("containerId", Constants.CONTAINER_ID);
                        parameters.put("eventPublished", "task1_completed");
                        parameters.put("jmsConnection", "local");
                        pid = kieService.startProcess(containerId, bpmn, parameters);


                    }
                    else if(bpmn == BPMN.PROCESS_GENERIC) {
                        System.out.println("Enter a process definition id:");
                        String processDefinitionId = scanner.nextLine();
                        pid = kieService.startProcess(containerId, processDefinitionId);
                    }
                    else {
                        pid = kieService.startProcess(containerId, bpmn.value());
                    }

                    LOG.info("Process instance {} started with {}", bpmn.value(), pid);
                }


            }
            else if (selection == 2) {
                System.out.println("Enter an event name: (allowed: task1_completed, event1, event2, event_sub)");
                String signalName = scanner.nextLine();
                kieService.signal(containerId, signalName,"OK");
            }
            else if (selection == 3) {
                System.out.println("Enter a taskId:");
                String taskId = scanner.nextLine();
                TaskInstance taskInstance = kieService.taskDetail(containerId, Long.valueOf(taskId));
                LOG.info("TaskInstance detail: {}", taskInstance);
            }
            else if (selection == 4) {
                System.out.println("Enter a process definition id:");
                String processDefinitionId = scanner.nextLine();
                List<ProcessInstance> processInstanceList = kieService.findProcessInstancesWithVariables(processDefinitionId, 0, 100);
                LOG.info("Process Instances list: {}", processInstanceList);
            }
            else if (selection == 5) {


                List<String> groups = new ArrayList<>();
                groups.add("test");
                Map<String, List<String>> paramsMap = new HashMap<>();
                List<String> paramsValue = new ArrayList<> ();
                paramsValue.add("test");
                paramsMap.put("company", paramsValue);
                List<String> paramsValue2 = new ArrayList<> ();
                paramsValue2.add("livello1");
                paramsMap.put("level", paramsValue2);
                Map<String, List<String>> variablesMap = new HashMap<>();
                List<String> variablesValue1 = new ArrayList<String>();
                variablesValue1.add("test");
                variablesMap.put("company", variablesValue1);


                List<Long> tasks = kieService.potOwnedTasksByVariablesAndParams("bpmsAdmin", groups, paramsMap, variablesMap);
                LOG.info("Task list: {}", tasks);
            }
            else if (selection == 6) {


                List<String> groups = new ArrayList<>();
                groups.add("test");
                Map<String, List<String>> paramsMap = new HashMap<>();
                List<String> paramsValue = new ArrayList<> ();
                paramsValue.add("test");
                paramsMap.put("company", paramsValue);
                List<String> paramsValue2 = new ArrayList<> ();
                paramsValue2.add("livello1");
                paramsMap.put("level", paramsValue2);
                List<String> paramsValue3 = new ArrayList<> ();
                paramsValue3.add("");
                paramsMap.put("level-excluded", paramsValue3);
                Map<String, List<String>> variablesMap = new HashMap<>();
                List<String> variablesValue1 = new ArrayList<String>();
                variablesValue1.add("test");
                variablesMap.put("company", variablesValue1);


                List<Long> tasks = kieService.potOwnedTasksByVariablesAndParamsAndClause("bpmsAdmin", groups, paramsMap, variablesMap);
                LOG.info("Task list: {}", tasks);
            }
            else if (selection == 7) {
                kieService.endConversation();
                System.exit(1);
            }
        }

    }
}
