import java.io.*;

/**
 命令提示符类，用于处理用户的命令请求并与服务器进行交互
 */
public class CommandPrompt {
    // 输入输出流
    private final BufferedReader in;
    private final BufferedWriter out;
    // 控制台输入流
    private final BufferedReader consoleIn;
    // 根目录
    private final String rootDir;

    /**
     构造函数
     @param in 输入流，从服务器接收数据
     @param out 输出流，向服务器发送数据
     @param consoleIn 控制台输入流，用于获取用户输入的命令
     @param rootDir 客户端资源文件夹的路径
     */
    public CommandPrompt(BufferedReader in, BufferedWriter out, BufferedReader consoleIn, String rootDir) {
        this.in = in;
        this.out = out;
        this.consoleIn = consoleIn;
        this.rootDir = rootDir;
    }

    /**
     处理dir或cd命令
     @throws IOException 如果在与服务器通信时出现错误，则抛出异常
     */
    public void dirOrCd() throws IOException {
        String response = in.readLine();
        // 读取服务器的响应并输出，直到遇到某行以“$ "结尾
        while (!response.endsWith("$ ")) {
            System.out.println(response);
            response = in.readLine();
        }
        System.out.print(response);
    }

    /**
     处理get命令
     @param input 用户输入的get命令及相关参数
     @throws IOException 如果在与服务器通信时出现错误，则抛出异常
     */
    public void get(String input) throws IOException {
        String response = in.readLine();
        if (response.equals("Cannot download directory.")) {
            System.out.println(response);
            System.out.print(in.readLine());
            return;
        }
        response = in.readLine();
        if (response.startsWith("File not exists")) {
            System.out.println(response);
            System.out.print(in.readLine());
            return;
        }
        String[] inputs = input.split("\\s+");
        String filename = inputs[1];
        // 假如客户端已经存在同名文件，询问是否覆盖
        File file = new File(rootDir + File.separator + filename);
        if (file.exists()) {
            out.write("File already exists.\n");
            out.flush();
            System.out.print("File already exists. Do you want to overwrite it? (Y/N) ");
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            String answer = consoleIn.readLine();
            //向服务器发送用户的选择
            out.write(answer + "\n");
            out.flush();
            //假如回应是“N”或者”n"，则不下载文件
            if (answer.equals("N") || answer.equals("n")) {
                System.out.print(in.readLine());
                return;
            }
        } else {
            out.write("File not exists.\n");
            out.flush();
        }
        try (BufferedWriter fileOut = new BufferedWriter(new FileWriter(file))) {
            String line = in.readLine();
            while (!line.equals("EOF")) {
                fileOut.write(line + "\n");
                fileOut.flush();
                line = in.readLine();
            }
        }
        System.out.println("File downloaded.");
        System.out.print(in.readLine());
    }

    /**
     处理put命令
     @param input 用户输入的put命令及相关参数
     @throws IOException 如果在与服务器通信时出现错误，则抛出异常
     */
    public void put(String input) throws IOException {
        String response = in.readLine();
        if (response.startsWith("Access denied.")) {
            System.out.println(response);
            System.out.print(in.readLine());
            return;
        }
        String[] inputs = input.split("\\s+");
        String filename = inputs[1];
        // 假如是目录，提示用户不能上传目录
        File file = new File(rootDir + File.separator + filename);
        if (file.isDirectory()) {
            out.write("Cannot upload directory.\n");
            out.flush();
            System.out.print(in.readLine());
            return;
        } else {
            out.write("OK.\n");
            out.flush();
        }
        // 假如客户端的资源文件夹内不存在同名文件，提示用户文件不存在
        if (!file.exists()) {
            out.write("File not exists.\n");
            out.flush();
            System.out.println("File not exists.");
            System.out.print(in.readLine());
            return;
        } else {
            out.write("File exists.\n");
            out.flush();
        }
        //假如服务器上已经存在同名文件，询问用户是否覆盖
        response = in.readLine();
        if (response.startsWith("File exists.")) {
            System.out.print(response);
            String answer = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
            out.write(answer + "\n");
            out.flush();
            if (answer.equals("N")) {
                System.out.print(in.readLine());
                return;
            }
        }
        try (BufferedReader fileIn = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = fileIn.readLine()) != null) {
                out.write(line + "\n");
                out.flush();
            }
        }
        out.write("EOF\n");
        out.flush();
        System.out.println(in.readLine());
        System.out.print(in.readLine());
    }

    /**
     * 处理exit命令
     * @throws IOException 如果在与服务器通信时出现错误，则抛出异常
     */
    public void exit() throws IOException {
        String response = in.readLine();
        System.out.print(response);
    }

    /**
     * 命令提示符状态，处理用户的各种命令请求
     * @throws IOException 如果在与服务器通信时出现错误，则抛出异常
     */
    public void open() throws IOException {
        System.out.print(in.readLine());
        while (true) {
            String input = consoleIn.readLine().trim();

            out.write(input + "\n");
            out.flush();
            if (input.startsWith("dir")) {
                dirOrCd();
            } else if (input.startsWith("cd")) {
                dirOrCd();
            } else if (input.startsWith("get")) {
                get(input);
            } else if (input.startsWith("put")) {
                put(input);
            } else if (input.startsWith("exit")) {
                exit();
                break;
            } else {
                System.out.println(in.readLine());
                System.out.print(in.readLine());
            }
        }
    }
}