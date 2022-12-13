
package Server;

import Client.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 服务器端的客户端对应类
 */
public class MyServerHandler implements Runnable{
    /**
     * 与对应客户端的套接字
     */
    private Socket socket;
    /**
     * 输入流
     */
    private DataInputStream dis;
    /**
     * 输出流
     */
    private DataOutputStream dos;
    /**
     * 是否正在运行标志
     */
    private boolean isRunning;
    /**
     * 连接的客户端对应的用户名
     */
    private String name;

    /**
     * 默认构造函数
     */
    public MyServerHandler () { }

    /**
     * 实际使用的构造函数
     * @param socket 客户端与服务器端连接使用的套接字
     */
    public MyServerHandler (Socket socket) {
        this.socket = socket;
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.isRunning = true;
            this.name = receive();
            this.send("Welcome to the Chatroom");
            this.sendMessage(name + " enter the chatroom", true);
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
    }

    public String getName() {
        return this.name;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * 从输入流中获取数据
     * @return 输入流中的数据，从客户端发送过来
     */
    public String receive() {
        String msg = "";
        try {
            msg = this.dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
        return msg;
    }

    /**
     * 往输出流中写入数据
     * @param msg 需要写入的数据
     */
    public void send(String msg) {
        try {
            this.dos.writeUTF(msg);
            this.dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            release();
        }
    }

    /**
     * 向客户端发送在线用户列表
     * @param msg 序列化的在线用户列表
     */
    public void sendOnlineUsrList(String msg) {
        ArrayList<MyServerHandler> onlineUsrList = MyServer.getOnlineUsrList();
        StringBuilder sb = new StringBuilder();
        for (MyServerHandler x :
                onlineUsrList) {
            sb.append(x.getName());
            sb.append("\n");
        }
        sb.append("------------------------------");

        String name = getNameInCommand(msg);
        for (MyServerHandler x :
                onlineUsrList) {
            if (name.equals(x.getName())) {
                x.send(sb.toString());
            }
        }
    }

    /**
     * 服务器端的发送信息方法
     * @param msg 需要发送的信息
     * @param isSys 判断信息是否是系统命令
     */
    public void sendMessage(String msg, boolean isSys) {
        // 判断是否是私聊
        boolean isPrivate = msg.startsWith("@");
        if (isPrivate) {    // 私聊，格式：@UserName:XXXXXX
            int index = msg.indexOf(":");
            if (index != -1) {
                String targetName = msg.substring(1, index);
                msg = msg.substring(index + 1);
                ArrayList<MyServerHandler> onlineUsrList = MyServer.getOnlineUsrList();
                for (MyServerHandler other : onlineUsrList) {
                    if (other.name.equals(targetName)) {
                        // 判断是否需要匿名发送
                        if (MyServer.getUsrIsAnonymousMap().containsKey(name) && MyServer.getUsrIsAnonymousMap().get(name)) {
                            other.send("Somebody talk to you in private: " + msg);
                        } else {
                            other.send(name + " talk to you in private: " + msg);
                        }
                    }
                }
            }
        } else {
            ArrayList<MyServerHandler> onlineUsrList = MyServer.getOnlineUsrList();
            for (MyServerHandler other : onlineUsrList) {
                if (other == null) {
                    continue;
                } else if (!isSys) {    // 给客户端发信息,用于执行指令
                    // 判断是否是匿名信息
                    if (MyServer.getUsrIsAnonymousMap().containsKey(name) && MyServer.getUsrIsAnonymousMap().get(name)) {
                        other.send("Somebody: " + msg);
                    } else {
                        other.send(name + ": " + msg);
                    }
                } else {
                    other.send(msg);
                }
            }
        }
    }

    /**
     * 处理用户退出聊天室方法
     * @param msg 用户退出聊天室的命令
     */
    private void dealSomebodyQuit(String msg) {
        String name = getNameInCommand(msg);
        for (MyServerHandler x:
             MyServer.getOnlineUsrList()) {
            if (name.equals(x.getName())) {
                MyServer.usrLogOut(x);
            }
        }
    }

    /**
     * 改变某用户的匿名状态
     * @param msg 某用户改变自己匿名状态的命令
     */
    private void changeAnonymous(String msg) {
        String name = getNameInCommand(msg);
        HashMap<String, Boolean> map = MyServer.getUsrIsAnonymousMap();
        if (map.containsKey(name)) {
            boolean tmp = map.get(name);
            map.put(name, !tmp);
        }
    }

    /**
     * 从用户的命令中获取用户名
     * @param msg 服务器端接收到的信息
     * @return 发出命令的用户的用户名
     */
    private String getNameInCommand(String msg) {
        int index = msg.indexOf("\t");
        return msg.substring(index+1);
    }

    /**
     * 释放链接
     */
    private void release() {
        isRunning = false;
        Utils.close(this.dos, this.dis, this.socket);
        ArrayList<MyServerHandler> onlineUsrList = MyServer.getOnlineUsrList();
        onlineUsrList.remove(this);
        sendMessage(name + " leaved", true);
    }

    @Override
    public void run() {
        while (isRunning) {
            String msg = receive();
            // 判断信息是聊天信息，申请在线用户列表命令，退出命令还是更改匿名状态命令
            if (!msg.equals("")) {
                if (msg.startsWith("@@list")) {
                    sendOnlineUsrList(msg);
                } else if (msg.startsWith("@@quit")){
                    dealSomebodyQuit(msg);
                } else if (msg.startsWith("@@isAnonymous")) {
                    changeAnonymous(msg);
                } else {
                    sendMessage(msg, false);
                }
            }
        }
    }
}
