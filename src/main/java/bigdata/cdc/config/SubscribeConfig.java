package bigdata.cdc.config;

public class SubscribeConfig {

    private String serverId;
    private JdbcConfig jdbcConfig;
    private ZkConfig zkConfig;
    private TagetDBConfig adbConfig;
    private  PgConfig pgConfig;

    public TagetDBConfig getAdbConfig() {
        return adbConfig;
    }

    public void setAdbConfig(TagetDBConfig adbConfig) {
        this.adbConfig = adbConfig;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public void setJdbcConfig(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    public ZkConfig getZkConfig() {
        return zkConfig;
    }

    public void setZkConfig(ZkConfig zkConfig) {
        this.zkConfig = zkConfig;
    }

    public PgConfig getPgConfig() {
        return pgConfig;
    }

    public void setPgConfig(PgConfig pgConfig) {
        this.pgConfig = pgConfig;
    }
}
