package org.redhat.bpm.extensions.command;

import org.drools.core.command.impl.GenericCommand;
import org.kie.api.executor.ExecutionResults;
import org.kie.internal.command.Context;

/**
 * 
 * If a process instance is stopped on a timer we can set it as triggered and
 * make the process skip it.
 * 
 * @author wsiqueir
 *
 */
public class SetTimerAsTriggeredCommand extends AbstractTimerActionCommand implements GenericCommand<ExecutionResults> {

	public SetTimerAsTriggeredCommand(String containerId, Long processInstanceId) {
		super(containerId, processInstanceId, CommandType.TRIGGER);
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