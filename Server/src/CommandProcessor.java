import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.net.Socket;

/**
 命令处理器类，用于解析并处理用户输入的命令
 */
public class CommandProcessor {
    //输入输出流
    private final BufferedReader in;
    private final BufferedWriter out;
    //当前路径
    private Path currentPath;
    //用户ip
    private final String ip;
    //日志
    private final Logger log;
    //用户
    private final User user;
    //根目录
    private final String rootDir;
    //socket
    private final Socket socket;

    /**
     构造函数，初始化相关属性
     @param in 从客户端读取数据的缓冲字符输入流
     @param out 向客户端发送数据的缓冲字符输出流
     @param currentPath 当前工作目录的路径
     @param ip 客户端IP地址
     @param log 日志记录器
     @param user 当前登录的用户
     @param rootDir FTP服务器根目录的路径
     @param socket 连接到客户端的Socket
     */
    public CommandProcessor(BufferedReader in, BufferedWriter out, Path currentPath, String ip, Logger log, User user, String rootDir, Socket socket) {
        this.in = in;
        this.out = out;
        this.currentPath = currentPath;
        this.ip = ip;
        this.log = log;
        this.user = user;
        this.rootDir = rootDir;
        this.socket = socket;
    }

    /**
     解析命令
     @param input 用户输入的命令字符串
     @return 解析得到的Command对象
     */
    private Command parseCommand(String input) {
        String[] tokens = input.split("\\s+");
        String command = tokens[0];
        String arg = tokens.length > 1 ? tokens[1] : null;
        return new Command(command, arg);
    }

    /**
     处理dir命令，列出当前目录下的文件和文件夹
     @throws IOException 如果读取或写入数据时发生IO异常，则抛出该异常
     */
    private void dir() throws IOException {
        //列出当前目录下的文件和文件夹，用不同的颜色区分，一行显示6个
        File[] files = this.currentPath.toFile().listFiles();
        int count = 0;
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                out.write("\033[34m" + file.getName() + "\033[0m\t");
            } else {
                out.write(file.getName() + "\t");
            }
            count++;
            //假如count为6的倍数并且不是最后一个文件，换行
            if (count % 6 == 0 && count != files.length) {
                out.write("\n");
            }
        }
        out.write("\n");
        out.flush();
        //日志记录用户操作
        this.log.log(ip, user.getUsername() + " list files in " + this.currentPath + "\n");
    }

    /**
     处理cd命令，切换目录
     @param arg 目标目录名称
     @throws IOException 如果读取或写入数据时发生IO异常，则抛出该异常
     */
    private void cd(String arg) throws IOException {
        //切换目录
        Path newPath;
        //cd ..返回上一级目录
        if (Objects.equals(arg, "..")) {
            newPath = this.currentPath.getParent();
        } else {
            newPath = Paths.get(this.currentPath.toString(), arg);
        }

        //判断目录是否存在，是否是目录，是否在根目录下
        if (newPath == null || !newPath.startsWith(this.rootDir)) {
            out.write("Access denied.\n");
            out.flush();
        } else if (!Files.exists(newPath) || !Files.isDirectory(newPath)) {
            out.write("Directory not exists.\n");
            out.flush();
        } else {
            this.currentPath = newPath;
        }
        //日志记录用户操作
        log.log(ip, user.getUsername() + " change directory to " + this.currentPath + "\n");
    }

    /**
     * 处理put命令，上传文件到服务器
     * @param arg 上传文件的名称
     * @throws IOException 如果读取或写入数据时发生IO异常，则抛出该异常
     */
    private void put(String arg) throws IOException {
        //假如用户为匿名用户，提示用户无权限
        if (Objects.equals(user.getUsername(), "anonymous")) {
            out.write("Access denied.\n");
            out.flush();
            return;
        } else {
            out.write("OK\n");
            out.flush();
        }
        //假如客户端响应为目录，返回
        String response = in.readLine();
        if (Objects.equals(response, "Cannot upload directory.")) {
            return;
        }
        //假如客户端响应文件不存在，返回
        response = in.readLine();
        if (Objects.equals(response, "File not exists.")) {
            return;
        }
        //判断文件是否存在，假如存在提示用户是否覆盖
        Path filePath = this.currentPath.resolve(arg);
        if (Files.exists(filePath)) {
            out.write("File exists. Overwrite? (Y/N) \n");
            out.flush();
            response = in.readLine();
            if (Objects.equals(response, "N")) {
                return;
            }
        } else {
            out.write("File not exists.\n");
            out.flush();
        }
        //创建新文件或者覆盖原有的文件
        Files.write(filePath, new byte[0]);
        //接收文件，根据是否成功输出不同的信息
        try (BufferedWriter fileWriter = Files.newBufferedWriter(filePath)) {
            String line;
            boolean success = false;
            while ((line = in.readLine()) != null) {
                if (Objects.equals(line, "EOF")) {
                    out.write("File uploaded.\n");
                    out.flush();
                    success = true;
                    break;
                }
                fileWriter.write(line + "\n");
            }
            if (!success) {
                out.write("File upload failed.\n");
                out.flush();
            }
        }
        //日志记录用户操作
        log.log(ip, user.getUsername() + " upload file " + filePath + "\n");
    }

    /**
     * 处理get命令，下载服务器上的文件到客户端
     * @param arg 下载文件的名称
     * @throws IOException 如果读取或写入数据时发生IO异常，则抛出该异常
     */
    private void get(String arg) throws IOException {
        //判断是文件还是目录
        Path filePath = this.currentPath.resolve(arg);
        if (Files.isDirectory(filePath)) {
            // 提示用户不能下载目录
            out.write("Cannot download directory.\n");
            out.flush();
            return;
        } else {
            out.write("OK\n");
            out.flush();
        }
        //判断文件是否存在
        if (!Files.exists(filePath)) {
            out.write("File not exists.\n");
            out.flush();
            return;
        } else {
            out.write("File exists.\n");
            out.flush();
        }
        String response = in.readLine();
        if (response.startsWith("File already exists")) {
            String answer = in.readLine();
            //假如回应是N或者n，返回
            if (Objects.equals(answer, "N") || Objects.equals(answer, "n")) {
                return;
            }
        }
        //发送文件
        try (BufferedReader fileReader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                out.write(line + "\n");
            }
        }
        out.write("EOF\n");
        out.flush();
        //日志记录用户操作
        log.log(ip, user.getUsername() + " download file " + filePath + "\n");
    }

    /**
     * 处理exit命令，退出服务器
     */
    private void exit() {
        user.setLoggedIn(false);
        //日志记录用户退出
        log.log(ip, user.getUsername() + " exit.\n");
        //在服务器端输出提示信息
        System.out.println(user.getUsername() + " exit.");
        //关闭日志
        log.close();
    }

    /**
     * 处理用户输入的命令，根据命令调用相应的处理方法
     * @throws IOException 如果读取或写入数据时发生IO异常，则抛出该异常
     */
    public void process() throws IOException {
        while (true) {
            String input = in.readLine().trim();
            Command command = parseCommand(input);

            if (Objects.equals(command.type(), "dir")) {
                dir();
            } else if (Objects.equals(command.type(), "cd")) {
                cd(command.arg());
            } else if (Objects.equals(command.type(), "put")) {
                put(command.arg());
            } else if (Objects.equals(command.type(), "get")) {
                get(command.arg());
            } else if (Objects.equals(command.type(), "exit")) {
                exit();
                break;
            } else {
                out.write("Invalid command.\n");
                out.flush();
                log.log(ip, user.getUsername() + " input invalid command.\n");
            }
            //提示用户当前所在目录
            out.write(user.getUsername() + "@" + socket.getInetAddress().getHostAddress() + ":" + this.currentPath + "$ \n");
            out.flush();
        }

        // 关闭连接
        out.write("Goodbye.\n");
        out.flush();
    }
}