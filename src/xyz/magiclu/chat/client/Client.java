package xyz.magiclu.chat.client;

import java.io.IOException;

/**
 * Created by Administrator on 2018/7/31.
 */
public interface Client {

    /*
    客户端通宵功能：
        1.发送
        2.接受
        3.关闭客户端
        4.强行清空缓冲区
     */

    /**
     * 发送
     */
    void send(String message) throws IOException;

    /**
     *
     */
    byte[] receive() throws IOException;

    /**
     *
     */
    void close() throws IOException;

    void flush() throws IOException;
}
