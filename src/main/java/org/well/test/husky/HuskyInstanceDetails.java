package org.well.test.husky;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @ClassName:HuskyInstanceDetails
 * @Description:服务注册与消费时的附加数据 payload
 * @author well
 * @date:2020年4月9日
 *
 */
@JsonRootName("details")
public class HuskyInstanceDetails {

    private String id;

    private String listenAddress;

    private int listenPort;
    // 接口路径匹配，匹配规则：AntPathMatcher
    private String interfaceName = "*";

    public HuskyInstanceDetails(String id, String listenAddress, int listenPort, String interfaceName) {
        this.id = id;
        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
        this.interfaceName = interfaceName;
    }

    public HuskyInstanceDetails(String id, String listenAddress, int listenPort) {
        this.id = id;
        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
    }

    public HuskyInstanceDetails() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public String toString() {
        return "HuskyInstanceDetails [id=" + id + ", listenAddress=" + listenAddress + ", listenPort=" + listenPort + ", interfaceName="
                + interfaceName + "]";
    }
    
}
