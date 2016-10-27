package com.test.nettyserver;

public class NettyServer {
    public static void main(String[] args ){
    	int port=8081;
    	NettyServerThread nettyserver=new NettyServerThread(port);
    	new Thread(nettyserver,"NettyServerThread").start();
    }
}
