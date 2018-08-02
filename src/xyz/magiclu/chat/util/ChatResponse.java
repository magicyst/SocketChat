package xyz.magiclu.chat.util;

/**
 * Created by Administrator on 2018/7/30.
 * 客户端传递的是response对象
 * 信道传递的是response字符串
 * 对象和字符串的转化
 * 响应bean
 * [user1&user2..]#[master]:[message]
 */
public class ChatResponse {

    private String signal;      //响应特殊信号(!error#用户名已存在)
    private String signal_str;  //特殊信号描述全文
    private String users;       //用户在线列表 user1&user2&...
    private String master;      //发信人       master
    private String domain;      //响应的目标   domain
    private String message;     //信息         message
    private String context;     //响应全文     master:message

    /**
     * 组成响应
     * 把多个零散的信息组合成一条响应字符串。发个客户端解析
     * @param userlist  用户列表字符串
     * @param master    发信人
     * @param message       信息
     */
    public ChatResponse(String userlist, String master, String domain, String message) {

        this.users = userlist;
        this.domain = domain;
        this.message = message;
        this.master = master;
    }

    /**
     * 注意是客户端提前响应用的
     * 把提取响应字符串中的信息
     * 形式：[user1&user2..]#[master]:[message]
     *      1、可能msg为空字符串
     *      2、可能只返回用户列表如：[user1&user2..]#
     * @param response 响应字符串
     */
    public ChatResponse(String response){

        if(response.charAt(0) == '!'){

            this.signal_str = response;
            String[] temp = response.split("#");
            this.signal = temp[0].substring(1);
            this.message = temp[1];

            System.out.println("signal:"+this.signal+"   "+this.master);

        }else {
            //通过#号把字符串分开
            String[] temp1 = response.split("#");

            //赋值用户列表
            this.users = response.split("#")[0];

            //如果没出现第二情况无响应信息  user1&user2..#  判断
            if (temp1.length > 1) {

                this.context = temp1[1];

                //分割context,获取master and message
                String[] temp2 = this.context.split(":");

                //temp2的
                if (temp2.length > 1) {

                    this.master = temp2[0];
                    this.message = temp2[1];
                }
            }
        }
    }

    /**
     * 处理器回复响应的构造器，每次响应需要返回在线在线用户列表
     *
     * @param users   在线用户列表
     * @param chatRequest 用户的发到服务器的信息，需要响应转发,如果request为null,作为刷新列表响应
     */
    public ChatResponse(String users, ChatRequest chatRequest){

        //初始化用户列表
        this.users = users;

        //如果请求不为空，则初始化信息
        if(chatRequest != null && chatRequest.getMessage() != null){
            this.master = chatRequest.getMaster();
            this.domain = chatRequest.getDomain();
            this.message = chatRequest.getMessage();
            this.context = master+":"+message;
        }

    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    /**
     * 发给客户端的响应
     * @return 返回响应字符串
     */
    @Override
    public String toString() {

        //如果含有特殊信号
        if(isSpecialResponse())
            return this.signal_str;

        //返回用户列表
        String reslut = this.users + "#";

        //如果请求全文不为空
        if (this.context != null)
            return reslut + this.context;

        return reslut;

    }

    /**
     *
     * @return 返回一个用户名字符串数组
     */
    public String[] getUserList(){

        if (this.users != null)
            return this.users.split("&");

        return null;
    }

    /**
     * 查看是否为特殊请求
     * @return 特殊请求返回true,否则返回false
     */
    public boolean isSpecialResponse(){

        if ("".equals(this.signal) || this.signal == null)
            return false;

        return true;
    }
}
