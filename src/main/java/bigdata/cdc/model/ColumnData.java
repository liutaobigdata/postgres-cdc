package bigdata.cdc.model;

import java.io.Serializable;
import java.util.Objects;

public class ColumnData implements Serializable {


    private static final long serialVersionUID = 4767055418095657107L;
    private String name;
    private String dataType;
    private String value;

    public ColumnData() {
    }

    public ColumnData(String name, String dataType, String value) {
        this.name = name;
        this.dataType = dataType;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
