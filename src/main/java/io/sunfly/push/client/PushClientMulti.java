package io.sunfly.push.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.sunfly.push.PushMessageDecoder;
import io.sunfly.push.PushMessageEncoder;
import io.sunfly.push.message.LoginRequest;

public class PushClientMulti {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Usage: PushClientMulti <ip> <port> <start_client_seq> <end_client_seq>");
            return;
        }

        final String ip = args[0];
        final int port = Integer.parseInt(args[1]);
        final int startSeq = Integer.parseInt(args[2]);
        final int endSeq = Integer.parseInt(args[3]);

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
         .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(0, 30, 0));  // 30 seconds
                pipeline.addLast(new PushMessageEncoder());
                pipeline.addLast(new PushMessageDecoder());
                pipeline.addLast(new ClientHandler());
            }
        });

        // connect to server
        for (int i = startSeq; i < endSeq; ++i) {
            ChannelFuture f = b.connect(ip, port).sync();
            f.channel().writeAndFlush(new LoginRequest(String.format("push-dev-%07d", i)));
        }

        System.out.println("All connected...");

        Thread.sleep(Long.MAX_VALUE);
    }
}
