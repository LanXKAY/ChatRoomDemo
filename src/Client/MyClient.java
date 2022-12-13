/**
 * 客户端主线程
 */
package Client;

import java.io.IOException;
import java.net.Socket;

public class MyClient {
    public static void main(String[] args) throws IOException {
        System.out.println("------------Client------------");
        Socket socket = new Socket("localhost", 8888);

        // 用户登录，进行用户信息的校验
        InputUsrInfoUtil inputUsrInfoUtil = new InputUsrInfoUtil(socket);
        if (inputUsrInfoUtil.inputUsrInfo()) {
            // 登录成功，获取登录用户的用户名，然后启动发送与接收信息线程
            String name = inputUsrInfoUtil.getName();
            System.out.println("Welcome! " + name);
            new Thread(new Send(socket, name)).start();
            new Thread(new Receive(socket)).start();
        }
    }
}
