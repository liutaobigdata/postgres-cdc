package bigdata.cdc.config;

public class ZkConfig {
    private String address = "localhost:2181";

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
