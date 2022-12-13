package Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * 读取用户文件初始化相关数据结构方法
 */
public class ReadUsrFile {
    public static void initUsrInfo(String usrFilePath, HashMap<String, String> usrListMap, HashMap<String, Boolean> usrIsAnonymousMap) {

        try (BufferedReader br = new BufferedReader(new FileReader(usrFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] account = line.split(" ");
                usrListMap.put(account[0], account[1]);
                usrIsAnonymousMap.put(account[0], false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
