package xyz.magiclu.chat.server;

import xyz.magiclu.chat.client.Client;
import xyz.magiclu.chat.client.TCPClient;
import xyz.magiclu.chat.util.ChatRequest;
import xyz.magiclu.chat.util.ChatResponse;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * 接受client链接信道
 * 保存信道
 */
public class TCPChannel extends Thread {

	private static int count = 0;                  //线程计数器
	private Client client;						   //客户端通讯器
	private ExecutorService executorService;   //服务器处理类

	private byte[] bytes = new byte[1024];         //读入缓冲区

	/**
	 * 保存信道
	 * @param sk   注入socket
	 * @param name 注入信道名
     */
	public TCPChannel(Socket sk, String name){

		synchronized (TCPChannel.class) {

			executorService = ExecutorService.getInstance();
			this.setName(name);
			count++;
			this.client = new TCPClient(sk);
		}
	}

	/**
	 * 维持信道，时刻监听信道的请求，拿给处理器解析
	 */
	@Override
	public void run() {

		while (!this.isInterrupted()) {
			try {

				if ((bytes = client.receive()) != null) {
					//得到信息
					String msg = new String(bytes);

					//查看请求字符串是否为特殊信号
					executorService.parse(new ChatRequest(msg));

				}
			} catch (SocketException e){

				try {
					client.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 向客户端做出响应
	 * 		1.如果response里的domain属性为all则响应
	 * 		2.如果response的domain属性为当前线程的name则响应
	 * @param response
     */
	public void ack(ChatResponse response){

		//System.out.println("response  domain:"+response.getDomain());
		System.out.println("response toString: "+response.toString());
		if(response.isSpecialResponse()){

			try {
				client.send(response.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			//如果响应头里domain属性值为all
			if ("all".equals(response.getDomain())) {

				//发送所有信息
				client.send(response.toString());
				client.flush();
			}else {

				//否则如果该响应响应是否命中当前信道
				if(this.getName().equals(response.getDomain())) {

					//发送所有信息
					client.send(response.toString());
					client.flush();
				}else {

					//否则只发送在线用户列表
					client.send(response.getUsers());
					client.flush();
				}
			}
		}catch (IOException e) {
			//信道发送异常
			e.printStackTrace();
		}
	}

	/**
	 * 关闭当前信道
	 * @throws IOException
     */
	public void close() throws IOException {

		//中断线程并关闭
		this.interrupt();
		client.close();
	}

}
 
