package org.redhat.bpm.extensions.command;

import org.drools.core.command.impl.GenericCommand;
import org.kie.api.executor.ExecutionResults;
import org.kie.internal.command.Context;

/**
 * 
 * If the given process instance Id is stopped at a timer node, this will cancel it
 *
 * @author wsiqueir
 */
public class CancelTimerCommand extends AbstractTimerActionCommand implements GenericCommand<ExecutionResults> {


	public CancelTimerCommand(String containerId, Long processInstanceId) {
		super(containerId, processInstanceId, CommandType.CANCEL);
	}



	@Override
	public ExecutionResults execute(Context context) {
		try {
			return super.execute(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
