package io.sunfly.push;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private int listenPort;

    private int mqPrefetchCount;
    private String mqAddresses;
    private String mqUsername;
    private String mqPassword;

    private Config() {

    }

    public static Config load() throws IOException {
        InputStream is = Config.class.getResourceAsStream("/config.properties");
        Properties props = new Properties();
        props.load(is);
        is.close();

        Config conf = new Config();
        conf.setListenPort(Integer.parseInt(props.getProperty("listen.port")));
        conf.setMqPrefetchCount(Integer.parseInt(props.getProperty("mq.prefetchCount")));
        conf.setMqAddresses(props.getProperty("mq.addresses"));
        conf.setMqUsername(props.getProperty("mq.username"));
        conf.setMqPassword(props.getProperty("mq.password"));

        return conf;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getMqPrefetchCount() {
        return mqPrefetchCount;
    }

    public void setMqPrefetchCount(int mqPrefetchCount) {
        this.mqPrefetchCount = mqPrefetchCount;
    }

    public String getMqAddresses() {
        return mqAddresses;
    }

    public void setMqAddresses(String mqAddresses) {
        this.mqAddresses = mqAddresses;
    }

    public String getMqUsername() {
        return mqUsername;
    }

    public void setMqUsername(String mqUsername) {
        this.mqUsername = mqUsername;
    }

    public String getMqPassword() {
        return mqPassword;
    }

    public void setMqPassword(String mqPassword) {
        this.mqPassword = mqPassword;
    }
}
