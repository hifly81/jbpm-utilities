package org.redhat.bpm.service;

import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.*;
import org.redhat.bpm.query.AdvancedQueryFactory;
import org.redhat.bpm.util.BPMN;

import java.util.*;
import java.util.stream.Collectors;

import static org.kie.server.client.QueryServicesClient.*;

public abstract class KieService {

    private static final String[] POT_OWNED_STATUS = {"Created", "Ready", "Reserved", "InProgress", "Suspended"};
    public static final String[] OPEN_STATUS = { "Created", "Ready", "Reserved", "InProgress", "Suspended" };
    private static final int ARBITRARY_LONG_VALUE = 10000;

    protected KieServicesConfiguration config;

    protected KieServicesClient client;

    public void startConverasation() {
        client = KieServicesFactory.newKieServicesClient(config);
    }

    public void endConversation() {
        client.completeConversation();
    }

    public void registerAdvancedQueries() {
        AdvancedQueryFactory factory = new AdvancedQueryFactory();
        QueryServicesClient queryService = client.getServicesClient(QueryServicesClient.class);
        factory.getDefinitions().forEach(queryDefinition -> {
            queryService.replaceQuery(queryDefinition);
        });

    }

    public List<ProcessInstance> findProcessInstancesWithVariables(String processDefinitionId, Integer page, Integer pageSize) {
        QueryServicesClient queryService = client.getServicesClient(QueryServicesClient.class);

        QueryFilterSpec queryFilterSpec = new QueryFilterSpecBuilder()
                .equalsTo("processid", processDefinitionId)
                .get();

        List<ProcessInstance> query = queryService.query(AdvancedQueryFactory.FIND_PROCESS_INSTANCES_WITH_VARIABLES,
                QUERY_MAP_PI_WITH_VARS, queryFilterSpec, page, pageSize, ProcessInstance.class);

        return query;
    }

    public List<Long> potOwnedTasksByVariablesAndTaskParamsInOr(String user, List<String> groups, Map<String, List<String>> paramsMap, Map<String, List<String>> variablesMap) {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("user", user);
        parameters.put("groups", groups);

        parameters.put("status", Arrays.asList(POT_OWNED_STATUS));

        if (paramsMap != null) {
            parameters.put("paramsMap", paramsMap);
        }

        if (variablesMap != null) {
            parameters.put("variablesMap", variablesMap);
        }

        QueryServicesClient queryService = client.getServicesClient(QueryServicesClient.class);

        List<TaskInstance> taskWithDuplicates = queryService.query(AdvancedQueryFactory.POT_OWNED_TASKS_BY_VARIABLES_AND_PARAMS, QUERY_MAP_TASK, "potOwnedTasksByVariablesAndParamsFilter", parameters, 0, ARBITRARY_LONG_VALUE, TaskInstance.class);

        List<Long> ids = taskWithDuplicates.stream().map(taskInstance -> taskInstance.getId()).distinct().collect(Collectors.toList());

        return ids;

    }

    public List<Long> potOwnedTasksByVariablesAndTaskParamsInAnd(
            String user,
            List<String> groups,
            Map<String, List<String>> paramsMap,
            Map<String, List<String>> variablesMap) {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("user", user);
        parameters.put("groups", groups);
        parameters.put("status", Arrays.asList(OPEN_STATUS));

        //evaluate paramsMap composition
        if(paramsMap != null && !paramsMap.isEmpty() && paramsMap.size() > 5)
            throw new IllegalStateException("Max 5 paramsMap are allowed!");

        List<String> keys = new ArrayList<>(paramsMap.keySet());
        String keyToCheck = null;
        String keyToExclude = null;
        String keyToQuery = null;
        String keyToExcludeWithoutCheck = null;

        int excludedCount = 0;
        for(String key: keys) {
            if (key.matches(".*-excluded")) {
                keyToExclude = key;
                excludedCount++;
            }
        }

        if(paramsMap.size() == 0 || (paramsMap.size() == 1 && excludedCount == 1))
            throw new IllegalStateException("At least one param must be provided");
        if(excludedCount > 2)
            throw new IllegalStateException("A maximum of 2 *-excluded params are allowed");
        if(excludedCount == 0 && paramsMap.size() > 2)
            throw new IllegalStateException("Max 2 paramsMap are allowed if no *-excluded param is provided");


        //use AND query with 1 condition
        if(paramsMap.size() == 1) {
            if (paramsMap != null)
                parameters.put("paramsMap", paramsMap);

            if (variablesMap != null)
                parameters.put("variablesMap", variablesMap);

            QueryServicesClient queryServices = client.getServicesClient(QueryServicesClient.class);
            List<TaskInstance> taskWithDuplicates = queryServices.query(AdvancedQueryFactory.POT_OWNED_TASKS_BY_VARIABLES_AND_PARAMS, QUERY_MAP_TASK, "potOwnedTasksByVariablesAndParamsFilter", parameters, 0, ARBITRARY_LONG_VALUE, TaskInstance.class);
            List<Long> ids = taskWithDuplicates.stream().map(taskInstance -> taskInstance.getId()).distinct().collect(Collectors.toList());


            return ids;

        }

        //use OR query and apply additional filtering in java
        else {
            List<String> keysToCheck = new ArrayList<>(2);
            List<String> keysToQueries = new ArrayList<>(2);
            boolean multipleParamsToExclude = false;
            boolean multipleParamsToQuery = false;

            //there is no key to exclude
            if(keyToExclude == null) {
                keyToQuery = keys.get(0);

                if(keys.size() == 2)
                    keyToCheck = keys.get(1);
            }
            else {
	            /*
	              there is a key to exclude: 4 cases are allowed
	            */

                //1) -excluded and keyToQuery
                if(paramsMap.size() == 2) {
                    for (String key : keys) {
                        if (!key.equalsIgnoreCase(keyToExclude)) {
                            keyToQuery = key;
                            break;
                        }
                    }

                    keyToExcludeWithoutCheck = keyToExclude.replace("-excluded","");
                }

                //2) -excluded, keyToQuery and keyToCheck
                else if(paramsMap.size() == 3) {
                    for (String key : keys) {
                        if (key.matches(keyToExclude.replace("-excluded", ""))) {
                            keyToCheck = key;
                            break;
                        }
                    }

                    for (String key : keys) {
                        if (!key.equalsIgnoreCase(keyToCheck) && !key.equalsIgnoreCase(keyToExclude)) {
                            keyToQuery = key;
                            break;
                        }
                    }
                }
                //3) -excluded, 2 keyToQuery and keyToCheck
                else if(paramsMap.size() == 4) {
                    for (String key : keys) {
                        if (key.matches(keyToExclude.replace("-excluded", ""))) {
                            keyToCheck = key;
                        } else if(!key.contains("-excluded")){
                            keysToQueries.add(key);
                        }
                    }
                    keyToQuery = keysToQueries.get(0);
                    multipleParamsToQuery = true;

                }
                //4) 2 excluded, 1 keyToQuery and 2 keyToCheck
                else if(paramsMap.size() == 5) {
                    List<String> includedKeys = new ArrayList<>(3);
                    for(String key: keys) {
                        if (key.matches(".*-excluded"))
                            keysToCheck.add(key.replace("-excluded", ""));
                        else
                            includedKeys.add(key);
                    }
                    for(String key: includedKeys) {
                        if(!keysToCheck.contains(key))
                            keyToQuery = key;
                    }
                    multipleParamsToExclude = true;
                }

            }

            //set new paramsMap for query
            Map<String, List<String>> newParamsMap = new HashMap<>(1);
            newParamsMap.put(keyToQuery, paramsMap.get(keyToQuery));
            parameters.put("paramsMap", newParamsMap);

            if (variablesMap != null)
                parameters.put("variablesMap", variablesMap);

            QueryServicesClient queryServices = client.getServicesClient(QueryServicesClient.class);
            List<TaskInstance> taskWithDuplicates = queryServices.query(AdvancedQueryFactory.POT_OWNED_TASKS_BY_VARIABLES_AND_PARAMS, QUERY_MAP_TASK, "potOwnedTasksByVariablesAndParamsFilter", parameters, 0, ARBITRARY_LONG_VALUE, TaskInstance.class);
            List<Long> ids = taskWithDuplicates.stream().map(taskInstance -> taskInstance.getId()).distinct().collect(Collectors.toList());
            taskWithDuplicates = findTasksWithParameters(ids, true);

            //filter parameters for additional condition to check
            List<Long> idsToReturn = new ArrayList<>();
            for (Long id : ids) {
                for (TaskInstance t1 : taskWithDuplicates) {
                    if (t1.getId().equals(id)) {
                        if(!multipleParamsToExclude) {
                            if (keyToCheck != null) {
                                //extract second param
                                if (t1.getInputData().containsKey(keyToCheck)) {
                                    Object o1 = t1.getInputData().get(keyToCheck);
                                    if (paramsMap.get(keyToCheck).contains(o1))
                                        idsToReturn.add(id);
                                }
                                if (!t1.getInputData().containsKey(keyToCheck))
                                    idsToReturn.add(id);
                            } else {
                                if (keyToExcludeWithoutCheck != null) {
                                    if (!t1.getInputData().containsKey(keyToExcludeWithoutCheck))
                                        idsToReturn.add(id);
                                }
                            }
                            if(multipleParamsToQuery) {
                                String keyTmp = keysToQueries.get(1);
                                if (t1.getInputData().containsKey(keyTmp)) {
                                    Object o1 = t1.getInputData().get(keyTmp);
                                    if (paramsMap.get(keyTmp).contains(o1))
                                        idsToReturn.add(id);
                                }
                            }
                        } else {
                            for(String incKey: keysToCheck) {
                                if (t1.getInputData().containsKey(incKey)) {
                                    Object o1 = t1.getInputData().get(incKey);
                                    if (paramsMap.get(incKey).contains(o1))
                                        idsToReturn.add(id);
                                }
                                if (!t1.getInputData().containsKey(incKey))
                                    idsToReturn.add(id);
                            }
                        }
                    }
                }
            }

            return idsToReturn;
        }
    }


    public void signal(String containerId, String signalName, String event) {
        client.getServicesClient(ProcessServicesClient.class).signal(containerId, signalName, event);
    }

    public Long startProcess(String containerId, String processId) {
        HashMap<String, Object> parameters = new HashMap<>();
        return client.getServicesClient(ProcessServicesClient.class).startProcess(containerId, processId, parameters);
    }

    public Long startProcess(String containerId, BPMN bpmn, HashMap<String, Object> parameters) {
        return client.getServicesClient(ProcessServicesClient.class).startProcess(containerId, bpmn.value(), parameters);
    }

    public TaskInstance taskDetail(String containerId, Long taskId) {

        UserTaskServicesClient userTaskService = client.getServicesClient(UserTaskServicesClient.class);
        TaskInstance taskInstance = userTaskService.getTaskInstance(containerId, taskId, true, true, true);

        return taskInstance;

    }

    public List<TaskInstance> findTasksWithParameters(List<Long> taskIds, boolean asc) {
        QueryServicesClient queryService = client.getServicesClient(QueryServicesClient.class);
        QueryFilterSpec queryFilterSpec = new QueryFilterSpecBuilder()
                .in("taskid", taskIds)
                .oderBy("taskid", asc)
                .get();

        List<TaskInstance> taskInstances = queryService.query(AdvancedQueryFactory.FIND_TASKS_WITH_PARAMETERS,
                QUERY_MAP_TASK_WITH_VARS, queryFilterSpec, 0, ARBITRARY_LONG_VALUE, TaskInstance.class);

        return taskInstances;

    }
}