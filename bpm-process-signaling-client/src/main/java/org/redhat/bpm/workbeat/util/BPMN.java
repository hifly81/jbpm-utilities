package org.redhat.bpm.workbeat.util;

public enum BPMN {

    PROCESS_SIGNALING(1, "org.redhat.bpm.workbeat.process-signaling"),
    PROCESS_SIGNALING_BIS(2, "org.redhat.bpm.workbeat.process-signaling-bis"),
    PROCESS_SIGNALING_MULTIPLE_EVENTS(3, "org.redhat.bpm.workbeat.process-signaling-multiple-events"),
    PROCESS_SIGNALING_TOPIC_PUB(4, "org.redhat.bpm.workbeat.process-signaling-topic-pub"),
    PROCESS_SIGNALING_MULTIPLE_EVENTS_SUB(5, "org.redhat.bpm.workbeat.process-signaling-multiple-events-sub"),
    PROCESS_TEST_TASK(6, "test-task.process-sample"),
    PROCESS_GENERIC(7, "");

    private int index;
    private String value;

    BPMN(int index, String v) {
        this.index = index;
        this.value = v;
    }

    public int index() {
        return index;
    }

    public String value() {
        return value;
    }

    public static BPMN fromIndex(int index) {

        switch (index) {
            case 1:
                return PROCESS_SIGNALING;
            case 2:
                return PROCESS_SIGNALING_BIS;
            case 3:
                return PROCESS_SIGNALING_MULTIPLE_EVENTS;
            case 4:
                return PROCESS_SIGNALING_TOPIC_PUB;
            case 5:
                return PROCESS_SIGNALING_MULTIPLE_EVENTS_SUB;
            case 6:
                return PROCESS_TEST_TASK;
            case 7:
                return PROCESS_GENERIC;
            default:
                return null;
        }

    }


}
