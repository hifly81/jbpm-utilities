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
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BusinessAdminTaskTest extends JbpmJUnitBaseTestCase {

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
    private TaskService taskService;
    private AuditService auditService;

    private static final String USER_ADMINISTRATOR = "Administrator";

    public BusinessAdminTaskTest() {
        super(true, true);
    }


    @Before
    public void before() {
        runtimeManager = createRuntimeManager("org/redhat/bpm/process/business-admin.bpmn2");
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

    @Test
    public void test_business_admin() {

        HashMap<String, Object> parameters = new HashMap<>();

        ProcessInstance pi = kieSession.startProcess("it.redhat.demo.business-admin", parameters);
        assertProcessInstanceActive(pi.getId());
        assertNodeTriggered(pi.getId(), "Start Process", "HR Task");

        List<TaskSummary> tasks = taskService.getTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, "");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(Status.Created, task.getStatus());

        Group group = TaskModelProvider.getFactory().newGroup();
        ((InternalOrganizationalEntity) group).setId( "hr");

        List<OrganizationalEntity> entities = new ArrayList<>();
        entities.add(group);

        taskService.nominate(task.getId(), "Administrator", entities);

        tasks = taskService.getTasksAssignedAsPotentialOwner("hr", "");
        assertEquals(1, tasks.size());
        task = tasks.get(0);

        assertEquals(Status.Ready, task.getStatus());

        taskService.claim(task.getId(), "paolo");
        taskService.start(task.getId(), "paolo");
        taskService.complete(task.getId(), "paolo", null);

        tasks = taskService.getTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, "");
        assertEquals(1, tasks.size());


    }
}
