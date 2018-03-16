package org.redhat.bpm.workbeat.wid;

import java.io.Serializable;

public class ProcessPayload implements Serializable {

    private String destinationName;
    private String containerId;
    private String eventPublished;
    private Long processId;

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getEventPublished() {
        return eventPublished;
    }

    public void setEventPublished(String eventPublished) {
        this.eventPublished = eventPublished;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public String toString() {
        return "Container ID:" + containerId + " - Process ID:" + processId + " - Event published:" + eventPublished;
    }
}
