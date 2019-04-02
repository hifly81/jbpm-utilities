package org.redhat.bpm.wid;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class LoadTasks implements WorkItemHandler {
	
	private static Logger LOG = LoggerFactory.getLogger(LoadTasks.class);
	private final RuntimeManager runtimeManager;
	
	public LoadTasks(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String processDefinition = (String) workItem.getParameter("processDefinition");
		String kieDeployment = (String) workItem.getParameter("kieDeployment");
		String taskCreationDate = (String) workItem.getParameter("taskCreationDate");

		if(processDefinition == null || processDefinition.equals("")) {
			LOG.error("LoadTasks can't be executed, no processDefinition!");
			manager.abortWorkItem(workItem.getId());
			return;
		}

		if(kieDeployment == null || kieDeployment.equals("")) {
			LOG.error("LoadTasks can't be executed, no kieDeployment!");
			manager.abortWorkItem(workItem.getId());
			return;
		}

		if(taskCreationDate == null || taskCreationDate.equals("")) {
			LOG.error("LoadTasks can't be executed, no taskCreationDate!");
			manager.abortWorkItem(workItem.getId());
			return;
		}

		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

		try {
			myFormat.parse(taskCreationDate);
		} catch (Exception e) {
			LOG.error("Task creation date is not in a valid format: valid format yyyy-MM-dd, current filter {}", taskCreationDate);
			manager.abortWorkItem(workItem.getId());
			return;
		}

		String[] dateSplitted = taskCreationDate.split("-");

		List<Long> processInstanceIds = loadProcessInstances(kieDeployment, processDefinition);
		
		LOG.info("Found {} processInstances from container {}. Process definition {}.", processInstanceIds.size(), kieDeployment, processDefinition);

		List<Long> tasks = loadTaskInstances(processInstanceIds, dateSplitted);

		LOG.info("Found {} tasks to handle new ownerships...", tasks == null? 0: tasks.size());

		HashMap<String,Object> results = new HashMap<String, Object>();
		results.put("taskInstanceIds", tasks);
		manager.completeWorkItem(workItem.getId(), results);
		
	}

	protected List<Long> loadProcessInstances(String kieDeployment, String processDefinition) {
		RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get());
		AuditService auditService = runtimeEngine.getAuditService();
		
		List<Long> processInstanceIds = auditService.findActiveProcessInstances(processDefinition).stream()
		.filter(pi -> kieDeployment.equals(pi.getExternalId()))
		.map(pi -> pi.getProcessInstanceId())
		.collect(Collectors.toList());
		return processInstanceIds;
	}

	protected List<Long> loadTaskInstances(List<Long> processInstanceIds, String[] dateSplitted) {
		RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get());
		TaskService taskService = runtimeEngine.getTaskService();

		Date date = Date.from(
				LocalDate.of( Integer.valueOf(dateSplitted[0]) ,  Integer.valueOf(dateSplitted[1])  ,  Integer.valueOf(dateSplitted[2])  ).atStartOfDay(ZoneId.systemDefault()).toInstant());

		List<Long> tasks = new ArrayList<>();
		if(processInstanceIds != null && !processInstanceIds.isEmpty()) {
			for(Long processInstanceId: processInstanceIds) {
				tasks.addAll(taskService.getTasksByStatusByProcessInstanceId(processInstanceId, Arrays.asList(Status.Ready, Status.InProgress), "").stream()
						.filter(ti -> ti.getCreatedOn().after(date))
						.map(ti -> ti.getId())
						.collect(Collectors.toList()));
			}
		}

		return tasks;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		throw new UnsupportedOperationException();
	}
	
}
