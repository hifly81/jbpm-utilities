package org.redhat.bpm.jms;

import javax.jms.*;
import javax.jms.IllegalStateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Properties;

public class GenericJMSProducer implements JMSProducer {

    private static final String REMOTING_URL = new String("remote://localhost:4447");
    private static final String INITIAL_CONTEXT_FACTORY = new String("org.jboss.naming.remote.client.InitialContextFactory");
    private static final String REMOTE_CONNECTION_FACTORY = new String("jms/RemoteConnectionFactory");
    private static final String CONNECTION_FACTORY = new String("java:/ConnectionFactory");

    private Session session = null;
    private MessageProducer publisher = null;
    private Connection connection;

    public void sendMsgToRemoteBroker(
            Serializable payload,
            String username,
            String password,
            String initialCf,
            String remoteCf,
            String remoteUrl,
            String destinationType,
            String destinationName) throws Exception {
        Context ic;
        ConnectionFactory cf;

        ic = getRemoteInitialContext(username, password, initialCf, remoteUrl);
        cf = (ConnectionFactory) ic.lookup(remoteCf == null? REMOTE_CONNECTION_FACTORY: remoteCf);

        Destination destination;
        if(destinationType.equalsIgnoreCase("queue"))
            destination = (Queue) ic.lookup(destinationName);
        else
            destination = (Topic) ic.lookup(destinationName);

        connection = cf.createConnection(username, password);
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        publisher = session.createProducer(destination);

        connection.start();

        ObjectMessage message = session.createObjectMessage();

        message.setObject(payload);
        publisher.send(message);

        closeConnection();

    }

    public void sendMsgToLocalBroker(
            Serializable payload,
            String username,
            String password,
            String localCf,
            String destinationType,
            String destinationName) throws Exception {
        Context ic;
        ConnectionFactory cf;

        ic = getInitialContext(username, password);
        cf = (ConnectionFactory) ic.lookup(localCf == null? CONNECTION_FACTORY: localCf);

        Destination destination;
        if(destinationType.equalsIgnoreCase("queue"))
            destination = (Queue) ic.lookup(destinationName);
        else
            destination = (Topic) ic.lookup(destinationName);

        connection = cf.createConnection(username, password);
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        publisher = session.createProducer(destination);

        connection.start();

        ObjectMessage message = session.createObjectMessage();

        message.setObject(payload);
        publisher.send(message);

        closeConnection();

    }

    @Override
    public void sendMsgToIBMMQ(Serializable payload, String username, String password, String remoteUrl, Integer remotePort, String channelName, String queueManager, String destinationName, String destinationType, String topicObject) throws Exception {
        throw new IllegalStateException("Not implemented");
    }

    private Context getRemoteInitialContext(String username, String password, String initialContextFactory, String remoteUrl) throws NamingException {

        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory == null? INITIAL_CONTEXT_FACTORY: initialContextFactory);
        env.put(Context.PROVIDER_URL, remoteUrl == null? REMOTING_URL: remoteUrl);
        if(username != null && password != null) {
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
        }
        return new InitialContext(env);
    }

    private Context getInitialContext(String username, String password) throws NamingException {

        final Properties env = new Properties();
        if(username != null && password != null) {
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
        }
        return new InitialContext(env);
    }

    public void closeConnection() throws Exception {
        publisher.close();
        session.close();
        connection.close();
    }

}
