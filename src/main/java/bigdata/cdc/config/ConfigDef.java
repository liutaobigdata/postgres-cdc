package bigdata.cdc.config;

import lombok.Data;

@Data
public class ConfigDef {
    private String appId;
    private String metaDomain;
    private String configFile;

    private boolean useApollo;
    private boolean useYukon;

    private int processId;

    private String tableFile;
    private String schema;
}
