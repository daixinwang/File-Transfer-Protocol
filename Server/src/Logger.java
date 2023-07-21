import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 日志记录器，用于记录操作信息和时间戳到指定文件中。
 */
public class Logger {
    private static final String LOG_FILE_NAME = "log.txt"; // 日志文件名
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 时间格式

    private final FileWriter fileWriter; // 文件写入流

    /**
     构造函数，创建一个新的日志文件写入流
     @param rootDirectory 根目录路径
     @throws IOException 如果创建文件失败，则抛出IOException异常
     */
    public Logger(String rootDirectory) throws IOException {
        File logFile = new File(rootDirectory, LOG_FILE_NAME);
        this.fileWriter = new FileWriter(logFile, true);
    }

    /**
     记录日志方法，包含IP地址、时间和操作信息，并将其写入日志文件中
     @param ip 客户端IP地址
     @param message 操作信息
     */
    public synchronized void log(String ip, String message) {
        try {
            fileWriter.write("[" + DATE_FORMAT.format(new Date()) + "] " + ip + ": " + message + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            // 输出英文错误信息
            System.err.println("Failed to write to log file:" + e.getMessage());
        }
    }

    /**
     关闭日志记录器，关闭文件写入流
     */
    public synchronized void close() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Failed to close the log file:" + e.getMessage());
        }
    }
}