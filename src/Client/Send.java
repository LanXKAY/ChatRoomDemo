package Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 客户端处理发送信息的类
 */
public class Send implements Runnable{
    /**
     * 用户名
     */
    private final String name;
    /**
     * 套接字
     */
    private final Socket socket;
    /**
     * 是否正在运行标志
     */
    private boolean isRunning;
    /**
     * 是否匿名标志
     */
    private boolean isAnonymous;
    /**
     * 标准键盘输入流
     */
    private BufferedReader console;
    /**
     * 与服务器端进行通信的输出流
     */
    private DataOutputStream dos;

    /**
     * 构造函数，初始化各种变量
     * @param socket 与服务器连接的套接字
     * @param name 当前客户端用户的用户名
     */
    public Send (Socket socket, String name) {
        this.name = name;
        this.socket = socket;
        this.isRunning = true;
        this.isAnonymous = false;
        this.console = new BufferedReader(new InputStreamReader(System.in));
        try {
            this.dos = new DataOutputStream(socket.getOutputStream());
            send(name);
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
    }

    /**
     * 从键盘读取输入方法
     * @return 从键盘读取到的输入
     */
    private String read() {
        String msg = "";
        try {
            msg = this.console.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
        return msg;
    }

    /**
     * 往输出流写入数据的方法
     * @param msg 需要传送的数据
     */
    private void send(String msg) {
        try {
            // 往输出流写入数据并且刷新推送
            this.dos.writeUTF(msg);
            this.dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
    }

    /**
     * 释放链接方法
     */
    private void release() {
        this.isRunning = false;
        Utils.close(this.dos, this.socket);
    }

    /**
     * 处理命令方法
     * @param command 传入的命令
     */
    private void dealCommand(String command) {
        command = command.toLowerCase();
        switch (command) {
            case "list":    // 查看当前在线用户命令
                System.out.println("当前在线用户有：");
                // 往服务器端发送命令，服务器调用 sendOnlineUsrList 方法后返回序列化的在线用户列表，再进行打印
                send("@@list\t" + this.name);
                break;
            case "quit":    // 退出聊天室命令
                System.out.println("退出聊天室");
                System.out.println("------------------------------");
                // 往服务器端发送命令，服务器调用 dealSomebodyQuit 方法，进行相关的处理（关闭对应服务器端线程，写日志...）
                send("@@quit\t" + this.name);
                release();
                // 结束程序
                System.exit(0);
                break;
            case "showanonymous":   // 显示当前匿名状态命令
                System.out.println("当前 isAnonymous=" + this.isAnonymous);
                System.out.println("------------------------------");
                break;
            case "anonymous":   // 更改匿名状态命令
                this.isAnonymous = !this.isAnonymous;
                System.out.println("已成功切换匿名模式，当前 isAnonymous=" + this.isAnonymous);
                // 往服务器端发送命令，服务器调用 changeAnonymous 方法，更改服务器中的用户匿名状态信息
                send("@@isAnonymous\t" + this.name);
                System.out.println("------------------------------");
                break;
            default:    // 错误提示
                System.out.println("命令输入有误，请检查后重新输入");
                System.out.println("------------------------------");
        }
    }

    @Override
    public void run() {
        while (this.isRunning) {
            // 持续监听读取键盘的输入
            String msg = read();
            if (!msg.equals("")) {
                // 命令需要特殊处理，将实际的命令截取出来后调用处理命令方法
                if (msg.startsWith("@@")) {
                    String command = msg.substring(2);
                    dealCommand(command);
                } else {
                    // 否则直接写入到数据输出流中
                    send(msg);
                }
            }
        }
    }
}
