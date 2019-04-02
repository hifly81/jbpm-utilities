package org.redhat.bpm.wid;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadProcesses implements WorkItemHandler {

	private static Logger LOG = LoggerFactory.getLogger(LoadProcesses.class);
	private final RuntimeManager runtimeManager;

	public LoadProcesses(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String processDefinition = (String) workItem.getParameter("processDefinition");
		String kieDeployment = (String) workItem.getParameter("kieDeployment");
		List<Set<VariableHolder>> variables = (List<Set<VariableHolder>> ) workItem.getParameter("variables");

		if(processDefinition == null || processDefinition.equals("")) {
			LOG.error("LoadProcesses can't be executed, no processDefinition!");
			manager.abortWorkItem(workItem.getId());
			return;
		}

		if(kieDeployment == null || kieDeployment.equals("")) {
			LOG.error("LoadProcesses can't be executed, no kieDeployment!");
			manager.abortWorkItem(workItem.getId());
			return;
		}

		if(variables == null || variables.isEmpty()) {
			LOG.error("LoadProcesses can't be executed, no input variables to match provided!");
			manager.abortWorkItem(workItem.getId());
			return;
		}


		List<Long> processInstanceIds = loadProcessInstances(kieDeployment, processDefinition);
		
		LOG.info("Found {} processInstances from container {}. Process definition {}.", processInstanceIds.size(), kieDeployment, processDefinition);

		List<Long> processes = filterProcessInstancesWithVariables(processInstanceIds, variables);

		LOG.info("Found {} processes to execute abort...", processes.size());

		HashMap<String,Object> results = new HashMap<String, Object>();
		results.put("processesIds", processes);
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

	protected List<Long> filterProcessInstancesWithVariables(List<Long> processInstanceIds, List<Set<VariableHolder>> variables) {
		RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get());
		AuditService auditService = runtimeEngine.getAuditService();

		List<Long> processes = new ArrayList<>();
		if(processInstanceIds != null && !processInstanceIds.isEmpty()) {
			for(Long processInstanceId: processInstanceIds) {
				LOG.info("Evaluating variables for process instance {} ...", processInstanceId);
				//find variables for process instance
				List<? extends VariableInstanceLog> variableInstanceLogs = auditService.findVariableInstances(processInstanceId);
				List<VariableHolder> variablesToMatch = variableInstanceLogs.stream()
				.map(v -> new VariableHolder( v.getVariableId(),  v.getValue()))
				.collect(Collectors.toList());

				boolean matching = false;

				//find exact combination in list
				int numberOfMatchesToFound = variables.get(0).size();
				for(Set<VariableHolder> variableHolderSet: variables) {
					int countNumberOfMatchesToFound = 0;
					for(VariableHolder variableHolder: variableHolderSet) {
						if(variablesToMatch.contains(variableHolder))
							countNumberOfMatchesToFound++;
					}
					if(countNumberOfMatchesToFound == numberOfMatchesToFound) {
						matching = true;
						break;
					}

				}

				if(matching) {
					LOG.info("****Process instance {} contains matching variables.****", processInstanceId);
					processes.add(processInstanceId);
				} else {
					LOG.warn("Process instance {} does not contain matching variables: -->", processInstanceId);
					for(VariableHolder variableHolder: variablesToMatch)
						LOG.warn("variable {} - value {}", variableHolder.getName(), variableHolder.getValue());


				}
			}
		}

		return processes;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		throw new UnsupportedOperationException();
	}
	
}
