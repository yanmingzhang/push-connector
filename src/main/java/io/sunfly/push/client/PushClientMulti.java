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

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class PushClientMulti {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: PushClientMulti <conn_str> <client_count>");
            return;
        }

        InetSocketAddress[] addresses = parseAddresses(args[0]);
        int clientCount = Integer.parseInt(args[1]);

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
        Random random = new Random();
        for (int i = 0; i < clientCount; ++i) {
            final String deviceId = String.format("push-test-%07d", i);
            final InetSocketAddress address = addresses[random.nextInt(addresses.length)];
            ChannelFuture f = b.connect(address).await();
            if (f.isSuccess()) {
                f.channel().attr(ClientHandler.AK_DEVICE_ID).set(deviceId);
                f.channel().writeAndFlush(new LoginRequest(deviceId, Collections.<String, UUID>emptyMap()));
            } else {
                System.err.println("Connect to endpoint " + address + " failed");
            }
        }

        System.out.println("All connected...");

        Thread.sleep(Long.MAX_VALUE);
    }

    private static InetSocketAddress[] parseAddresses(String addresses) {
        String[] addrs = addresses.split(" *, *");
        InetSocketAddress[] res = new InetSocketAddress[addrs.length];
        for (int i = 0; i < addrs.length; i++) {
            res[i] = parseAddress(addrs[i]);
        }

        return res;
    }

    private static InetSocketAddress parseAddress(String addressString) {
        int idx = addressString.indexOf(':');
        if (idx == -1) {
            throw new IllegalArgumentException(addressString);
        }

        String ip = addressString.substring(0, idx);
        int port = Integer.parseInt(addressString.substring(idx + 1));

        return new InetSocketAddress(ip, port);
    }
}
