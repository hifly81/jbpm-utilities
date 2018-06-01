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
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class UserTaskTest extends JbpmJUnitBaseTestCase {

	private RuntimeManager runtimeManager;
	private RuntimeEngine runtimeEngine;
	private KieSession kieSession;
	private TaskService taskService;
	private AuditService auditService;

	public UserTaskTest() {
		super(true, true);
	}

	@Before
	public void before() {
		runtimeManager = createRuntimeManager("org/redhat/bpm/process/user-task.bpmn2", "org/redhat/bpm/process/user-task-nogroup.bpmn2");
		runtimeEngine = getRuntimeEngine();
		kieSession = runtimeEngine.getKieSession();
		taskService = runtimeEngine.getTaskService();
		auditService = runtimeEngine.getAuditService();
	}

	@After
	public void after() {
		runtimeManager.disposeRuntimeEngine(runtimeEngine);
		runtimeManager.close();
	}

	//@Test
	public void test() {

		HashMap<String, Object> params = new HashMap<>();
		
		ArrayList<String> proposalManagers = new ArrayList<String>();
		proposalManagers.add("MJ1");
		proposalManagers.add("DP2");
		
		String proposalCode = "739";
		
		params.put("proposalCode", proposalCode);
		params.put("proposalManagers", proposalManagers);
		ProcessInstance pi = kieSession.startProcess("com.redhat.bpm.ht.user-task", params);

		assertProcessInstanceActive(pi.getId());
		assertNodeTriggered(pi.getId(), "StartProcess", "ForwardProposal");

		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("kermit", "");
		assertEquals(1, tasks.size());
		
		TaskSummary taskSummary = tasks.get(0);
		Map<String, Object> taskContent = taskService.getTaskContent(taskSummary.getId());
		String code = (String) taskContent.get("code");
		List<?> managers = (List<?>) taskContent.get("managers");
		
		assertEquals(proposalCode, code);
		assertEquals(proposalManagers, managers);
		
		taskService.start(taskSummary.getId(), "kermit");
		taskService.complete(taskSummary.getId(), "kermit", new HashMap<>());
		
		assertProcessInstanceCompleted(pi.getId());
		assertNodeTriggered(pi.getId(), "EndProcess");

	}

	//@Test
	public void test_nominate_no_group() {

		HashMap<String, Object> params = new HashMap<>();

		ArrayList<String> proposalManagers = new ArrayList<String>();
		proposalManagers.add("MJ1");
		proposalManagers.add("DP2");

		String proposalCode = "739";

		params.put("proposalCode", proposalCode);
		params.put("proposalManagers", proposalManagers);
		ProcessInstance pi = kieSession.startProcess("com.redhat.bpm.ht.user-task-nogroup", params);

		assertProcessInstanceActive(pi.getId());
		assertNodeTriggered(pi.getId(), "StartProcess", "ForwardProposal");

		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("kermit", "");

		assertEquals(0, tasks.size());

		List<Long> openTasks = taskService.getTasksByProcessInstanceId(pi.getId());

		assertTrue(openTasks.size() == 1);

		Group group = TaskModelProvider.getFactory().newGroup();
		((InternalOrganizationalEntity) group).setId( "HR");

		List<OrganizationalEntity> entities = new ArrayList<>();
		entities.add(group);

		//taskService.nominate(openTasks.get(0), "Administrator", entities);
		taskService.nominate(openTasks.get(0), "bpmsAdmin", entities);

		tasks = taskService.getTasksAssignedAsPotentialOwner("kermit", "");

		assertEquals(1, tasks.size());

		taskService.start(openTasks.get(0), "kermit");
		taskService.complete(openTasks.get(0), "kermit", new HashMap<>());

		assertProcessInstanceCompleted(pi.getId());
		assertNodeTriggered(pi.getId(), "EndProcess");


	}

}
