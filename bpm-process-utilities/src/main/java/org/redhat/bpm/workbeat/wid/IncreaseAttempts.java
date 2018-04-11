package org.redhat.bpm.workbeat.wid;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import java.util.HashMap;

public class IncreaseAttempts implements WorkItemHandler {

	public final static String MAX_ATTEMPTS = "maxAttempts";
	public final static String NUM_ATTEMPTS = "numAttempts";
	public final static String INITIAL_DELAY_IN_SECONDS = "initialDelayInSeconds";
	public final static String CONTENT_TYPE = "contentType";
	public final static String DELAY_ATTEMPTS = "delayAttempts";
	public final static String CONNECTION_TIMEOUT = "connectionTimeout";
	public final static String READ_TIMEOUT = "readTimeout";

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		Integer numAttempts = (Integer) workItem.getParameter( NUM_ATTEMPTS );
		Integer initialDelayInSeconds = (Integer) workItem.getParameter( INITIAL_DELAY_IN_SECONDS );

		numAttempts++;

		HashMap<String, Object> results = new HashMap<>();
		results.put( NUM_ATTEMPTS, numAttempts );
		results.put( DELAY_ATTEMPTS, numAttempts * initialDelayInSeconds + "s" );

		manager.completeWorkItem( workItem.getId(), results );

	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}
