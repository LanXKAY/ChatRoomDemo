package Server;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 服务端监听线程
 */
public class MyServer {
    // 所有用户信息，用户名～密码
    private static HashMap<String, String> usrListMap = new HashMap<>();
    // 用户匿名状态列表，用于在转发信息时判断是否需要匿名
    private static HashMap<String, Boolean> usrIsAnonymousMap = new HashMap<>();
    // 在线用户列表
    private static final ArrayList<MyServerHandler> onlineUsrList = new ArrayList<>();
    // 日志路径
    private static String logPath;

    /**
     * 服务端主进程
     * @param args 不需要参数
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // 监听端口
        ServerSocket serverSocket = new ServerSocket(8888);

        // 获取项目所在的路径
        String baseDir = System.getProperty("user.dir");

        // 用户文件路径，txt格式，一行为一个账号，账号密码使用空格分隔
        String usrFilePath = baseDir + "/resources/usrInfo.txt";
        logPath = baseDir + "/resources/log.txt";
        // 读取用户信息文件，初始化用户信息哈希表以及用户匿名状态列表
        ReadUsrFile.initUsrInfo(usrFilePath, usrListMap, usrIsAnonymousMap);

        DealServerCommand dealServerCommand = new DealServerCommand();
        new Thread(dealServerCommand).start();

        // 监听等待客户端连接
        while (true) {
            waitForConnect(serverSocket);
        }
    }

    /**
     * 监听线程
     * @param serverSocket 服务器端监听端口
     * @throws IOException
     */
    private static void waitForConnect(ServerSocket serverSocket) throws IOException {
        // 监听等待客户端连接
        Socket socket = serverSocket.accept();

        if (ValidateUsrInfo.validateUsrInfo(socket)) {  // 判断账户密码合法性
            // 获取ip地址
            InetAddress ipAddress = socket.getInetAddress();
            MyServerHandler myServerHandler = new MyServerHandler(socket);
            usrLogIn(myServerHandler);
            System.out.println(ipAddress  + ": " + myServerHandler.getName() +"已连接到聊天室");
            // 启动服务器端处理某特定客户端的线程
            new Thread(myServerHandler).start();
        }
    }


    public static ArrayList<MyServerHandler> getOnlineUsrList() {
        return onlineUsrList;
    }

    public static HashMap<String, String> getUsrListMap() {
        return usrListMap;
    }

    public static HashMap<String, Boolean> getUsrIsAnonymousMap() {
        return usrIsAnonymousMap;
    }

    /**
     * 用户登录成功方法，更新在线用户列表以及写日志
     * @param myServerHandler 登录成功的用户对应的客户端线程
     */
    public static void usrLogIn (MyServerHandler myServerHandler) {
        onlineUsrList.add(myServerHandler);
        writeLog(myServerHandler, 1, myServerHandler.getSocket());
    }

    /**
     * 用户退出聊天室成功方法，更新在线用户列表以及写日志
     * @param myServerHandler 退出聊天室的用户对应的客户端线程
     */
    public static void usrLogOut (MyServerHandler myServerHandler) {
        onlineUsrList.remove(myServerHandler);
        writeLog(myServerHandler, 3, myServerHandler.getSocket());
    }

    /**
     * 写日志函数，记录用户登录与退出相关信息
     *
     * @param myServerHandler
     * @param commandFlag     需要记录的操作：取1表示登录成功，取2表示登录失败，取3表示用户下线
     * @param socket
     */
    public static void writeLog (MyServerHandler myServerHandler, int commandFlag, Socket socket) {
        // 记录函数调用是否合法
        boolean callValid = true;

        InetAddress address = socket.getInetAddress();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        sb.append(currentTime).append(" ").append(address).append(" ");

        switch (commandFlag) {
            case 1:
                // log in successfully
                String name1 = myServerHandler.getName();
                sb.append(name1).append(" log in successfully!");
                break;
            case 2:
                // log in failure
                sb.append("log in failure!");
                break;
            case 3:
                // log out
                String name2 = myServerHandler.getName();
                sb.append(name2).append(" log out successfully!");
                break;
            default:
                callValid = false;
                break;
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(logPath, true)))) {
            if (callValid) {
                bw.write(sb.toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}