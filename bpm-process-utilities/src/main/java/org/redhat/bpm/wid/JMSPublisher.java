package org.redhat.bpm.wid;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.redhat.bpm.jms.IBMMQJMSProducer;
import org.redhat.bpm.jms.GenericJMSProducer;
import org.redhat.bpm.jms.JMSProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class JMSPublisher implements WorkItemHandler {

    private static Logger logger = LoggerFactory.getLogger("JMSPublisher");

    private String remoteUrl;
    private String username;
    private String password;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private RuntimeManager runtimeManager;

    public JMSPublisher(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

        String remoteCf = null;
        String initialCf = null;
        String localCf = null;
        String destinationType = "queue";
        String destinationName = (String) workItem.getParameter("destinationName");
        String jmsConnection = (String) workItem.getParameter("jmsConnection");
        Integer remotePort = null;
        String channelName = null;
        String queueManager = null;
        String topicObject = null;
        String restExceptionEndpoint = (String) workItem.getParameter("RestExceptionEndpoint");
        String restExceptionResponse = (String) workItem.getParameter("RestExceptionResponse");
        Integer restExceptionStatus = (Integer) workItem.getParameter("RestExceptionStatus");

        JMSProducer jmsProducer = null;

        if(workItem.getParameter("jmsConnection") == null)
            jmsConnection = "remote";

        if(workItem.getParameter("destinationName") == null)
            throw new IllegalArgumentException("destinationName can't be null!");

        if(workItem.getParameter("destinationType") != null)
            destinationType = (String)workItem.getParameter("destinationType");
        if(workItem.getParameter("remoteUrl") != null)
            remoteUrl = (String)workItem.getParameter("remoteUrl");
        if(workItem.getParameter("initialCf") != null)
            initialCf = (String)workItem.getParameter("initialCf");
        if(workItem.getParameter("remoteCf") != null)
            remoteCf = (String)workItem.getParameter("remoteCf");
        if(workItem.getParameter("localCf") != null)
            localCf = (String)workItem.getParameter("localCf");
        if(workItem.getParameter("username") != null)
            username = (String)workItem.getParameter("username");
        if(workItem.getParameter("password") != null)
            password = (String)workItem.getParameter("password");
        if(workItem.getParameter("remotePort") != null)
            remotePort = (Integer)workItem.getParameter("remotePort");
        if(workItem.getParameter("queueManager") != null)
            queueManager = (String)workItem.getParameter("queueManager");
        if(workItem.getParameter("channelName") != null)
            channelName = (String)workItem.getParameter("channelName");
        if(workItem.getParameter("topicObject") != null)
            topicObject = (String)workItem.getParameter("topicObject");

        try {

                ProcessPayload payload = new ProcessPayload();
                payload.setRestExceptionEndpoint(restExceptionEndpoint);
                payload.setRestExceptionResponse(restExceptionResponse);
                payload.setRestExceptionStatus(restExceptionStatus);
                payload.setProcessId(getProcessInfo(workItem.getProcessInstanceId()).getProcessId());

                //dispatch to specific JMS producer
                if(jmsConnection.equalsIgnoreCase("remote")) {
                    jmsProducer = new GenericJMSProducer();
                    jmsProducer.sendMsgToRemoteBroker(payload, username, password, initialCf, remoteCf, remoteUrl, destinationType, destinationName);
                }
                else if (jmsConnection.equalsIgnoreCase("local")) {
                    jmsProducer = new GenericJMSProducer();
                    jmsProducer.sendMsgToLocalBroker(payload, username, password, localCf, destinationType, destinationName);
                }
                else if (jmsConnection.equalsIgnoreCase("ibm")) {
                    jmsProducer = new IBMMQJMSProducer();
                    jmsProducer.sendMsgToIBMMQ(
                            payload, username, password, remoteUrl, remotePort, channelName, queueManager, destinationName, destinationType, topicObject);
                }
        }
        catch(Exception ex) {
            logger.error("can't publish errors on JMS audit:" + ex.getMessage());
        }
        finally {
            if(jmsProducer != null)
                try {
                    jmsProducer.closeConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        throw new RuntimeException("JMSPublisher is not abortable task");
    }

    private ProcessPayload getProcessInfo(Long piid) {
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine( EmptyContext.get() );
        AuditService auditService = runtimeEngine.getAuditService();

        ProcessInstanceLog pil = (ProcessInstanceLog) auditService.findProcessInstance( piid );
        if (pil == null) {
            return null;
        }

        while (pil.getParentProcessInstanceId() >= 1) {
            pil = (ProcessInstanceLog) auditService.findProcessInstance( pil.getParentProcessInstanceId() );
        }

        ProcessPayload processInfo = new ProcessPayload();
        processInfo.setProcessId(Long.valueOf(pil.getProcessId()));

        return processInfo;
    }

}
