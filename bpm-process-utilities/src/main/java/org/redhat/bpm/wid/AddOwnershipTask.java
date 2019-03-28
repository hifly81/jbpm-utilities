package org.redhat.bpm.wid;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.redhat.bpm.command.AddPeopleAssignmentsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddOwnershipTask implements WorkItemHandler {

	private static Logger LOG = LoggerFactory.getLogger(AddOwnershipTask.class);

	private final RuntimeManager runtimeManager;

	public AddOwnershipTask(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		Long taskId = (Long) workItem.getParameter("taskId");
		String potOwners = (String) workItem.getParameter("potOwners");

		if(taskId == null || (potOwners == null && potOwners.equals(""))) {
			LOG.error("Add Ownership can't be executed, no taskId");
			manager.abortWorkItem(workItem.getId());
			return;
		}

		if(potOwners == null || potOwners.equals("")) {
			LOG.error("Add Ownership can't be executed, no list of pot owners");
			manager.abortWorkItem(workItem.getId());
			return;
		}


		RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine( EmptyContext.get() );

		TaskService taskService = runtimeEngine.getTaskService();
		Task task = taskService.getTaskById( taskId);

		LOG.info("Evaluating Task {} to add new PotOwner {}, status {}, actual pot owners {}, creation Date {}", taskId, potOwners, task.getTaskData().getStatus(), task.getPeopleAssignments().getPotentialOwners(), task.getTaskData().getCreatedOn());

		String[] potOwnersSplitted = potOwners.split(",");
		List<Group> groups = new ArrayList<>();
		for(String pot: potOwnersSplitted) {
			Group group = TaskModelProvider.getFactory().newGroup();
			((InternalOrganizationalEntity) group).setId(pot);
			groups.add(group);
		}

		OrganizationalEntity[] entities = new OrganizationalEntity[groups.size()];
		for(int i = 0; i< groups.size(); i ++) {
			entities[i] = groups.get(i);
		}

		taskService.execute(new AddPeopleAssignmentsCommand(task.getId(), AddPeopleAssignmentsCommand.POT_OWNER, entities, false));

		LOG.info("Task {} added new PotOwner {}", taskId, potOwners);

		manager.completeWorkItem(workItem.getId(), new HashMap<>());
		
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		
	}

}
