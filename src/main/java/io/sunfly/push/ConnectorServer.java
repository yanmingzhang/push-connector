package io.sunfly.push;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.sunfly.push.lan.LanMessageDecoder;
import io.sunfly.push.lan.LanMessageEncoder;
import io.sunfly.push.lan.LanServerHandler;
import io.sunfly.push.wan.WanMessageDecoder;
import io.sunfly.push.wan.WanMessageEncoder;
import io.sunfly.push.wan.WanServerHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectorServer
{
    private final Config conf;
    private final ConcurrentMap<String, ChannelHandlerContext> devices;

    public ConnectorServer(Config conf) {
        this.conf = conf;

        devices = new ConcurrentHashMap<>(65536);
    }

    public Config getConf() {
        return conf;
    }

    public ConcurrentMap<String, ChannelHandlerContext> getDevices() {
        return devices;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap sb;

        // setting up internal listen port
        sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ReadTimeoutHandler(300));  // 5 minutes
                pipeline.addLast(new LanMessageEncoder());
                pipeline.addLast(new LanMessageDecoder());
                pipeline.addLast(new LanServerHandler(ConnectorServer.this));
            }
          })
          .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);

        ChannelFuture lanFuture = sb.bind(conf.getLanListenIp(), conf.getLanListenPort());

        // setting up public listen port
        final CassandraClient cassandraClient = new CassandraClient(conf.getCassandraAddress());
        final ExecutorService executorService = Executors.newFixedThreadPool(8);

        sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ReadTimeoutHandler(300));  // 5 minutes
                pipeline.addLast(new WanMessageEncoder());
                pipeline.addLast(new WanMessageDecoder());
                pipeline.addLast(new WanServerHandler(ConnectorServer.this, cassandraClient, executorService));
            }
          })
          .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);

        ChannelFuture wanFuture = sb.bind(conf.getWanListenIp(), conf.getWanListenPort());

        // Waiting for shutdown
        wanFuture.channel().closeFuture().sync();
        lanFuture.channel().closeFuture().sync();

        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        executorService.shutdown();
        cassandraClient.close();
    }

    public static void main(String[] args) throws Exception {
        Config conf = Config.load();
        new ConnectorServer(conf).run();
    }
}
