package xyz.magiclu.chat.util;

/**
 * 请求字符串类bean
 * 服务器传递的是请求对象
 * 信道传递的是请求字符串
 * 封装这请求字符串和请求对象的转化操作
 * 形式:[master]&[domain]&[message]
 * Created by Administrator on 2018/7/26.
 */
public class ChatRequest {

    private String signal;      //信号(下线通知服务器等) 规定如果有该信号，则必须放在请求开头,如(!close#)
    private String master;      //发信人
    private String domain;      //发送的人
    private String message;     //具体的信息

    public ChatRequest(){
        this("admin","all","hello");
    }

    /**
     * 发请求，转化为字符串
     * @param master   发信人
     * @param domain   发送的人
     * @param message  具体的信息
     */
    public ChatRequest(String master, String domain, String message) {
        this.domain = domain;
        this.message = message;
        this.master = master;
    }

    /**
     * 信道接受字符串转化为请求对象，服务器传递的是请求对象
     * 模式为:master&domain&message
     * @param request 获取响应字符串
     */
    public ChatRequest(String request){

        //解析特殊信号
        if(request.charAt(0) == '!'){

            String[] temp = request.split("#");
            this.signal = temp[0].substring(1);
            this.master = temp[1];

            System.out.println("signal:"+this.signal+"   "+this.master);

        }else {

            //解析客户端信息
            String[] context = request.split("&");

            if (context.length >= 3) {
                //赋值属性
                this.master = context[0];
                this.domain = context[1];
                this.message = context[2];
            }
        }
    }

    /*
    seter and geter
     */
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    /**
     * 返回请求字符串
     * @return master&domain&message
     */
    @Override
    public String toString() {
        return this.master+
                "&"+this.domain+
                "&"+this.message;
    }

    /**
     * 查看是否为特殊请求
     * @return 特殊请求返回true,否则返回false
     */
    public boolean isSpecialRequest(){

        if ("".equals(this.signal) || this.signal == null)
            return false;

        return true;
    }


}
