package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 用户登录校验方法
 */
public class ValidateUsrInfo {

    public static boolean validateUsrInfo(Socket socket) {
        // 获取用户信息列表
        HashMap<String, String> usrListMap = MyServer.getUsrListMap();

        try {
            // 输入流，用于接受客户端传送过来的用户名及密码
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            // 输出流，用于向客户端发送校验结果
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String usrName = "";
            String pasword = "";
            boolean invalid = true;
            while (invalid) {   // 若非法则一直提示客户端重新输入然后重新校验
                // 读取用户名及密码
                usrName = dis.readUTF();
                pasword = dis.readUTF();
                if (usrListMap.containsKey(usrName) && pasword.equals(usrListMap.get(usrName))) {   // 判断用户名及密码是否合法
                    // 禁止同一个用户重复登录
                    boolean isAlreadyOnline = false;
                    ArrayList<MyServerHandler> onlineUsrList = MyServer.getOnlineUsrList();
                    for (MyServerHandler x : onlineUsrList) {
                        if (usrName.equals(x.getName())) {
                            isAlreadyOnline = true;
                            break;
                        }
                    }
                    if (!isAlreadyOnline) {
                        // 校验合法，用户登录成功，向输出流中写 true
                        dos.writeBoolean(true);
                        dos.flush();
                        invalid = false;
                    } else {
                        // 校验合法但是属于重复登录的情况，登录失败需要写日志
                        MyServer.writeLog(null, 2, socket);
                        // 向输出流中写 false，并发送
                        dos.writeBoolean(false);
                        dos.flush();
                    }
                } else {
                    // 用户名与密码未通过校验，登录失败需要写日志
                    MyServer.writeLog(null, 2, socket);
                    // 向输出流中写 false，并发送
                    dos.writeBoolean(false);
                    dos.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
