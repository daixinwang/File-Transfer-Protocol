import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 用户身份验证类，用于验证用户的用户名和密码是否正确
 */
public class Authenticator {
    // 输入输出流
    private final BufferedReader in;
    private final BufferedWriter out;
    // 控制台输入流
    private final BufferedReader consoleIn;

    /**
     构造函数
     @param in 输入流，从服务器接收数据
     @param out 输出流，向服务器发送数据
     @param consoleIn 控制台输入流，用于获取用户输入的用户名和密码
     */
    public Authenticator(BufferedReader in, BufferedWriter out, BufferedReader consoleIn) {
        this.in = in;
        this.out = out;
        this.consoleIn = consoleIn;
    }

    /**
     用户登录验证过程
     @throws IOException 如果在与服务器通信时出现错误，则抛出异常
     */
    public void authenticate() throws IOException {
        // 尝试次数
        int tries = 0;
        while (true) {
            System.out.print("Username: ");
            String username = consoleIn.readLine().trim();

            out.write(username + "\n");
            out.flush();

            if ("anonymous".equals(username)) {
                System.out.println(in.readLine());
                break;
            }

            System.out.print("Password: ");
            String password = consoleIn.readLine().trim();

            out.write(password + "\n");
            out.flush();

            tries++;
            String response = in.readLine();
            if (!response.startsWith("Login successful") && tries >= 3) {
                System.out.println(response);
                response = in.readLine();
                System.out.print(response);
                System.exit(0);
            }
            System.out.println(response);


            if (response.startsWith("Login successful")) {
                break;
            }
        }
    }
}
