import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 客户端线程类，用于处理客户端的请求
 */
public class ClientThread extends Thread {
    // 客户端的Socket连接
    private final Socket socket;
    // 用户列表
    private final List<User> users;
    // 根目录路径
    private final String rootDir;

    /**
     构造函数，初始化相关属性
     @param socket 客户端的Socket连接
     @param users 用户列表
     @param rootDir 根目录路径
     */
    public ClientThread(Socket socket, List<User> users, String rootDir) {
        this.socket = socket;
        this.users = users;
        this.rootDir = rootDir;
    }

    /**
     程序的入口点，执行客户端请求处理逻辑
     */
    @Override
    public void run() {
        try (
                // 获取输入输出流
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            //获取用户的IP地址
            String ip = socket.getInetAddress().getHostAddress();
            //在服务器端输出提示信息
            System.out.println("Client " + ip + " connected.");
            //创建日志
            Logger log = new Logger(System.getProperty("user.dir") + "\\src");
            // 用户登录验证过程
            Authenticator authenticator = new Authenticator(users, in, out, ip, log, socket);
            User user = authenticator.run();
            if (user == null) {
                return;
            }
            // 进入命令提示符状态
            Path currPath = Paths.get(rootDir);
            out.write(user.getUsername() + "@" + socket.getInetAddress().getHostAddress() + ":" + currPath + "$ \n");
            out.flush();

            // 读取并处理用户输入的命令
            CommandProcessor processor = new CommandProcessor(in, out, currPath, ip, log, user, rootDir, socket);
            processor.process();
            // 关闭连接
            socket.close();
            //在服务器端输出提示信息
            System.out.println("Client " + ip + " disconnected.");
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
    }
}