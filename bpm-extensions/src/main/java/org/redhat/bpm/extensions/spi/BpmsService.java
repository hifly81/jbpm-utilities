package org.redhat.bpm.extensions.spi;

import org.redhat.bpm.extensions.command.CancelTimerCommand;
import org.redhat.bpm.extensions.command.SetTimerAsTriggeredCommand;
import org.jbpm.services.api.ProcessService;
import org.kie.api.executor.ExecutionResults;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.server.services.api.KieServerRegistry;
import org.redhat.bpm.extensions.command.TriggerNodeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BpmsService {

    private static final Logger LOG = LoggerFactory.getLogger(BpmsService.class);

    private final ProcessService processService;

    public BpmsService(ProcessService processService, KieServerRegistry context) {
        this.processService = processService;
    }


    public ExecutionResults cancelTimer(String containerId, Number processInstanceId, String eventPayload, String marshallingType) {
        ExecutionResults execute = processService
                .execute(containerId, ProcessInstanceIdContext.get(processInstanceId.longValue()),
                        new CancelTimerCommand(containerId, processInstanceId.longValue())
                );

        LOG.info("Verify cancelTimer Output: '{}'", execute);

        return execute;
    }

    public void triggerTimer(String containerId, Number processInstanceId, String eventPayload, String marshallingType) {
        ExecutionResults execute = processService
                .execute(containerId, ProcessInstanceIdContext.get(processInstanceId.longValue()),
                        new SetTimerAsTriggeredCommand(containerId, processInstanceId.longValue())
                );

        LOG.info("Verify triggerTimer Output: '{}'", execute);
    }

    public void triggerNode(String containerId, Number processInstanceId, String nodeName, String nodeNameToCancel, Boolean nodeNameToCancelRequired) {
        ExecutionResults execute = processService
                .execute(containerId, ProcessInstanceIdContext.get(processInstanceId.longValue()),
                        new TriggerNodeCommand(containerId, processInstanceId.longValue(), nodeName, nodeNameToCancel, nodeNameToCancelRequired
                ));

        LOG.info("Verify triggerNode Output: '{}'", execute);

    }


}
