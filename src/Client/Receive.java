package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 客户端的接收信息类
 */
public class Receive implements Runnable{
    /**
     * 与服务器端进行通信的输入流
     */
    private DataInputStream dis;
    /**
     * 套接字
     */
    private final Socket socket;
    /**
     * 是否正在运行标志
     */
    private boolean isRunning;

    /**
     * 构造函数，初始化各种变量
     * @param socket 与服务器连接的套接字
     */
    public Receive (Socket socket) {
        this.isRunning = true;
        this.socket = socket;
        try {
            this.dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
    }

    /**
     * 读取服务器端传送的信息并且打印出来
     */
    private void receive() {
        try {
            // 读取服务器端传送的信息
            String msg = this.dis.readUTF();
            System.out.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
    }

    /**
     * 释放输入流与套接字
     */
    private void release() {
        this.isRunning = false;
        Utils.close(this.dis, this.socket);
    }

    @Override
    public void run() {
        while (this.isRunning) {
            // 持续监听输入流接收信息
            receive();
        }
    }
}
