import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * 用户登录验证类,用于验证用户登录的用户名和密码
 */
public class Authenticator {
    // 用户列表
    private final List<User> users;
    // 输入输出流
    private final BufferedReader in;
    private final BufferedWriter out;
    // 用户IP地址
    private final String ip;
    // 日志记录器
    private final Logger log;
    // socket
    private final Socket socket;

    /**
     构造函数，初始化用户认证器的相关属性
     @param users 当前系统中已注册的用户列表
     @param in 客户端输入流
     @param out 客户端输出流
     @param ip 客户端的IP地址
     @param log 日志记录器
     @param socket 客户端连接的Socket对象 */
    public Authenticator(List<User> users, BufferedReader in, BufferedWriter out, String ip, Logger log, Socket socket) {
        this.users = users;
        this.in = in;
        this.out = out;
        this.ip = ip;
        this.log = log;
        this.socket = socket;
    }

    /**
     用户登录验证过程
     @return 登录成功的用户对象
     @throws IOException IO异常
     */
    public User run() throws IOException {
        User user = null;
        // 尝试次数
        int tries = 0;
        while (user == null) {
            String username = in.readLine().trim();

            // 匿名用户
            if ("anonymous".equals(username)) {
                user = new User("anonymous", "");
                out.write("Login successful, welcome anonymous!\n");
                out.flush();
                //日志记录匿名用户登陆成功
                log.log(ip, "Anonymous user login successful.\n");
                //在服务器端输出提示信息
                System.out.println("Anonymous user login successful.\n");
                break;
            }

            String password = in.readLine().trim();
            // 设置变量判断这次登录是否为重复登录
            boolean isRepeat = false;

            // 验证用户名和密码
            for (User u : users) {
                if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                    // 假如用户已经登录，则提示用户已经登录
                    if (u.isLoggedIn()) {
                        out.write("User already logged in.\n");
                        out.flush();
                        isRepeat = true;
                        //日志记录用户重复登录
                        log.log(ip, username + " already logged in.\n");
                        break;
                    }
                    user = u;
                    u.setLoggedIn(true);
                    out.write("Login successful, welcome " + username + "!\n");
                    out.flush();
                    //日志记录用户登录成功
                    log.log(ip, username + " login successful.\n");
                    //在服务器端输出提示信息
                    System.out.println(username + " login successful.");
                    break;
                }
            }

            // 用户名或密码错误
            if (user == null && !isRepeat) {
                out.write("Invalid username or password.\n");
                out.flush();
                //日志记录用户登录失败
                log.log(ip, "Invalid username or password.\n");
                //累计错误次数达到3次则断开连接
                tries++;
                if (tries >= 3) {
                    out.write("Too many attempts, closing connection.\n");
                    out.flush();
                    //日志记录用户登录失败次数过多
                    log.log(ip, "Too many attempts, closing connection.\n");
                    socket.close();
                    //在服务器端输出提示信息
                    System.out.println("Client " + ip + " disconnected.");
                    break;
                }
            }
        }
        return user;
    }
}