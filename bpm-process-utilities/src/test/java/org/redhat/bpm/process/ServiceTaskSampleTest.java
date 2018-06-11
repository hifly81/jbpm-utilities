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
import org.kie.internal.process.CorrelationAwareProcessRuntime;
import org.kie.internal.process.CorrelationKeyFactory;
import org.redhat.bpm.logging.LogProcessEventListener;
import org.redhat.bpm.wid.JMSTopicPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ServiceTaskSampleTest extends JbpmJUnitBaseTestCase {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceTaskSampleTest.class);

    private static final String PROCESS_FOLDER = "org/redhat/bpm/process/";

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
    private TaskService taskService;
    private CorrelationKeyFactory factory;


    private AuditService auditService;

    public ServiceTaskSampleTest() {
        super(true, true);
        factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    }

    @Before
    public void before() {


        LogProcessEventListener listener = new LogProcessEventListener(false);
        addProcessEventListener(listener);

        runtimeManager = createRuntimeManager(PROCESS_FOLDER + "serviceTaskSample.bpmn2");
        runtimeEngine = getRuntimeEngine();

        kieSession = runtimeEngine.getKieSession();
        kieSession.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());

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
    public void test_sv() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("name", "gio");

        ProcessInstance pi = kieSession.startProcess("org.redhat.bpm.process.serviceTaskSample", parameters);

        assertNodeTriggered(pi.getId(), "Test Service Task");

        assertProcessInstanceCompleted(pi.getId());
    }
}
