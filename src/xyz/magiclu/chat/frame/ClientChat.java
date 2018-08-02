package xyz.magiclu.chat.frame;

import xyz.magiclu.chat.client.Client;
import xyz.magiclu.chat.client.TCPClient;
import xyz.magiclu.chat.server.ChatServerFrame;
import xyz.magiclu.chat.util.ChatRequest;
import xyz.magiclu.chat.util.ChatResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

/**
 * Created by Administrator on 2018/7/26.
 */
public class ClientChat{

    private Client client;
    private Thread refresh = new RefreshTimer(200);
    private static int count = 0;   //客户计数器
    private String username;        //用户名
    private String ip;              //ip
    private int port;               //端口

    private JFrame login_jf;        //登录界面
    private JTextField username_ipt;//用户名
    private JTextField ip_ipt;      //链接ip
    private JTextField port_ipt;     //链接端口
    private JButton login_jb;       //登录按钮
    private JButton reset;          //重置

    private JFrame chat_jf;         //聊天界面
    private JButton send_jb = new JButton("发送");        //发送按钮
    private JTextArea ipt_area;     //输入域
    private JTextArea show_area;    //信息展示域
    JComboBox userlist;             //用户在线列表

    /**
     *
     * @param ip
     * @param port
     * @throws IOException
     */
    public ClientChat(String ip, int port) throws IOException {

        initLogin();
        initButton();
        login_jf.setVisible(true);
        /*synchronized (ClientChat.class) {
            this.setName("chat-" + count);
            count++;
            sk = new Socket(ip, port);
            initChat();

            initButton();
            chat_jf.setVisible(true);
        }*/

    }

    public ClientChat() throws IOException {

        //初始化登录窗口并显示
        initLogin();
        login_jf.setVisible(true);

        //初始化按钮事件
        initButton();

    }

    private void initButton() {

        //发送信息
        send_jb.addActionListener(e -> {

            //获取发送信息的内容
            String msg = ipt_area.getText();

            //获取的信息发送的目标
            String domain = this.userlist.getSelectedItem().toString();

            //把所有人换成英文符合
            if("所有人".equals(domain))
                domain = "all";

            //如果内容不为空
            if (!"".equals(msg)) {

                try {

                    //发送消息
                    client.send(new ChatRequest(this.username,domain,msg).toString());

                    //清空缓冲区
                    client.flush();

                    //如果发消息异常，输入域置空
                    ipt_area.setText("");

                } catch (SocketException e1){

                    //服务器已经关闭
                    int flag = JOptionPane.showConfirmDialog(null,"服务器已关闭,是否关闭客户端?","系统提示",JOptionPane.YES_NO_OPTION);

                    if(flag == 0){
                        chat_jf.dispose();
                    }
                } catch (IOException e1) {
                    //网络异常
                    e1.printStackTrace();
                }


            }

        });

        //重置
        this.reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //重置
                ClientChat.this.username_ipt.setText("");
                ClientChat.this.ip_ipt.setText("");
                ClientChat.this.port_ipt.setText("");

            }
        });

        //登录
        this.login_jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //为ip,端口，用户名赋值
                ClientChat.this.ip = ClientChat.this.ip_ipt.getText();
                ClientChat.this.port = Integer.valueOf(ClientChat.this.port_ipt.getText());
                ClientChat.this.username = ClientChat.this.username_ipt.getText();

                try {
                    //新建通道，连接服务器
                    ClientChat.this.client = new TCPClient(ip,port);

                    //获取信道发给服务器
                    client.send(username);

                    //等待服务器允许
                    byte[] bytes = client.receive();
                    ChatResponse response = new ChatResponse(new String(bytes));
                    if(response.isSpecialResponse()){
                        if(response.getSignal().equals("error"))
                            JOptionPane.showMessageDialog(null, response.getMessage(), "登录异常", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //开启守护线程,200毫秒刷新一次(刷新用户在线列表，以及聊天信息域)
                    ClientChat.this.refresh.start();

                    //销毁login窗口
                    login_jf.dispose();

                    //然后进入聊天界面
                    initChat();
                    chat_jf.setVisible(true);

                } catch (ConnectException e1){

                    JOptionPane.showMessageDialog(null, "服务器灭未开启", "系统提示", JOptionPane.WARNING_MESSAGE);

                } catch (IOException e1) {

                    //连接服务器异常
                    e1.printStackTrace();
                }

            }
        });

    }

    /**
     * 刷新聊天文本框
     */
    private void receive() {

        try {

            //打开信道接受服务器响应
            byte[] bytes;

            if((bytes = client.receive()) != null){

                //响应字符串
                //String res = new String(bytes, 0, len);
                String res = new String(bytes);

                //用井号"#"分割用户列表
                System.out.println("client:  "+res);

                //封装响应
                ChatResponse response = new ChatResponse(res);

                //刷新在线用户列表
                refreshUserList(response.getUserList());

                //响应全文
                String context = response.getContext();

                //如果响应全文不为空，则更新聊天内容
                if (context != null)
                    show_area.append(context+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷新用户列表
     */
    private void refreshUserList(String[] users){

        this.userlist.removeAllItems();

        this.userlist.addItem("所有人");
        for(String user : users){
            this.userlist.addItem(user);
        }
    }

    /**
     * 登录界面
     */
    private void initLogin(){

        this.login_jf = new JFrame("登录");
        login_jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        login_jf.setLocationRelativeTo(null);
        login_jf.setSize(450, 370);

        //第0个JPanel,页面提示
        JPanel panel0 = new JPanel();
        panel0.add(new JLabel("局域网聊天登录"));

        // 第 1 个 JPanel, 使用默认的浮动布局
        JPanel panel1 = new JPanel();
        panel1.add(new JLabel("用户名"));
        username_ipt = new JTextField(11);
        panel1.add(username_ipt);

        // 第 2 个 JPanel, 使用默认的浮动布局
        JPanel panel2 = new JPanel();
        panel2.add(new JLabel("链接地址"));
        ip_ipt = new JTextField(11);
        ip_ipt.setText(ChatServerFrame.ip);
        panel2.add(ip_ipt);

        JPanel panel4 = new JPanel();
        panel4.add(new JLabel("链接端口"));
        port_ipt = new JTextField(5);
        port_ipt.setText(String.valueOf(ChatServerFrame.port));
        panel4.add(port_ipt);

        // 第 3 个 JPanel, 使用浮动布局, 并且容器内组件居中显示
        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.login_jb = new JButton("登录");
        this.reset = new JButton("重置");
        panel3.add(this.login_jb);
        panel3.add(this.reset);

        // 创建一个垂直盒子容器, 把上面 3 个 JPanel 串起来作为内容面板添加到窗口
        Box vBox = Box.createVerticalBox();
        vBox.add(panel0);
        vBox.add(panel1);
        vBox.add(panel2);
        vBox.add(panel4);
        vBox.add(panel3);

        this.login_jf.setContentPane(vBox);

        this.login_jf.pack();//紧缩一起
    }

    /**
     * 聊天界面
     */
    private void initChat() {


        chat_jf = new JFrame(this.username);

        chat_jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        chat_jf.setLocationRelativeTo(null);
        chat_jf.setSize(450, 370);
        chat_jf.setBackground(Color.black);
        chat_jf.getContentPane().setBackground(Color.black);

        JPanel show_jp = new JPanel();

        show_area = new JTextArea(15, 35);
        JScrollPane show_jsp = new JScrollPane(show_area);

        //设置矩形大小.参数依次为(矩形左上角横坐标x,矩形左上角纵坐标y，矩形长度，矩形宽度)
        show_jsp.setBounds(13, 10, 350, 340);

        //默认的设置是超过文本框才会显示滚动条，以下设置让滚动条一直显示
        show_jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //show_area.setBackground(new Color(100,100,100));
        show_jp.setBackground(new Color(166, 162, 166));
        show_jp.add(show_jsp);

        JPanel user_jp = new JPanel();
        user_jp.setBackground(new Color(166, 162, 166));
        userlist = new JComboBox();
        userlist.addItem("所有人");
        user_jp.add(new JLabel("选择聊天用户"));
        user_jp.add(userlist);


        JPanel ipt_jp = new JPanel();
        ipt_jp.setBackground(new Color(166, 162, 166));

        ipt_area = new JTextArea(3, 28);
        JScrollPane ipt_jsp = new JScrollPane(ipt_area);
        ipt_jsp.setBounds(13, 10, 350, 340);
        ipt_jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        ipt_jp.add(ipt_jsp);
        ipt_jp.add(send_jb);


        Box vbox = Box.createVerticalBox();

        vbox.add(user_jp);
        vbox.add(show_jp);
        vbox.add(ipt_jp);

        chat_jf.add(vbox);

        chat_jf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                int flag = JOptionPane.showConfirmDialog(null,"是否确定关闭客户端？","系统提示",JOptionPane.YES_NO_OPTION);

                if(flag == 0) {

                    //客户端关闭,需要给服务器一个下线信号!close#为下线信号
                    try {

                        //关闭刷新线程
                        refresh.interrupt();

                        //发送服务器一个下线信号
                        client.send("!close#"+ClientChat.this.username);

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * 刷新在线用户列表和消息域的守护线程
     */
    private class RefreshTimer extends Thread{

        private long timeout;  //每个timeout毫秒刷新一次

        /**
         * 设置刷新时间，以及设置守护线程
         * @param timeout 刷新时间
         */
        public RefreshTimer(long timeout){

            this.timeout = timeout;
            this.setDaemon(true);
        }

        /**
         * 刷新任务
         */
        public void run(){

            while (!this.isInterrupted()){
                try {

                    System.out.println("time");
                    //睡眠后刷新
                    Thread.sleep(timeout);

                    //刷新
                    ClientChat.this.receive();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
