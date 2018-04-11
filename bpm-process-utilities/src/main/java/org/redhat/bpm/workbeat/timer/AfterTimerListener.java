package org.redhat.bpm.workbeat.timer;

import org.jbpm.workflow.instance.node.BoundaryEventNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.kie.api.definition.process.Node;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AfterTimerListener extends DefaultProcessEventListener {

	private static final Logger LOG = LoggerFactory.getLogger( AfterTimerListener.class );

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {
		NodeInstance nodeInstance = event.getNodeInstance();
		
		if (nodeInstance instanceof TimerNodeInstance) {

			LOG.debug( "afterNodeLeft TimerNodeInstance {} {} {}", nodeInstance.getClass(), nodeInstance.getNodeId(), nodeInstance.getNodeName() );
			
		} else if (nodeInstance instanceof BoundaryEventNodeInstance) {
			
			// BoundaryEventNodeInstance is used both for boundary signal and boundary timer
			// in actual version of the product 6.4.7.GA-redhat-1 the discriminant is:
			// the presence of attribute TimeDuration on node meta data
			
			Node node = nodeInstance.getNode();
			Map<String, Object> metaData = node.getMetaData();
			Object timeDuration = metaData.get( "TimeDuration" );
			
			if (timeDuration != null) {
				LOG.debug( "afterNodeLeft BoundaryEventNodeInstance with timeDuration {} {} {}", nodeInstance.getClass(), nodeInstance.getNodeId(), nodeInstance.getNodeName() );

			}
			
		}
		
	}
	
}
