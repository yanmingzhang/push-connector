package io.sunfly.push;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private int listenPort;

    private String address;

    private Config() {

    }

    public static Config load() throws IOException {
        InputStream is = Config.class.getResourceAsStream("/config.properties");
        Properties props = new Properties();
        props.load(is);
        is.close();

        Config conf = new Config();
        conf.listenPort = Integer.parseInt(props.getProperty("listen.port"));
        conf.address = props.getProperty("cassandra.address");

        return conf;
    }

    public int getListenPort() {
        return listenPort;
    }

    public String getAddress() {
        return address;
    }
}
