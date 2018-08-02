package xyz.magiclu.chat.server;

import xyz.magiclu.chat.util.ChatLoginException;
import xyz.magiclu.chat.util.ChatRequest;
import xyz.magiclu.chat.util.ChatResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Administrator on 2018/7/26.
 */
public class ExecutorService {

    private static HashMap<String, TCPChannel> client_pool = new HashMap<>(); //用户信道

    private boolean client_close_signal = false;  //用户下线通知
    private String tapeout_name;

    /*
    内部类实现单例
     */
    public static class ChatExecutorServiceHolder {
        public static ExecutorService instance = new ExecutorService();
    }

    /**
     * @return 返回处理器实例
     */
    public static ExecutorService getInstance() {

        return ChatExecutorServiceHolder.instance;
    }

    /**
     * 私有构造器
     */
    private ExecutorService() {
    }

    /**
     * 加入信道
     *
     * @param sk 信道socket
     * @return 返回登录用户名
     */
    public String join(Socket sk) {

        synchronized (this) {
            //由于客户端链接的时候会把自己的用户名传来，
            byte[] bytes = new byte[1024];
            int len = -1;
            try {
                //接受用户名
                if ((len = sk.getInputStream().read(bytes)) != -1) {

                    //记录用户名
                    String username = new String(bytes, 0, len);

                    if(client_pool.containsKey(username))
                        throw new ChatLoginException("用户名以存在!");

                    //创建信道线程,保持socket
                    TCPChannel cli = new TCPChannel(sk, username);

                    //加入用户信道池
                    client_pool.put(username, cli);

                    //开启信道线程，不断的等待接受客户端的信息
                    cli.start();

                    parse(null);

                    return username;
                }

            } catch (ChatLoginException e) {

                //错误响应发送
                new TCPChannel(sk, "").ack(new ChatResponse("!error#"+e.getMessage()));

            } catch (IOException e) {
                //信道通讯异常
                e.printStackTrace();
            }

            return "";
        }
    }

    /**
     * 解析客户端socket流传来的命令
     *
     * @param chatRequest 客户端请求
     */
    public void parse(ChatRequest chatRequest) {

        //查看chatRequest是否包含特殊信号
        if (chatRequest != null && chatRequest.isSpecialRequest()) {

            //如果信号为关闭,则关闭当前信道
            if (chatRequest.getSignal().equals("close")) {
                try {

                    //关闭信道
                    this.closeByName(chatRequest.getMaster());

                    this.client_close_signal = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        //保存在线用户列表字符串
        String users = "";

        //在线池查询
        for (String username : client_pool.keySet()) {

            users += "&" + username;
        }

        if(users != "")
            users = users.substring(1);

        //把请求发给所有信道，信道自己筛选
        for (String chat : client_pool.keySet()) {

            //每个信道响应请求,包括:发送在线列表以及信息

            //通过请求返回响应，信道自动筛选
            client_pool.get(chat).ack(new ChatResponse(users, chatRequest));
        }
    }


    /**
     * 服务器关闭
     * 关闭所有信道
     */
    public void closeAll() throws IOException {

        for (String client : client_pool.keySet()) {

            this.closeByName(client);
        }
    }

    /**
     * 关闭指定用户信道
     */
    public void closeByName(String name) throws IOException {

        client_pool.get(name).close();
        client_pool.remove(name);
    }

    /**
     * 用户下线通知
     *
     * @return 返回是否有用户下线
     */
    public String tapeOutLog() {

        if (this.client_close_signal) {

            return this.tapeout_name + "已下线";
        }

        return null;
    }

}
