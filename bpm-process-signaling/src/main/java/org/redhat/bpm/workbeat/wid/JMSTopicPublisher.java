package org.redhat.bpm.workbeat.wid;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class JMSTopicPublisher implements WorkItemHandler {

    //TODO changeit
    private static final String username = "";
    //TODO changeit
    private static final String password = "";
    private static final String REMOTING_URL = new String("remote://localhost:4447");
    private static final String INITIAL_CONTEXT_FACTORY = new String("org.jboss.naming.remote.client.InitialContextFactory");
    private static final String REMOTE_CONNECTION_FACTORY = new String("jms/RemoteConnectionFactory");
    private static final String CONNECTION_FACTORY = new String("java:/ConnectionFactory");

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        String destinationName = (String) workItem.getParameter("destinationName");
        String containerId = (String) workItem.getParameter("containerId");
        String eventPublished = (String) workItem.getParameter("eventPublished");
        String jmsConnection = (String) workItem.getParameter("jmsConnection");

        long processId = workItem.getProcessInstanceId();

        Context ic = null;
        ConnectionFactory cf = null;
        Connection connection = null;

        try {

            if(jmsConnection.equalsIgnoreCase("remote")) {
                ic = getRemoteInitialContext();
                cf = (ConnectionFactory) ic.lookup(REMOTE_CONNECTION_FACTORY);
            } else {
                ic = getInitialContext();
                cf = (ConnectionFactory) ic.lookup(CONNECTION_FACTORY);
            }


            Topic topic = (Topic)ic.lookup(destinationName);
            connection = cf.createConnection(username, password);
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer publisher = session.createProducer(topic);

            connection.start();

            ObjectMessage message = session.createObjectMessage();
            ProcessPayload processPayload = new ProcessPayload();
            processPayload.setContainerId(containerId);
            processPayload.setDestinationName(destinationName);
            processPayload.setEventPublished(eventPublished);
            processPayload.setProcessId(processId);

            message.setObject(processPayload);
            publisher.send(message);

        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.err.println("Can't publish to JMS Topic");
        }
        finally {
            if(ic != null) {
                try {
                    ic.close();
                }
                catch(Exception e) {
                    System.err.println("Can't close jms resources");
                }
            }
            closeConnection(connection);
        }

        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        throw new RuntimeException("JMSTopicPublisher is not abortable task");
    }

    private void closeConnection(Connection con) {
        try {
            if (con != null)
                con.close();
        }
        catch(JMSException jmse) {
            System.err.println("Could not close connection " + con +" exception was " + jmse);
        }
    }

    private Context getRemoteInitialContext() throws NamingException {

        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, REMOTING_URL);
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialContext(env);
    }

    private Context getInitialContext() throws NamingException {

        final Properties env = new Properties();
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialContext(env);
    }

}
