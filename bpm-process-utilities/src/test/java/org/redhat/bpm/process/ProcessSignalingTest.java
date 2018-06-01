package org.redhat.bpm.process;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationAwareProcessRuntime;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.redhat.bpm.logging.LogProcessEventListener;
import org.redhat.bpm.wid.JMSTopicPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class ProcessSignalingTest extends JbpmJUnitBaseTestCase {

    private final static Logger LOG = LoggerFactory.getLogger(ProcessSignalingTest.class);

    private static final String PROCESS_FOLDER = "org/redhat/bpm/process/";

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
    private TaskService taskService;
    private CorrelationKeyFactory factory;


    private AuditService auditService;

    public ProcessSignalingTest() {
        super(true, true);
        factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    }

    @Before
    public void before() {


        LogProcessEventListener listener = new LogProcessEventListener(false);
        addProcessEventListener(listener);

        runtimeManager = createRuntimeManager(
                PROCESS_FOLDER + "process-signaling.bpmn2",
                PROCESS_FOLDER + "process-signaling-bis.bpmn2",
                PROCESS_FOLDER + "process-signaling-multiple-events.bpmn2",
                PROCESS_FOLDER + "process-signaling-multiple-events-sub.bpmn2",
                PROCESS_FOLDER + "process-signaling-topic-pub.bpmn2");
        runtimeEngine = getRuntimeEngine();

        kieSession = runtimeEngine.getKieSession();
        kieSession.getWorkItemManager().registerWorkItemHandler("JMSTopicPublisher", new JMSTopicPublisher());

        taskService = runtimeEngine.getTaskService();
        auditService = runtimeEngine.getAuditService();

        listener.setRuntimeManager(runtimeManager);

    }

    @After
    public void after() {
        runtimeManager.disposeRuntimeEngine(runtimeEngine);
        runtimeManager.close();
    }


    @Test
    public void test_process_single_event() {
        HashMap<String, Object> parameters = new HashMap<>();

        ProcessInstance pi = ((CorrelationAwareProcessRuntime)kieSession).startProcess("org.redhat.bpm.process-signaling", getCorrelationKey("key1"), parameters);
        ProcessInstance pi2 = ((CorrelationAwareProcessRuntime)kieSession).startProcess("org.redhat.bpm.process-signaling-bis", getCorrelationKey("key2"), parameters);

        assertProcessInstanceActive(pi.getId());
        assertNodeTriggered(pi.getId(), "StartProcess", "Script Task 1");

        assertProcessInstanceActive(pi2.getId());
        assertNodeTriggered(pi2.getId(), "StartProcess", "Script Task 1");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.exit(-1);
        }

        kieSession.signalEvent("task1_completed", "OK");

        assertProcessInstanceCompleted(pi.getId());
        assertNodeTriggered(pi.getId(), "End Event");

        assertProcessInstanceCompleted(pi2.getId());
        assertNodeTriggered(pi2.getId(), "End Event");
    }

    @Test
    public void test_process_multiple_event() {
        HashMap<String, Object> parameters = new HashMap<>();
        ProcessInstance pi = ((CorrelationAwareProcessRuntime)kieSession).startProcess("org.redhat.bpm.process-signaling-multiple-events", getCorrelationKey("key3"), parameters);

        assertProcessInstanceActive(pi.getId());
        assertNodeTriggered(pi.getId(), "StartProcess", "Script Task 1");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.exit(-1);
        }

        kieSession.signalEvent("event1", "OK", pi.getId());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.exit(-1);
        }

        kieSession.signalEvent("event2", "OK", pi.getId());

        assertProcessInstanceCompleted(pi.getId());
        assertNodeTriggered(pi.getId(), "End Event");

    }

    @Test
    public void test_process_multiple_event_subprocess() {
        HashMap<String, Object> parameters = new HashMap<>();
        ProcessInstance pi = ((CorrelationAwareProcessRuntime)kieSession).startProcess("org.redhat.bpm.process-signaling-multiple-events-sub", getCorrelationKey("key4"), parameters);

        assertProcessInstanceActive(pi.getId());
        assertNodeTriggered(pi.getId(), "StartProcess", "Script Task 1");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.exit(-1);
        }

        kieSession.signalEvent("event_sub", "OK", pi.getId());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.exit(-1);
        }
        ;

        assertProcessInstanceCompleted(pi.getId());
        assertNodeTriggered(pi.getId(), "End Event");

    }

    @Test
    public void test_process_topic_sub() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("destinationName", "jms/topic/bpmnProcessesTopic");
        parameters.put("containerId", "org.redhat.bpm:bpm-process-signaling:0.0.1-SNAPSHOT");
        parameters.put("eventPublished", "task1_completed");
        parameters.put("jmsConnection", "remote");
        ProcessInstance pi = ((CorrelationAwareProcessRuntime)kieSession).startProcess("org.redhat.bpm.process-signaling-topic-pub", getCorrelationKey("key5"), parameters);

        assertProcessInstanceActive(pi.getId());
        assertNodeTriggered(pi.getId(), "StartProcess", "Script Task 1");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.exit(-1);
        }


        String signalValue = null;
        List<? extends VariableInstanceLog> history = auditService.findVariableInstances(pi.getId());
        for (VariableInstanceLog log : history) {
            if ("eventPublished".equals(log.getVariableId())) {
                signalValue = log.getValue();
                LOG.info("Found eventPublished:" + signalValue);
                break;
            }
        }

        kieSession.signalEvent(signalValue, "OK", pi.getId());

        assertProcessInstanceCompleted(pi.getId());
        assertNodeTriggered(pi.getId(), "End Event");

    }

    private CorrelationKey getCorrelationKey(String key) {
        return factory.newCorrelationKey(key);
    }

}
