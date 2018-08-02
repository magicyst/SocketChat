package xyz.magiclu.chat.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * tcp请求实现聊天室
 * Created by Administrator on 2018/7/31.
 */
public class TCPClient implements Client{

    private Socket sk;                         //客户端
    private OutputStream out;                  //输出
    private InputStream in;                    //输入

    private byte[] bytes = new byte[1024];     //信道读入缓冲区
    private int len = -1;                      //缓冲区长度

    /**
     * tcp协议客户端通宵
     * @param ip   服务端
     * @param port 服务器端口
     */
    public TCPClient(String ip, int port){

        try {

            //连接服务器
            sk = new Socket(ip,port);

            //初始化信道
            in = sk.getInputStream();
            out = sk.getOutputStream();
        } catch (IOException e) {
            //找不到服务器地址
            e.printStackTrace();
        }
    }

    /**
     *
     * @param sk 对外注入信道
     */
    public TCPClient(Socket sk){

        this.sk = sk;
        try {
            this.in = sk.getInputStream();
            this.out = sk.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送功能
     * @param message 发送给客户端的信息
     */
    @Override
    public void send(String message) throws IOException {

        //发送信息
        out.write(message.getBytes());
    }

    /**
     * 接受服务器端信息
     * @return             接受服务器信息返回字节数组,如果read不到则返回空字节数组
     * @throws IOException 信道读取异常
     */
    @Override
    public byte[] receive() throws IOException {

        byte[] temp = new byte[1024];

        if((len = in.read(bytes)) != -1)
            return Arrays.copyOf(bytes,len);

        return null;
    }

    /**
     * 关闭通道
     * @throws IOException 关闭通道异常
     */
    @Override
    public void close() throws IOException {

        in.close();
        out.close();
        sk.close();
    }

    /**
     * 清空缓冲区
     */
    @Override
    public void flush() throws IOException {

        out.flush();
    }


}
