package org.redhat.bpm.wid;

import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class AbortProcessTask implements WorkItemHandler {

	private static Logger LOG = LoggerFactory.getLogger(AbortProcessTask.class);

	private final RuntimeManager runtimeManager;

	public AbortProcessTask(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String kieDeployment = (String) workItem.getParameter("kieDeployment");
		Long processId = (Long) workItem.getParameter("processId");


		RuntimeEngine runtimeEngine = getRuntimeEngine(kieDeployment, processId);
		KieSession kieSession = runtimeEngine.getKieSession();

		LOG.info("Going to abort process instance {}", processId);

		RuleFlowProcessInstance wfp = (RuleFlowProcessInstance) kieSession.getProcessInstance(processId, false);
		if (wfp == null) {
			throw new ProcessInstanceNotFoundException("Process instance with id " + processId + " not found");
		}

		//state 3 is aborted
		wfp.setState(3);

		LOG.info("Process instance {} aborted", processId);

		manager.completeWorkItem(workItem.getId(), new HashMap<>());
		
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) { }

	private RuntimeEngine getRuntimeEngine(String identifier, Long piid) {
		RuntimeManager runtimeManager = getRuntimeManager(identifier);
		RuntimeEngine runtimeEngine = null;
		runtimeEngine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(piid));
		return runtimeEngine;
	}

	private RuntimeManager getRuntimeManager(String identifier) {
		RuntimeManager runtimeManager = RuntimeManagerRegistry.get().getManager(identifier);
		if (runtimeManager == null) {
			throw new IllegalStateException("There is no runtime manager for identifier " + identifier);
		}
		return runtimeManager;
	}

}
