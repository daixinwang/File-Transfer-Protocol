import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

/**
 * 服务器类，用于启动服务器并监听客户端连接请求
 */

public class Server {
    private static final int DEFAULT_PORT = 8888; // 默认端口号
    private static final int MAX_CLIENTS = 5; // 最大客户端连接数

    private final List<User> users; // 用户列表
    private final String rootDir; // 根目录路径

    /**
     构造函数，从配置文件中读取用户列表和根目录路径，初始化相关属性
     @param configPath 配置文件的路径
     @throws IOException 如果读取配置文件失败，则抛出该异常
     */
    public Server(String configPath) throws IOException {
        // 读取配置文件，初始化用户列表和根目录路径
        ConfigReader reader = new ConfigReader(configPath);
        this.users = reader.getUsers();
        this.rootDir = reader.getRootDir();
    }

    /**
     启动服务器并监听客户端连接请求
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("Server started on port " + DEFAULT_PORT);

            while (true) {
                // 循环等待客户端连接请求，最多支持MAX_CLIENTS个客户端同时连接
                if (Thread.activeCount() - 1 < MAX_CLIENTS) { // activeCount是包含主线程在内的线程数，因此要减去1
                    ClientThread clientThread = new ClientThread(serverSocket.accept(), users, rootDir);
                    clientThread.start();
                } else {
                    System.out.println("Maximum number of clients reached");
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    /**
     服务器的入口方法，创建服务器对象并启动FTP服务器
     @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            Server server = new Server("src/config.txt");
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
