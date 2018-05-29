package org.redhat.bpm.model;

import org.kie.server.api.model.instance.TaskInstance;

import java.util.Map;


public class TaskDetailWithVariable {

    private TaskInstance taskDetail;
    private Map<String, Object> processInstanceVariables;

    public TaskDetailWithVariable() {
    }

    public TaskDetailWithVariable(TaskInstance taskDetail, Map<String, Object> processInstanceVariables) {
        this.taskDetail = taskDetail;
        this.processInstanceVariables = processInstanceVariables;
    }

    public TaskInstance getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(TaskInstance taskDetail) {
        this.taskDetail = taskDetail;
    }

    public Map<String, Object> getProcessInstanceVariables() {
        return processInstanceVariables;
    }

    public void setProcessInstanceVariables(Map<String, Object> processInstanceVariables) {
        this.processInstanceVariables = processInstanceVariables;
    }

}
