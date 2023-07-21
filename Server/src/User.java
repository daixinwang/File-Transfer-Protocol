/**
 用户类，用于表示系统中的用户。
 */
public class User {
    // 用户名和密码
    private final String username;
    private final String password;
    // 当前登录状态
    private boolean loggedIn = false;

    /**
     构造函数，用于创建新用户对象。
     @param username 用户名
     @param password 密码
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     获取用户名。
     @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     获取密码。
     @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     获取当前登录状态。
     @return 当前登录状态
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     设置用户登录状态。
     @param loggedIn 登录状态
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
