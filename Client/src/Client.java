import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 客户端类，用于与服务器建立连接并进行交互。
 */
public class Client {
    // 服务器的IP地址和端口号
    private final String host;
    private final int port;
    // 客户端的资源目录设置为当前目录下的ClientSrc目录
    private final String rootDir = System.getProperty("user.dir") + File.separator + "ClientSrc";

    /**
     * 构造函数，用于初始化客户端类的对象。
     * @param host 服务器的IP地址
     * @param port 服务器监听的端口号
     */
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 启动客户端，与服务器建立连接并进行交互。
     */
    public void start() {
        try (
                // 与服务器建立连接
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // 用户登录验证过程
            Authenticator authenticator = new Authenticator(in, out, consoleIn);
            authenticator.authenticate();

            // 进入命令提示符状态
            CommandPrompt commandPrompt = new CommandPrompt(in, out, consoleIn, rootDir);
            commandPrompt.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端的主函数，用于创建客户端类的对象，并启动客户端。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        //输入服务器的IP地址
        System.out.print("Please input server IP address: ");
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
        String host = null;
        try {
            host = consoleIn.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Client client = new Client(host, 8888);
        client.start();
    }
}