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
import org.redhat.bpm.wid.RESTPooledWorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class RestPooledTest extends JbpmJUnitBaseTestCase {

    private final static Logger LOG = LoggerFactory.getLogger(RestPooledTest.class);

    private static final String PROCESS_FOLDER = "org/redhat/bpm/process/";

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
    private TaskService taskService;
    private CorrelationKeyFactory factory;


    private AuditService auditService;

    public RestPooledTest() {
        super(true, true);
        factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    }

    @Before
    public void before() {


        LogProcessEventListener listener = new LogProcessEventListener(false);
        addProcessEventListener(listener);

        runtimeManager = createRuntimeManager(
                PROCESS_FOLDER + "rest-sample.bpmn2");
        runtimeEngine = getRuntimeEngine();

        kieSession = runtimeEngine.getKieSession();
        kieSession.getWorkItemManager().registerWorkItemHandler("RestPool", new RESTPooledWorkItemHandler(true, 500, 50));

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

        ProcessInstance pi = ((CorrelationAwareProcessRuntime)kieSession).startProcess("test.rest-sample", getCorrelationKey("key1"), parameters);

        assertNodeTriggered(pi.getId(), "RESTPOOLED");
    }



    private CorrelationKey getCorrelationKey(String key) {
        return factory.newCorrelationKey(key);
    }

}
