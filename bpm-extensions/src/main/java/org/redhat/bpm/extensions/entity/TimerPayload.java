package org.redhat.bpm.extensions.entity;

public class TimerPayload {

    private String name;
    private Long delay;
    private Long period;
    private Integer repeatLimit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    public Integer getRepeatLimit() {
        return repeatLimit;
    }

    public void setRepeatLimit(Integer repeatLimit) {
        this.repeatLimit = repeatLimit;
    }
}
