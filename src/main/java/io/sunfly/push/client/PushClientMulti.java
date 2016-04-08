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
import io.sunfly.push.wan.WanMessageDecoder;
import io.sunfly.push.wan.WanMessageEncoder;
import io.sunfly.push.wan.message.LoginRequest;

import java.util.Collections;
import java.util.UUID;

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
                pipeline.addLast(new WanMessageEncoder());
                pipeline.addLast(new WanMessageDecoder());
                pipeline.addLast(new ClientHandler());
            }
        });

        // connect to server
        for (int i = startSeq; i < endSeq; ++i) {
            final String deviceId = String.format("push-test-%07d", i);
            ChannelFuture f = b.connect(ip, port).sync();
            f.channel().attr(ClientHandler.AK_DEVICE_ID).set(deviceId);
            f.channel().writeAndFlush(new LoginRequest(deviceId, Collections.<String, UUID>emptyMap()));
        }

        System.out.println("All connected...");

        Thread.sleep(Long.MAX_VALUE);
    }
}
