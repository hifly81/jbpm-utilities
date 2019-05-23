package org.redhat.bpm.extensions.command;

import org.kie.api.executor.CommandContext;
import org.redhat.bpm.extensions.entity.TimerPayload;
import org.redhat.bpm.extensions.service.UpdateTimerService;
import org.kie.api.executor.ExecutionResults;
import org.kie.internal.command.Context;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public abstract class AbstractTimerActionCommand {

	protected UpdateTimerService updateTimerService;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private String containerId;
	private Long processInstanceId;
	private CommandType commandType;

	public AbstractTimerActionCommand() {
		updateTimerService = UpdateTimerService.Factory.get();
	}

	public AbstractTimerActionCommand(String containerId, Long processInstanceId, CommandType commandType) {
		updateTimerService = UpdateTimerService.Factory.get();
		this.processInstanceId = processInstanceId;
		this.containerId = containerId;
		this.commandType = commandType;
	}

	public ExecutionResults execute(CommandContext ctx,
									BiConsumer<String, Long> action) throws Exception {
		UserTransaction ut = (UserTransaction) InitialContext
				.doLookup("java:jboss/UserTransaction");
		try {
			ut.begin();
			String identifier = (String) ctx.getData().get("identifier");
			// avoid errors if users send processInstanceId as text
			String piidStr = String.valueOf(ctx.getData().get(
					"processInstanceId"));
			long piid = Long.parseLong(piidStr);
			logger.warning("Running action " + action + " on identifier "
					+ identifier + " and process instance " + piid);
			action.accept(identifier, piid);
			ut.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ut.rollback();
		}
		return null;
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