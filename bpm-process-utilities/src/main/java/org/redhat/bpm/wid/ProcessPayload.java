package org.redhat.bpm.wid;

import java.io.Serializable;

public class ProcessPayload implements Serializable {

    private String destinationName;
    private String containerId;
    private String eventPublished;
    private String errorDetail;
    private Long processId;
    private String restExceptionEndpoint;
    private String restExceptionResponse;
    private Integer restExceptionStatus;

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

    public String getRestExceptionEndpoint() {
        return restExceptionEndpoint;
    }

    public void setRestExceptionEndpoint(String restExceptionEndpoint) {
        this.restExceptionEndpoint = restExceptionEndpoint;
    }

    public String getRestExceptionResponse() {
        return restExceptionResponse;
    }

    public void setRestExceptionResponse(String restExceptionResponse) {
        this.restExceptionResponse = restExceptionResponse;
    }

    public Integer getRestExceptionStatus() {
        return restExceptionStatus;
    }

    public void setRestExceptionStatus(Integer restExceptionStatus) {
        this.restExceptionStatus = restExceptionStatus;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String toString() {
        return "Container ID:" + containerId + " - Process ID:" + processId + " - Event published:" + eventPublished;
    }
}
