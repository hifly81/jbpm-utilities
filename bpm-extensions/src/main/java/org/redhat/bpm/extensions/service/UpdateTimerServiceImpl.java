package org.redhat.bpm.extensions.service;

import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.redhat.bpm.extensions.entity.TimerPayload;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.command.UpdateTimerCommand;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class UpdateTimerServiceImpl implements UpdateTimerService {

	private RuntimeManager runtimeManager;

	private static final Logger logger = LoggerFactory.getLogger(UpdateTimerServiceImpl.class);

	public void setAsTriggered(String identifier, long piid) {
		RuntimeEngine runtimeEngine = getRuntimeEngine(identifier, piid);
		KieSession kSession = runtimeEngine.getKieSession();
		WorkflowProcessInstance pi = (WorkflowProcessInstance) kSession.getProcessInstance(piid);
		List<NodeInstance> result = getTimerInstance(pi);
		if(result != null && ! result.isEmpty()) {
			((TimerNodeInstance)result.get(0)).triggerCompleted(true);
			dispose(runtimeEngine);
		}
	}


	public List<TimerPayload> cancelTimer(String identifier, long piid) {
		List<TimerPayload> timerPayloads = null;
		logger.info("Finding timers for process instance: " + piid + " in container:" + identifier);
		RuntimeEngine runtimeEngine = getRuntimeEngine(identifier, piid);
		KieSession kSession = runtimeEngine.getKieSession();
		WorkflowProcessInstance pi = (WorkflowProcessInstance) kSession.getProcessInstance(piid);
		List<NodeInstance> result = getTimerInstance(pi);
		TimerManager tm = getTimerManager(kSession);

		Map<Long, TimerInstance> timers =  tm.getTimerMap();
		logger.info("Timer Map:" + timers);

		if(timers != null && !timers.isEmpty()) {
			timerPayloads = new ArrayList<>(timers.size());
			for(Map.Entry<Long, TimerInstance> entry: timers.entrySet()) {
				logger.info("Found timer to be deleted with id:" + entry.getValue().getId());
				tm.cancelTimer(entry.getValue().getId());
				TimerPayload timerPayload = new TimerPayload();
				timerPayload.setDelay(entry.getValue().getDelay());
				timerPayload.setPeriod(entry.getValue().getPeriod());
				timerPayload.setRepeatLimit(entry.getValue().getRepeatLimit());
				timerPayload.setName(((TimerNodeInstance)result.get(0)).getTimerNode().getName());
				timerPayloads.add(timerPayload);
			}
		}

		dispose(runtimeEngine);
		return timerPayloads;
	}

	/**
	 * Find active nodes and if there are timers attached, it reschedule the timers with the remaining delay.
	 * Timer nodes handled are: TimerNodeInstance and StateBasedNodeInstance
	 */
	public void cancelTimerAndReload(String identifier, long piid) {
		RuntimeEngine runtimeEngine = getRuntimeEngine(identifier, piid);
		KieSession kSession = runtimeEngine.getKieSession();
		WorkflowProcessInstance pi = (WorkflowProcessInstance) kSession.getProcessInstance(piid);
		List<NodeInstance> result = getTimerInstance(pi);
		if (result.isEmpty())
			throw new IllegalStateException("Process " + piid + " is not on timer node!");


		TimerManager tm = getTimerManager(kSession);
		Map<Long, TimerInstance> timers = tm.getTimerMap();

		if (timers != null && !timers.isEmpty()) {
			for (Map.Entry<Long, TimerInstance> timerEntry : timers.entrySet()) {

				TimerInstance timer = timerEntry.getValue();

				if(piid != timer.getProcessInstanceId()) {
					throw new IllegalStateException("Process " + piid + " is not the same linked to the timer node! Process id on timer node is:" + timer.getProcessInstanceId());
				}

				logger.info("Timer id to check {} for Process {}", timer.getTimerId(), timer.getProcessInstanceId());

				for (NodeInstance nodeInstance : result) {

					logger.info("Evaluating Active node instance {} for Process {} to search for timer...", nodeInstance.getClass().getName(), pi.getId());

					if (nodeInstance instanceof TimerNodeInstance) {

						TimerNodeInstance tni = (TimerNodeInstance) nodeInstance;
						if (tni.getTimerId() == timer.getId()) {
							TimerInstance newTimer = rescheduleTimer(timer, tm);
							logger.info("New timer {} about to be registered for Process {}", newTimer, timer.getProcessInstanceId());
							tm.registerTimer(newTimer, pi);
							((TimerNodeInstance) nodeInstance).internalSetTimerId(newTimer.getId());
							logger.info("New timer {} successfully registered for Process {}", newTimer, newTimer.getProcessInstanceId());
							break;
						} else {
							logger.info("ERROR timer mismatch - timerId {} - timer id in node {}", timer.getId(), tni.getTimerId());
						}
					} else if (nodeInstance instanceof StateBasedNodeInstance) {
						StateBasedNodeInstance sbni = (StateBasedNodeInstance) nodeInstance;
						List<Long> timerList = sbni.getTimerInstances();

						logger.info("Evaluating timers linked to state based node {} for Process {}", timerList, timer.getProcessInstanceId());

						if ((timerList != null && timerList.contains(timer.getId()))) {

							TimerInstance newTimer = rescheduleTimer(timer, tm);
							logger.info("New timer {} about to be registered for Process {}", newTimer, timer.getProcessInstanceId());
							tm.registerTimer(newTimer, pi);
							timerList.clear();
							timerList.add(newTimer.getId());

							sbni.internalSetTimerInstances(timerList);
							logger.info("New state based timer {} successfully registered for Process {}", newTimer, newTimer.getProcessInstanceId());
							break;

						}
						else {
							logger.info("No Timers linked to state based node, skip node for Process {}!", timer.getProcessInstanceId());
						}
					}
				}
			}
		} else {
			logger.info("No Timers found for Process {}", piid);
		}


	}

	// not tested!
	public void updateTimerNode(Long piid, String identifier, long delay,
			long period, int repeatLimit) {
		RuntimeEngine runtimeEngine = getRuntimeEngine(identifier, piid);
		KieSession kSession = runtimeEngine.getKieSession();
		WorkflowProcessInstance pi = (WorkflowProcessInstance) kSession
				.getProcessInstance(piid);
		List<NodeInstance> result = getTimerInstance(pi);
		UpdateTimerCommand cmd = new UpdateTimerCommand(piid, ((TimerNodeInstance)result.get(0))
				.getTimerNode().getName(), delay, period, repeatLimit);
		kSession.execute(cmd);
		dispose(runtimeEngine);
	}


	private static TimerManager getTimerManager(KieSession ksession) {
		KieSession internal = ksession;
		if (ksession instanceof CommandBasedStatefulKnowledgeSession) {
			internal = ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
					.getCommandService().getContext()).getKieSession();
		}
		return ((InternalProcessRuntime) ((StatefulKnowledgeSessionImpl) internal)
				.getProcessRuntime()).getTimerManager();
	}

	private void dispose(RuntimeEngine runtimeEngine) {
		runtimeManager.disposeRuntimeEngine(runtimeEngine);
	}
	
	private RuntimeEngine getRuntimeEngine(String identifier, Long piid) {
		runtimeManager = getRuntimeManager(identifier);
		RuntimeEngine runtimeEngine = null;
		runtimeEngine = runtimeManager
				.getRuntimeEngine(ProcessInstanceIdContext.get(piid));
		return runtimeEngine;
	}

	private RuntimeManager getRuntimeManager(String identifier) {
		RuntimeManager runtimeManager = RuntimeManagerRegistry.get()
				.getManager(identifier);

		if (runtimeManager == null) {
			throw new IllegalStateException(
					"There is no runtime manager for identifier " + identifier);
		}

		return runtimeManager;
	}

	private List<NodeInstance> getTimerInstance(WorkflowProcessInstance pi) {
		if (pi == null) {
			throw new IllegalArgumentException("Couldn't find process instance:" + pi.getId());
		}

		List<NodeInstance> result = new ArrayList<>();

		for (NodeInstance n : pi.getNodeInstances()) {

			logger.info("Active node instance {} for Process {}", n.getClass().getName(), pi.getId());

			if (n instanceof TimerNodeInstance || n instanceof StateBasedNodeInstance) {
				result.add(n);
			}
		}

		return result;
	}

	private TimerInstance rescheduleTimer(TimerInstance timer, TimerManager tm) {
		logger.info("Found timer {} that is going to be canceled", timer);
		long delay = timer.getDelay();
		int repeatLimit = timer.getRepeatLimit();
		long period = timer.getPeriod();
		tm.cancelTimer(timer.getTimerId());

		logger.info("Timer {} canceled successfully", timer);

		TimerInstance newTimer = new TimerInstance();

		if (delay != 0) {
			newTimer.setDelay(calculateDelay(delay, timer));
		}
		newTimer.setPeriod(period);
		newTimer.setRepeatLimit(repeatLimit);
		newTimer.setTimerId(timer.getTimerId());

		return newTimer;
	}


	private long calculateDelay(long delay, TimerInstance timer) {
		long diff = System.currentTimeMillis() - timer.getActivated().getTime();
		return delay - diff;
	}
}
