package org.redhat.bpm.model;

import java.util.Map;


public class TaskDetailWithVariableCustom {

    private TaskDetail taskDetail;
    private Map<String, Object> processInstanceVariables;

    public TaskDetailWithVariableCustom() {
    }

    public TaskDetailWithVariableCustom(TaskDetail taskDetail, Map<String, Object> processInstanceVariables) {
        this.taskDetail = taskDetail;
        this.processInstanceVariables = processInstanceVariables;
    }

    public TaskDetail getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(TaskDetail taskDetail) {
        this.taskDetail = taskDetail;
    }

    public Map<String, Object> getProcessInstanceVariables() {
        return processInstanceVariables;
    }

    public void setProcessInstanceVariables(Map<String, Object> processInstanceVariables) {
        this.processInstanceVariables = processInstanceVariables;
    }

}
