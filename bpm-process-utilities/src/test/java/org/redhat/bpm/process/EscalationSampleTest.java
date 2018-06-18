package org.redhat.bpm.process;

import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKeyFactory;
import org.redhat.bpm.logging.LogProcessEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class EscalationSampleTest extends JbpmJUnitBaseTestCase {

    private final static Logger LOG = LoggerFactory.getLogger(EscalationSampleTest.class);

    private static final String PROCESS_FOLDER = "org/redhat/bpm/process/";

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
    private TaskService taskService;
    private CorrelationKeyFactory factory;


    private AuditService auditService;

    public EscalationSampleTest() {
        super(true, true);
        factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    }

    @Before
    public void before() {


        LogProcessEventListener listener = new LogProcessEventListener(false);
        addProcessEventListener(listener);

        runtimeManager = createRuntimeManager(PROCESS_FOLDER + "escalation-sample.bpmn2");
        runtimeEngine = getRuntimeEngine();

        kieSession = runtimeEngine.getKieSession();

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
    public void test() {
        HashMap<String, Object> parameters = new HashMap<>();

        ProcessInstance pi = kieSession.startProcess("org.redhat.bpm.escalation-sample", parameters);

        assertNodeTriggered(pi.getId(), "shipped");

    }
}
