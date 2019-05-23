package org.redhat.bpm.extensions.command;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.definition.process.Node;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TriggerNodeCommand implements GenericCommand<ExecutionResults> {

    private static final long serialVersionUID = -8252686458877022331L;
    private static final Logger logger = LoggerFactory.getLogger(TriggerNodeCommand.class);

    private String containerId;
    private long processInstanceId;
    private String nodeName;
    private String nodeNameToCancel;
    private Boolean nodeNameToCancelRequired;

    private RuntimeManager runtimeManager;


    public TriggerNodeCommand(String containerId, long processInstanceId, String nodeName, String nodeNameToCancel, Boolean nodeNameToCancelRequired) {
        this.containerId = containerId;
        this.processInstanceId = processInstanceId;
        this.nodeName = nodeName;
        this.nodeNameToCancel = nodeNameToCancel;
        this.nodeNameToCancelRequired = nodeNameToCancelRequired;
    }


    @Override
    public ExecutionResults execute(Context context) {

        RuntimeEngine runtimeEngine = getRuntimeEngine(containerId, processInstanceId);
        KieSession kieSession = runtimeEngine.getKieSession();

        logger.debug("About to trigger (create) node instance for node {} in process instance {}", nodeName, processInstanceId);
        RuleFlowProcessInstance wfp = (RuleFlowProcessInstance) kieSession.getProcessInstance(processInstanceId, false);
        if (wfp == null) {
            throw new ProcessInstanceNotFoundException("Process instance with id " + processInstanceId + " not found");
        }

        if(nodeNameToCancelRequired != null && nodeNameToCancelRequired) {
            //node to cancel if property set
            NodeInstance nodeInstance = wfp.getNodeInstances(true).stream().filter(ni -> ni.getNodeName().equalsIgnoreCase(nodeNameToCancel)).findFirst().orElse(null);
            if (nodeInstance == null) {
                throw new IllegalStateException("Node instance with id " + nodeInstance.getId() + " not found");
            }
            logger.info("Found node instance {} to be canceled", nodeInstance);
            ((NodeInstanceImpl) nodeInstance).cancel();
        }

        Node node = getNodesRecursively((NodeContainer) wfp.getNodeContainer()).stream().filter(ni -> ni.getName().equalsIgnoreCase(nodeName)).findFirst().orElse(null);

        if (node == null) {
            throw new IllegalStateException("Node instance with name " + nodeName + " not found");
        }

        logger.debug("Triggering node {} on process instance {}", node, wfp);
        wfp.getNodeInstance(node).trigger(null, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
        logger.info("Node {} successfully triggered", node);

        return null;
    }

    public List<Node> getNodesRecursively(NodeContainer nodeContainer) {
        List<Node> nodes = new ArrayList<>();
        processNodeContainer(nodeContainer, nodes);
        return nodes;
    }

    public void processNodeContainer(org.jbpm.workflow.core.NodeContainer nodeContainer, List<Node> nodes) {

        for (Node node : nodeContainer.getNodes()){
            nodes.add(node);
            if (node instanceof org.jbpm.workflow.core.NodeContainer) {
                processNodeContainer((org.jbpm.workflow.core.NodeContainer) node, nodes);
            }
        }
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

}