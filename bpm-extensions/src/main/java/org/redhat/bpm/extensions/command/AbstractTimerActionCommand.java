package org.redhat.bpm.extensions.command;

import org.redhat.bpm.extensions.entity.TimerPayload;
import org.redhat.bpm.extensions.service.UpdateTimerService;
import org.kie.api.executor.ExecutionResults;
import org.kie.internal.command.Context;

import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractTimerActionCommand {

	protected UpdateTimerService updateTimerService;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private String containerId;
	private Long processInstanceId;
	private CommandType commandType;

	public AbstractTimerActionCommand(String containerId, Long processInstanceId, CommandType commandType) {
		updateTimerService = UpdateTimerService.Factory.get();
		this.processInstanceId = processInstanceId;
		this.containerId = containerId;
		this.commandType = commandType;
	}


	public ExecutionResults execute(Context ctx) throws Exception {
		ExecutionResults executionResults = new ExecutionResults();
		try {
			String identifier = containerId;
			long piid = processInstanceId;
			switch (commandType) {
				case CANCEL:
					List<TimerPayload> timerPayloadList = updateTimerService.cancelTimer(identifier, piid);
					executionResults.setData("resultList", timerPayloadList);
					break;
				case TRIGGER:
					updateTimerService.setAsTriggered(identifier, piid);
					executionResults.setData("resultList", null);
					break;
			}



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return executionResults;
	}

}