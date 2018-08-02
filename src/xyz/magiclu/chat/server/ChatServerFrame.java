package xyz.magiclu.chat.server;

import xyz.magiclu.chat.frame.ClientChat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * Created by Administrator on 2018/7/26.
 */
public class ChatServerFrame extends Thread{

    private JFrame server_jf;                                //服务器界面
    private JTextArea server_log;                            //服务器日志
    private JButton start_jb = new JButton("启动服务器");    //开启服务器
    private JButton join_jb = new JButton("加入聊天");       //加入聊天
    private JTextField ip_jt;                               //服务器地址输入
    private JTextField port_jt;                             //服务器端口输入

    private boolean start = false;                          //是否开启服务器


    public static String ip = "localhost";                  //服务器地址
    public static int port = 10000;                         //服务器端口
    private ServerSocket server;
    private ExecutorService exeute = ExecutorService.getInstance(); //服务器处理内核

    public ChatServerFrame(){

        //初始化按钮
        initButton();

        //开启窗口界面,并显示
        initFrame();
        server_jf.setVisible(true);

    }

    /**
     * 初始化按钮事件
     */
    private void initButton() {

        //服务器的启动与关闭
        start_jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //如果服务器没有启动
                if(!ChatServerFrame.this.isStart()) {

                    try {
                        //创建服务器
                        ChatServerFrame.this.server =
                                new ServerSocket(ChatServerFrame.port);

                        //启动服务器线程
                        ChatServerFrame.this.start();

                        //设置服务器状态,和按钮状态
                        ChatServerFrame.this.setStart(true);
                        ChatServerFrame.this.start_jb.setText("关闭服务器");

                        //加入log
                        ChatServerFrame.this.server_log.append("服务器已启动.....\n");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }else {

                    //用户失误提示
                    int flag = JOptionPane.showConfirmDialog(null,"是否确定关闭服务器？","系统提示",JOptionPane.YES_NO_OPTION);

                    if(flag == 0) {
                        try {
                            //关闭服务器
                            ChatServerFrame.this.server.close();

                            //关闭信道
                            exeute.closeAll();

                            //否则，关闭服务器线程
                            ChatServerFrame.this.interrupt();

                            //设置服务器状态，和按钮状态
                            ChatServerFrame.this.setStart(false);
                            ChatServerFrame.this.start_jb.setText("开启服务器");

                            //加入log
                            ChatServerFrame.this.server_log.append("服务器已关闭.....\n");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

            }
        });

        //加入聊天室
        join_jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    new ClientChat();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * 初始化窗口
     */
    private void initFrame() {

        server_jf = new JFrame("局域网聊天");
        server_jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        server_jf.setLocationRelativeTo(null);
        server_jf.setSize(500, 500);

        //端口和ip输入
        JPanel jp1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jp1.add(new Label("ip"));
        ip_jt = new JTextField(11);
        ip_jt.setText(ChatServerFrame.ip);
        jp1.add(ip_jt);
        jp1.add(new Label("  "));
        jp1.add(new Label("port"));
        port_jt = new JTextField(5);
        port_jt.setText(String.valueOf(ChatServerFrame.port));
        jp1.add(port_jt);

        //服务器日志
        JPanel jp2 = new JPanel();
        server_log = new JTextArea(20,38);
        JScrollPane log_jsp = new JScrollPane(server_log);

        //设置矩形大小.参数依次为(矩形左上角横坐标x,矩形左上角纵坐标y，矩形长度，矩形宽度)
        log_jsp.setBounds(13, 10, 350, 340);

        //默认的设置是超过文本框才会显示滚动条，以下设置让滚动条一直显示
        log_jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jp2.add(log_jsp);

        //启动，和加入聊天
        JPanel jp3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jp3.add(start_jb);
        jp3.add(join_jb);

        Box vbox= Box.createVerticalBox();
        vbox.add(jp1);
        vbox.add(jp2);
        vbox.add(jp3);

        server_jf.add(vbox);
        server_jf.pack();

        server_jf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                if(ChatServerFrame.this.server != null) {
                    //用户失误操作
                    int flag = JOptionPane.showConfirmDialog(null, "是否确定关闭服务器?", "系统提示", JOptionPane.YES_NO_OPTION);

                    //确定关闭
                    if (flag == 0) {

                        try {

                            //否则，关闭服务器线程
                            ChatServerFrame.this.interrupt();

                            //关闭服务器
                            ChatServerFrame.this.server.close();

                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

    }

    @Override
    public void run() {

        try {

            while (!ChatServerFrame.this.isInterrupted()) {

                //等待客户端连入
                Socket client = ChatServerFrame.this.server.accept();

                String username = ChatServerFrame.this.exeute.join(client);

                ChatServerFrame.this.server_log.append("用户 : "+username+" 连入.....  "+"\n        信息为:"+client.toString()+"\n");


            }

        }catch (SocketException e){

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }
}

