package org.redhat.bpm.process;

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
import org.redhat.bpm.wid.AbortProcessTask;
import org.redhat.bpm.wid.LoadCSV;
import org.redhat.bpm.wid.LoadProcesses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ProcessAbortTest extends JbpmJUnitBaseTestCase {

	private final static Logger LOG = LoggerFactory.getLogger(ProcessAbortTest.class);

	private static final String PROCESS_FOLDER = "org/redhat/bpm/process/";

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
	private TaskService taskService;

	private AuditService auditService;

    public ProcessAbortTest() {
        super(true, true);
    }
    
    @Before
    public void before() {

        runtimeManager = createRuntimeManager(PROCESS_FOLDER + "process-sample.bpmn2", PROCESS_FOLDER + "technical-process.bpmn2");
        runtimeEngine = getRuntimeEngine();
        
        kieSession = runtimeEngine.getKieSession();
        kieSession.getWorkItemManager().registerWorkItemHandler("LoadProcesses", new LoadProcesses(runtimeManager));
        kieSession.getWorkItemManager().registerWorkItemHandler("LoadCSV", new LoadCSV(runtimeManager));
		kieSession.getWorkItemManager().registerWorkItemHandler("AbortProcessTask", new AbortProcessTask(runtimeManager));
        
        taskService = runtimeEngine.getTaskService();
        auditService = runtimeEngine.getAuditService();

    }
    
    @After
    public void after() {
        runtimeManager.disposeRuntimeEngine(runtimeEngine);
        runtimeManager.close();
    }
    
    @Test
	public void test() {

        //###### list of users and roles are in file: usergroups.properties

        //###### Start 2 BPMN processes with human tasks assigned to Director
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("group", "Director");
        parameters.put("var2", "01");
        parameters.put("var1", "DSDSDSD");
		ProcessInstance pi = kieSession.startProcess("it.redhat.demo.bpm.process.process-sample", parameters);


        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("group", "Director");
        parameters.put("var1", "DSDSDSD");
        ProcessInstance pi2 = kieSession.startProcess("it.redhat.demo.bpm.process.process-sample", parameters2);

        HashMap<String, Object> parameters3 = new HashMap<>();
        parameters3.put("group", "Director");
        parameters3.put("var2", "02");
        parameters.put("var1", "DSDSDSD");
        ProcessInstance pi3 = kieSession.startProcess("it.redhat.demo.bpm.process.process-sample", parameters3);

        assertProcessInstanceActive(pi.getId());
        assertProcessInstanceActive(pi2.getId());
        assertProcessInstanceActive(pi3.getId());
        assertNodeTriggered(pi.getId(), "StartProcess", "Agent Task");
        assertNodeTriggered(pi2.getId(), "StartProcess", "Agent Task");
        assertNodeTriggered(pi3.getId(), "StartProcess", "Agent Task");

        //###### Start 2 BPMN processes with human tasks

        //###### Start it.redhat.demo.bpm.process.technical-process
        HashMap<String, Object> parameters4 = new HashMap<>();
        parameters4.put("kieDeployment", "default-singleton");
        parameters4.put("processDefinition", "it.redhat.demo.bpm.process.process-sample");
        parameters4.put("technicalOperation", "ABORTPROCESS");
        parameters4.put("csvHeaderNumber", 2);
        parameters4.put("csvFile", "variables.csv");
        parameters4.put("loadVariables", true);


        ProcessInstance pi4 = kieSession.startProcess("it.redhat.demo.bpm.process.technical-process", parameters4);

        assertProcessInstanceCompleted(pi4.getId());

        assertProcessInstanceAborted(pi.getId());
        assertProcessInstanceActive(pi2.getId());
        assertProcessInstanceActive(pi3.getId());

	}

}
