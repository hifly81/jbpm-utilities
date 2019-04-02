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
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.redhat.bpm.wid.AddOwnershipTask;
import org.redhat.bpm.wid.LoadTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OwnershipOnTaskTest extends JbpmJUnitBaseTestCase {

	private final static Logger LOG = LoggerFactory.getLogger(OwnershipOnTaskTest.class);

	private static final String PROCESS_FOLDER = "org/redhat/bpm/process/";

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
	private TaskService taskService;

	private AuditService auditService;

    public OwnershipOnTaskTest() {
        super(true, true);
    }
    
    @Before
    public void before() {

        runtimeManager = createRuntimeManager(PROCESS_FOLDER + "task-sample.bpmn2", PROCESS_FOLDER + "technical-process.bpmn2");
        runtimeEngine = getRuntimeEngine();
        
        kieSession = runtimeEngine.getKieSession();
        kieSession.getWorkItemManager().registerWorkItemHandler("LoadTasks", new LoadTasks(runtimeManager));
		kieSession.getWorkItemManager().registerWorkItemHandler("AddOwnershipTask", new AddOwnershipTask(runtimeManager));
        
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
		ProcessInstance pi = kieSession.startProcess("it.redhat.demo.bpm.process.task-sample", parameters);
        ProcessInstance pi2 = kieSession.startProcess("it.redhat.demo.bpm.process.task-sample", parameters);

        assertProcessInstanceActive(pi.getId());
        assertProcessInstanceActive(pi2.getId());
        assertNodeTriggered(pi.getId(), "StartProcess", "Agent Task");
        assertNodeTriggered(pi2.getId(), "StartProcess", "Agent Task");

        List<TaskSummary> tasks = new ArrayList<>();
        tasks.addAll(taskService.getTasksByStatusByProcessInstanceId(pi.getId(), Arrays.asList(Status.Ready), ""));
        tasks.addAll(taskService.getTasksByStatusByProcessInstanceId(pi2.getId(), Arrays.asList(Status.Ready), ""));

        assertEquals(2, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(Status.Ready, task.getStatus());
        task = tasks.get(1);
        assertEquals(Status.Ready, task.getStatus());

        tasks.clear();
        //###### Start 2 BPMN processes with human tasks

        //###### Start it.redhat.demo.bpm.process.ownership-on-tasks process, it will add new potential owners to previous processes
        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("kieDeployment", "default-singleton");
        parameters2.put("processDefinition", "it.redhat.demo.bpm.process.task-sample");
        parameters2.put("potOwners", "kie-server,backoffice");
        parameters2.put("taskCreationDate", "1900-10-10");
        parameters2.put("technicalOperation", "TASKOWNERSHIP");
        ProcessInstance pi3 = kieSession.startProcess("it.redhat.demo.bpm.process.technical-process", parameters2);

        assertProcessInstanceCompleted(pi3.getId());
        //###### Start it.redhat.demo.bpm.process.ownership-on-tasks process, it will add new potential owners to previous processes


        //###### Verify that a new potential owner can work on tasks
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tasks.addAll(taskService.getTasksByStatusByProcessInstanceId(pi.getId(), Arrays.asList(Status.Ready), ""));
        tasks.addAll(taskService.getTasksByStatusByProcessInstanceId(pi2.getId(), Arrays.asList(Status.Ready), ""));

        assertEquals(2, tasks.size());
        task = tasks.get(0);
        assertEquals(Status.Ready, task.getStatus());

        taskService.claim(task.getId(), "giovanni");
        taskService.start(task.getId(), "giovanni");
        taskService.complete(task.getId(), "giovanni", null);

        task = tasks.get(1);
        assertEquals(Status.Ready, task.getStatus());

        taskService.claim(task.getId(), "giovanni");
        taskService.start(task.getId(), "giovanni");
        taskService.complete(task.getId(), "giovanni", null);

        assertProcessInstanceCompleted(pi.getId());
        assertProcessInstanceCompleted(pi2.getId());
        //###### Verify that a new potential owner can work on tasks


	}



}
