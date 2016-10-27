package com.test.nettyserver;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class NettyServerThread implements Runnable{
	
	private ServerSocketChannel servChannel;//监听客户端连接是所有客户端连接的父通道
	private Selector selector;//Reactor线程，创建多路复用，轮询 . 一个单独的线程可以管理多个channel，从而管理多个网络连接。
	private boolean stop;
	/**
	 * 初始化多路复用器，绑定监听端口
	 * @param port
	 */
	public NettyServerThread(int port)
	{
		try {
			selector =Selector.open();
			servChannel=ServerSocketChannel.open();
			servChannel.configureBlocking(false);//设置为非阻塞模式
			servChannel.socket().bind(new InetSocketAddress(port),1024);//绑定接听端口
			servChannel.register(selector,SelectionKey.OP_ACCEPT);//将servchannel注册到Reactor线程的多路复用器selector上
	        System.out.println("server is starting in port"+port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop()
	{
		this.stop=true;
	}

	public void run() {
		// TODO Auto-generated method stub
		while(!stop)
		{
			try {
				int num=selector.select();//多路复用器在线程run()方法下轮询准备就绪的key 返回SelectionKey对象
				Set<SelectionKey> selectedkeys =selector.selectedKeys(); //通过该方法获得监听到的就绪通道
				Iterator<SelectionKey> it=selectedkeys.iterator();
				SelectionKey key=null;
				while(it.hasNext())
				{
					key=it.next();
					it.remove();   //自己移除键集中的通道
					handleInput(key);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key)  {
		// TODO Auto-generated method stub
		if(key.isValid())//处理新接入的请求消息
		{
		if(key.isAcceptable())//accept the new connection
		{
			ServerSocketChannel ssc=(ServerSocketChannel )key.channel();
			try {
				SocketChannel socketchannel=ssc.accept();//得到客户端通道
				socketchannel.configureBlocking(false);
				socketchannel.register(selector,SelectionKey.OP_READ);//将新接入的客户端向selector注册监听读就绪，用来读取客户端发送的网络消息
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(key.isReadable()){ //Read the data
			SocketChannel sc=(SocketChannel) key.channel();
			ByteBuffer readBuffer=ByteBuffer.allocate(1024);
			try {
				int readnumber= sc.read(readBuffer);//异步读取客户端请求消息到缓冲区
				if(readnumber>0){
					readBuffer.flip();//将limit置为position位置，将position置为0，Buffer为读取数据做准备
					byte[] bytes=new byte[readBuffer.remaining()];//返回limit和当前位置之间的元素个数
					readBuffer.get(bytes);
					String body=new String(bytes,"UTF-8");
					System.out.println("server receive order :"+body);
					dowrite(sc,"hello client,i'm server!");
				}
				else if(readnumber<0){//对端链路关闭
					key.cancel();
					sc.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		}
	}

	private void dowrite(SocketChannel sc, String msg) {
		// TODO Auto-generated method stub
		if(msg!=null && msg.trim().length()>0){
			byte[] bytes=msg.getBytes();
			ByteBuffer writeBuffer=ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			try {
				sc.write(writeBuffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!writeBuffer.hasRemaining())
				   System.out.print(" server send msg successed");
		}
		
	}

}
