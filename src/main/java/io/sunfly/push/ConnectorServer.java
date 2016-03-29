package io.sunfly.push;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class ConnectorServer
{
    private final Config conf;

    public ConnectorServer(Config conf) {
        this.conf = conf;
    }

    public void run() throws Exception {
        final RabbitmqClient rabbitmqClient = new RabbitmqClient(conf);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ReadTimeoutHandler(300));  // 5 minutes
                pipeline.addLast(new PushMessageEncoder());
                pipeline.addLast(new PushMessageDecoder());
                pipeline.addLast(new ConnectorServerHandler(rabbitmqClient));
            }
          })
          .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);

        // Bind and start to accept incoming connections.
        ChannelFuture f = sb.bind(conf.getListenPort()).sync();
        f.channel().closeFuture().sync();

        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        rabbitmqClient.close();
    }

    public static void main(String[] args) throws Exception {
        Config conf = Config.load();
        new ConnectorServer(conf).run();
    }
}
