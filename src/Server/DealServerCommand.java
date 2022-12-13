package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 负责读取并处理服务器端命令的线程
 */
public class DealServerCommand implements Runnable{

    public DealServerCommand() {
    }

    @Override
    public void run() {
        System.out.println("------------Server------------");
        System.out.println("Waiting for command:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String cmd = br.readLine();
                switch (cmd) {
                    case "list" :
                        System.out.println("当前在线用户有：");
                        ArrayList<MyServerHandler> onlineUsrList = MyServer.getOnlineUsrList();
                        for (MyServerHandler x :
                                onlineUsrList) {
                            System.out.println(x.getName());
                        }
                        System.out.println("------------------------------");
                        break;
                    case "listall":
                        System.out.println("所有用户有：");
                        HashMap<String, String> usrListMap = MyServer.getUsrListMap();
                        for (String usrName : usrListMap.keySet()) {
                            System.out.println(usrName);
                        }
                        System.out.println("------------------------------");
                        break;
                    case "quit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("命令输入有误，请检查后重新输入");
                        System.out.println("------------------------------");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
