package Client;

import java.io.*;
import java.net.Socket;

/**
 * 用户登录检验工具
 */
public class InputUsrInfoUtil {
    /**
     * 用户名
     */
    private String name;
    /**
     * 套接字
     */
    private final Socket socket;

    public InputUsrInfoUtil (Socket socket) {
        this.socket = socket;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 用户登录检验方法
     * @return 用户是否登录成功
     */
    public boolean inputUsrInfo () {
        try {
            // 与服务器进行通信的输入流
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            // 与服务器进行通信的输出流
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            // 读取键盘输入的输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                // 提示用户输入账号，从键盘读取输入然后将输入写到输出流中发送到服务器端
                System.out.println("请输入用户名:");
                String name = br.readLine();
                dos.writeUTF(name);
                dos.flush();
                // 提示用户输入密码，从键盘读取输入然后将输入写到输出流中发送到服务器端
                System.out.println("请输入密码:");
                String password = br.readLine();
                dos.writeUTF(password);
                dos.flush();
                // 服务器在接收到账号密码后会进行校验然后返回结果，等待结果
                if (dis.readBoolean()) {
                    // 账号密码通过验证，记录用户名并且登录成功
                    this.name = name;
                    return true;
                } else {
                    // 否则提示用户错误
                    System.out.println("用户名与密码有误！请检查后重新输入");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
