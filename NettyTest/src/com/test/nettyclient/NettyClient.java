package com.test.nettyclient;

public class NettyClient {
      public static void main(String args[]){
    	  int port=8081;
    	  new Thread(new NettyClientThread("127.0.0.1",port),"NettyClient").start();
      }
}
