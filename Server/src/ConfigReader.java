import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 ConfigReader类用于从文件中读取配置信息，包括用户信息和根目录路径。
 */
public class ConfigReader {
    //文件路径
    private final String path;

    /**
     构造函数
     @param path 文件路径
     */
    public ConfigReader(String path) {
        this.path = path;
    }

    /**
     读取用户信息并存储到List中
     @return 包含用户信息的List
     @throws IOException 如果读取文件失败则抛出IOException异常
     */
    public List<User> getUsers() throws IOException {
        List<User> users = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            //读取文件的每一行，判断是否为分隔符，如果不是则将用户名和密码存入List中，如果是就退出循环
            String line = in.readLine();
            while (!line.equals("----")) {
                String[] parts = line.split(" ");
                users.add(new User(parts[0], parts[1]));
                line = in.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.println("错误发生在这里。");
            e.printStackTrace();
        }
        return users;
    }

    /**
     读取根目录路径
     @return 根目录的绝对路径
     @throws IOException 如果读取文件失败则抛出IOException异常
     */
    public String getRootDir() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(path));
        //读取文件的每一行，判断是否为分隔符，如果是则返回下一行的内容
        String line = in.readLine();
        while (!line.equals("----")) {
            line = in.readLine();
        }
        return in.readLine();
    }
}
