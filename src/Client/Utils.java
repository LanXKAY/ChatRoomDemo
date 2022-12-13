package Client;

import java.io.Closeable;

/**
 * 工具类
 */
public class Utils {

    /**
     * 用于关闭输入流，输出流或者套接字
     * @param targets 需要被关闭的输入流，输出流以及套接字
     */
    public static void close(Closeable... targets) {
        for (Closeable target : targets) {
            try {
                if (target != null) {
                    target.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
