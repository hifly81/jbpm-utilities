package org.redhat.bpm.extensions.command;

import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;


public class CancelTimerAndReloadCommand extends AbstractTimerActionCommand implements Command {

	public ExecutionResults execute(CommandContext ctx) throws Exception {
		return super.execute(ctx, updateTimerService::cancelTimerAndReload);
	}

}