package   bigdata.cdc.config;

public class TagetDBConfig {

    private String url;
    private String user;
    private String password;
    private String timeout;

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TagetDBConfig url(String url) {
        this.url = url;
        return this;
    }

    public TagetDBConfig user(String user) {
        this.user = user;
        return this;
    }

    public TagetDBConfig password(String password) {
        this.password = password;
        return this;
    }

    public TagetDBConfig timeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

}
