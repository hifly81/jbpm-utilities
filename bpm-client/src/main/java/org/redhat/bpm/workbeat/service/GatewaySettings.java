package org.redhat.bpm.workbeat.service;


import org.redhat.bpm.workbeat.util.Constants;

public class GatewaySettings {

    private String username;
    private String password;

    private String hostname;
    private Integer port;
    private String protocol;
    private Integer timeout;
    private String contextPath;

    private GatewaySettings() { }

    public static GatewaySettings create(String username, String password) {

        GatewaySettings instance = new GatewaySettings();
        instance.username = username;
        instance.password = password;
        instance.hostname = Constants.KIESERVER_HOSTNAME;
        instance.port = Constants.KIESERVER_PORT;
        instance.protocol = Constants.KIESERVER_PROTOCOL;
        instance.timeout = Constants.KIESERVER_TIMEOUT;
        instance.contextPath = Constants.KIESERVER_CONTEXTPATH;

        return instance;

    }

    public GatewaySettings hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public GatewaySettings port(Integer port) {
        this.port = port;
        return this;
    }

    public GatewaySettings protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public GatewaySettings hostname(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public GatewaySettings contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public String getContextPath() {
        return contextPath;
    }

}
