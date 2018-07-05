package org.redhat.bpm.query;

import org.kie.server.api.model.definition.QueryDefinition;

import java.util.ArrayList;

public class AdvancedQueryFactory {

    //FIXME define datsource bpm
    private static final String SOURCE = "java:jboss/datasources/PostgresqlDS";

    public static final String FIND_PROCESS_INSTANCES_WITH_VARIABLES = "findProcessInstancesWithVariables";
    public static final String POT_OWNED_TASKS_BY_VARIABLES_AND_PARAMS = "potOwnedTasksByVariablesAndTaskParams";
    public static final String FIND_TASKS_WITH_PARAMETERS = "findTasksWithParameters";
    public static final String TASKS_BY_VARIABLES_AND_PARAMS = "tasksByVariablesAndParams";
    public static final String TASKS_BY_NAMES_VARIABLES_AND_PARAMS = "tasksByNamesAndVariablesAndParams";
    public static final String TASKS_BY_GROUPS_AND_VARIABLES_AND_PARAMS = "tasksByGroupsAndVariablesAndParams";
    public static final String TASK_BY_GROUPS_AND_VARIABLES_AND_PARAMS_FILTER = "tasksByGroupsAndVariablesAndParamsFilter";
    public static final String TASK_BY_GROUPS_AND_VARIABLES_AND_PARAMS_NOT_ACTUALOWNER_FILTER = "tasksByGroupsAndVariablesAndParamsNotActualOwnerFilter";
    public static final String PROCESS = "PROCESS";
    public static final String CUSTOM = "CUSTOM";
    public static final String TASK = "TASK";

    public ArrayList<QueryDefinition> getDefinitions() {

        ArrayList<QueryDefinition> definitions = new ArrayList<>();

        definitions.add(findProcessInstancesWithVariables());
        definitions.add(potOwnedTasksByVariablesAndParams());
        definitions.add(findTasksWithParameters());
        definitions.add(tasksByVariablesAndParams());
        definitions.add(tasksByNamesAndVariablesAndParams());
        definitions.add(tasksByGroupsAndVariablesAndParams());

        return definitions;

    }

    public QueryDefinition findProcessInstancesWithVariables() {

        QueryDefinition query = new QueryDefinition();
        query.setName(FIND_PROCESS_INSTANCES_WITH_VARIABLES);
        query.setSource(SOURCE);

        query.setExpression(" select pil.*, v.variableId, v.value " +
                " from ProcessInstanceLog pil " +
                " INNER JOIN VariableInstanceLog v " +
                " ON (v.processInstanceId = pil.processInstanceId) " +
                " INNER JOIN ( " +
                " 	select vil.processInstanceId ,vil.variableId, MAX(vil.ID) maxvilid " +
                " 	FROM VariableInstanceLog vil " +
                " 	GROUP BY vil.processInstanceId, vil.variableId " +
                " 	ORDER BY vil.processInstanceId " +
                " ) x " +
                " ON (v.variableId = x.variableId  AND v.id = x.maxvilid) ");

        query.setTarget(PROCESS);

        return query;

    }

    public QueryDefinition potOwnedTasksByVariablesAndParams() {

        QueryDefinition query = new QueryDefinition();
        query.setName(POT_OWNED_TASKS_BY_VARIABLES_AND_PARAMS);
        query.setSource(SOURCE);
        query.setExpression(" select task.taskid, task.actualowner, task.status, param.name paramname, param.value paramvalue, variable.variableid variablename, variable.value variablevalue, pot.entity_id potowner, ex.entity_id exclowner " +
                " from audittaskimpl task " +
                " inner join ( " +
                "       select param.taskid, param.name, param.value  " +
                "       from taskvariableimpl param  " +
                "       where param.type = 0  " +
                " ) param " +
                " on (param.taskid = task.taskid) " +
                " inner join peopleassignments_potowners pot " +
                " on (pot.task_id = task.taskid) " +
                " left join peopleassignments_exclowners ex " +
                " on (ex.task_id = task.taskid) " +
                " inner join variableinstancelog variable " +
                " on (variable.processinstanceid = task.processinstanceid) " +
                " inner join ( " +
                "       select vil.processinstanceid ,vil.variableid, max(vil.id) maxvilid " +
                "       from variableinstancelog vil " +
                "       group by vil.processinstanceid, vil.variableid " +
                "       order by vil.processinstanceid " +
                " ) x " +
                " on (variable.variableid = x.variableid and variable.id = x.maxvilid) ");
        query.setTarget(CUSTOM);

        return query;

    }

    public QueryDefinition findTasksWithParameters() {

        QueryDefinition query = new QueryDefinition();
        query.setName(FIND_TASKS_WITH_PARAMETERS);
        query.setSource(SOURCE);
        query.setExpression("select ti.*, tv.name tvname, tv.value tvvalue "+
                "from AuditTaskImpl ti " +
                "inner join (select tv.taskId, tv.name, tv.value from TaskVariableImpl tv where tv.type = 0) tv "+
                "on (tv.taskId = ti.taskId)");
        query.setTarget(TASK);

        return query;

    }

    public QueryDefinition tasksByVariablesAndParams() {

        QueryDefinition query = new QueryDefinition();
        query.setName(TASKS_BY_VARIABLES_AND_PARAMS);
        query.setSource(SOURCE);
        query.setExpression(" select task.taskid, task.actualowner, task.status, " +
                " param.name paramname, param.value paramvalue, " +
                " variable.variableid variablename, variable.value variablevalue " +
                " from audittaskimpl task " +
                " inner join ( " +
                "       select param.taskid, param.name, param.value  " +
                "       from taskvariableimpl param  " +
                "       where param.type = 0  " +
                " ) param " +
                " on (param.taskid = task.taskid) " +
                " inner join variableinstancelog variable " +
                " on (variable.processinstanceid = task.processinstanceid) " +
                " inner join ( " +
                "       select vil.processinstanceid ,vil.variableid, max(vil.id) maxvilid " +
                "       from variableinstancelog vil " +
                "       group by vil.processinstanceid, vil.variableid " +
                "       order by vil.processinstanceid " +
                " ) x " +
                " on (variable.variableid = x.variableid and variable.id = x.maxvilid) ");
        query.setTarget(CUSTOM);

        return query;

    }

    public QueryDefinition tasksByNamesAndVariablesAndParams() {

        QueryDefinition query = new QueryDefinition();
        query.setName(TASKS_BY_NAMES_VARIABLES_AND_PARAMS);
        query.setSource(SOURCE);
        query.setExpression(" select task.taskid, task.actualowner, task.name tvname, task.status, " +
                " param.name paramname, param.value paramvalue, " +
                " variable.variableid variablename, variable.value variablevalue " +
                " from audittaskimpl task " +
                " inner join ( " +
                "       select param.taskid, param.name, param.value  " +
                "       from taskvariableimpl param  " +
                "       where param.type = 0  " +
                " ) param " +
                " on (param.taskid = task.taskid) " +
                " inner join variableinstancelog variable " +
                " on (variable.processinstanceid = task.processinstanceid) " +
                " inner join ( " +
                "       select vil.processinstanceid ,vil.variableid, max(vil.id) maxvilid " +
                "       from variableinstancelog vil " +
                "       group by vil.processinstanceid, vil.variableid " +
                "       order by vil.processinstanceid " +
                " ) x " +
                " on (variable.variableid = x.variableid and variable.id = x.maxvilid) ");
        query.setTarget(CUSTOM);

        return query;

    }

    public QueryDefinition tasksByGroupsAndVariablesAndParams() {

        QueryDefinition query = new QueryDefinition();
        query.setName(TASKS_BY_GROUPS_AND_VARIABLES_AND_PARAMS);
        query.setSource(SOURCE);
        query.setExpression(" select task.taskid, task.actualowner, task.status, param.name paramname, param.value paramvalue, variable.variableid variablename, variable.value variablevalue, pot.entity_id potowner, ex.entity_id exclowner " +
                " from audittaskimpl task " +
                " inner join ( " +
                "       select param.taskid, param.name, param.value  " +
                "       from taskvariableimpl param  " +
                "       where param.type = 0  " +
                " ) param " +
                " on (param.taskid = task.taskid) " +
                " inner join peopleassignments_potowners pot " +
                " on (pot.task_id = task.taskid) " +
                " left join peopleassignments_exclowners ex " +
                " on (ex.task_id = task.taskid) " +
                " inner join variableinstancelog variable " +
                " on (variable.processinstanceid = task.processinstanceid) " +
                " inner join ( " +
                "       select /*+ NO_MERGE */ vil.processinstanceid ,vil.variableid, max(vil.id) maxvilid " +
                "       from variableinstancelog vil " +
                "       group by vil.processinstanceid, vil.variableid " +
                "       order by vil.processinstanceid " +
                " ) x " +
                " on (variable.variableid = x.variableid and variable.id = x.maxvilid) ");
        query.setTarget(CUSTOM);

        return query;

    }
}
