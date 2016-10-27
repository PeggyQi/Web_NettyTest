package com.test.nettyclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NettyClientThread implements Runnable{
      private String host;
      private int port;
      private Selector selector;
      private SocketChannel socketChannel;
      private volatile boolean stop; 
      public NettyClientThread(String host,int port){
    	  this.host=host == null?"127.0.0.1":host;
    	  this.port=port;
    	  try {
			selector=Selector.open();
			socketChannel=SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
      }

	public void run() {
		// TODO Auto-generated method stub
		doConnect();
		while(!stop)
		{
			try {
				int selectnumber=selector.select();
				Set<SelectionKey> selectedKeys=selector.selectedKeys();
				Iterator it=selectedKeys.iterator();
				SelectionKey key=null;
				while(it.hasNext()){
					key=(SelectionKey) it.next();
					it.remove();
					handleInput(key);
//					if(key!=null){
//						key.cancel();
//					    if(key.channel()!=null){
//					    	key.channel().close();
//					    }
//					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(selector!=null)
			try {
				selector.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void handleInput(SelectionKey key) {
		// TODO Auto-generated method stub
		if(key.isValid()){//判断是否连接成功
			SocketChannel socketChannel=(SocketChannel) key.channel();
			if(key.isConnectable()){
				try {
					if(socketChannel.finishConnect()){
						socketChannel.register(selector,SelectionKey.OP_READ);
						doWrite(socketChannel,"hi ,I'm client");
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			if(key.isReadable()){
				ByteBuffer readBuffer= ByteBuffer.allocate(1024);
				try {
					int readbytes=socketChannel.read(readBuffer);
					if(readbytes>0){
						readBuffer.flip();
						byte[] bytes=new byte[readBuffer.remaining()];
						readBuffer.get(bytes);
						String body=new String(bytes,"UTF-8");
						System.out.println("client receive msg :"+body);
						this.stop=true;
					}
					else if(readbytes<0){
						key.cancel();
						socketChannel.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

	private void doConnect() {
		// TODO Auto-generated method stub
		try {
			if(socketChannel.connect(new InetSocketAddress(host,port))){//如果连接成功，注册到多路复用器上，发送请求消息，读应答
				socketChannel.register(selector,SelectionKey.OP_READ);
				doWrite(socketChannel,"hi i'm client");
			}
			else
				socketChannel.register(selector,SelectionKey.OP_CONNECT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doWrite(SocketChannel socketChannel,String msg) {
		// TODO Auto-generated method stub
		byte[] bytes=msg.getBytes();
		ByteBuffer writeBuffer=ByteBuffer.allocate(bytes.length);
		writeBuffer.put(bytes);
		writeBuffer.flip();
		try {
			socketChannel.write(writeBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!writeBuffer.hasRemaining())
		   System.out.print("client send msg successed");
	}
}
