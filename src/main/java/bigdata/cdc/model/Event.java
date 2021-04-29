package  bigdata.cdc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Event implements Serializable {

    private static final long serialVersionUID = 3414755790085772526L;

    private long lsn;

    private transient String slotName;
    private transient String serverId;
    private String schema;
    private String table;
    private EventType eventType;
    private List<ColumnData> dataList = new ArrayList<>();

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public List<ColumnData> getDataList() {
        return dataList;
    }

    public void setDataList(List<ColumnData> dataList) {
        this.dataList = dataList;
    }

    public long getLsn() {
        return lsn;
    }

    public void setLsn(long lsn) {
        this.lsn = lsn;
    }

}
