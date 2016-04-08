package io.sunfly.push;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

public class Config {
    private InetAddress wanListenIp;
    private int wanListenPort;

    private InetAddress lanListenIp;
    private int lanListenPort;

    private String cassandraAddress;

    private Config() {

    }

    public static Config load() throws IOException {
        InputStream is = Config.class.getResourceAsStream("/config.properties");
        Properties props = new Properties();
        props.load(is);
        is.close();

        Config conf = new Config();
        conf.wanListenIp = InetAddress.getByName(props.getProperty("wan.listen.ip").trim());
        conf.wanListenPort = Integer.parseInt(props.getProperty("wan.listen.port").trim());
        conf.lanListenIp = InetAddress.getByName(props.getProperty("lan.listen.ip").trim());
        conf.lanListenPort = Integer.parseInt(props.getProperty("lan.listen.port").trim());

        conf.cassandraAddress = props.getProperty("cassandra.address");

        return conf;
    }

    public InetAddress getWanListenIp() {
        return wanListenIp;
    }

    public int getWanListenPort() {
        return wanListenPort;
    }

    public InetAddress getLanListenIp() {
        return lanListenIp;
    }

    public int getLanListenPort() {
        return lanListenPort;
    }

    public String getCassandraAddress() {
        return cassandraAddress;
    }
}
