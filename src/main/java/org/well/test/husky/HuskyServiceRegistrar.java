package org.well.test.husky;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.well.test.husky.core.ServiceRegistrar;
import org.well.test.husky.util.InetAddressUtils;

/**
 * @ClassName:HuskyServiceRegistrar
 * @Description:服务注册
 * @author well
 * @date:2020年4月9日
 *
 */
public class HuskyServiceRegistrar {

    // Zookeeper 连接
    private String zkConnectString;
    // 服务名
    private String serverName;
    // 服务所在机器的IP
    private String serverIp;
    // 服务端口，默认随机端口
    private int serverPort;
    // 接口路径匹配，匹配规则：AntPathMatcher
    private String interfaceName;
    // 服务根路径模式，取值参考：org.apache.curator.x.discovery.UriSpec.build(ServiceInstance<?> serviceInstance, Map<String, Object> variables)
    private String serverUriSpec;
    // 注册时的数据 payload
    private HuskyInstanceDetails serverDetailData;

    private ServiceRegistrar<HuskyInstanceDetails> serviceRegistrar;
    private ServiceInstance<HuskyInstanceDetails> localServiceInstance;

    public HuskyServiceRegistrar() {
        zkConnectString = "127.0.0.1:2181";
        serverName = "service-" + UUID.randomUUID().toString().replaceAll("-", "");
        serverIp = getAllLocalIPs().stream().map(InetAddress::getHostAddress).findFirst().orElse("localhost");
        serverPort = new Random().nextInt(9999) + 10000;
        interfaceName = "*";
        serverUriSpec = "{scheme}://{address}:{port}";
        serverDetailData = new HuskyInstanceDetails(UUID.randomUUID().toString(), serverIp, serverPort, interfaceName);
    }

    // 获取所有本地可用IP
    private static Collection<InetAddress> getAllLocalIPs() {
        try {
            Collection<InetAddress> all = ServiceInstanceBuilder.getAllLocalIPs();
            // 优先选择 ipv4
            List<InetAddress> ipv4 = all.stream().filter(item -> InetAddressUtils.isIPv4Address(item.getHostAddress())).collect(Collectors.toList());
            if (ipv4 != null && !ipv4.isEmpty()) {
                return ipv4;
            }
            return all;
        } catch (SocketException e) {
            // ignore
        }
        return Collections.emptyList();
    }

    public HuskyServiceRegistrar init() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnectString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        try {
            client.blockUntilConnected(60, TimeUnit.SECONDS);
            serviceRegistrar = new ServiceRegistrar<>(client, HuskyInstanceDetails.class);
        } catch (Exception e) {
            throw new RuntimeException("Initialize Registrar error. Try again later...", e);
        }
        return this;
    }

    public boolean registLocalService() {
        if (this.localServiceInstance == null) {
            this.localServiceInstance = buildLocalServiceInstance();
        }
        return registService(this.localServiceInstance);
    }

    // 构建本地服务实例
    private synchronized ServiceInstance<HuskyInstanceDetails> buildLocalServiceInstance() {
        try {
            ServiceInstance<HuskyInstanceDetails> instance = ServiceInstance.<HuskyInstanceDetails> builder()
                    .name(serverName)
                    .address(serverIp)  // address不写的话，会取本地ip
                    .port(serverPort)
                    .payload(serverDetailData)
                    .uriSpec(new UriSpec(serverUriSpec))
                    .build();
            return instance;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
    
    public boolean registService(ServiceInstance<HuskyInstanceDetails> instance) {
        if (this.serviceRegistrar == null) {
            throw new RuntimeException("Please init first.");
        }
        if (instance == null) {
            return false;
        }
        
        try {
            this.serviceRegistrar.registerService(instance);
            return true;
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean registServices(ServiceInstance<HuskyInstanceDetails>... instances) {
        if (instances == null) {
            return false;
        }
        boolean isSuccess = true;
        for (ServiceInstance<HuskyInstanceDetails> instance : instances) {
            isSuccess &= registService(instance);
            if (!isSuccess) {
                int flag = Arrays.asList(instances).indexOf(instance);
                // 撤销之前注册成功的实例
                unregistServices(Arrays.copyOfRange(instances, 0, flag));
                break;
            }
        }
        return isSuccess;
    }
    
    public void unregistLocalService() {
        unregistService(this.localServiceInstance);
    }
    
    public void unregistService(ServiceInstance<HuskyInstanceDetails> registedInstance) {
        if (registedInstance == null) {
            return ;
        }
        try {
            this.serviceRegistrar.unregisterService(registedInstance);
        } catch (Exception e) {
            // ignore
        }
    }

    @SuppressWarnings("unchecked")
    public void unregistServices(ServiceInstance<HuskyInstanceDetails>... registedInstances) {
        for (ServiceInstance<HuskyInstanceDetails> registedInstance : registedInstances) {
            unregistService(registedInstance);
        }
    }

    public boolean updateService(ServiceInstance<HuskyInstanceDetails> serviceInstance) {
        try {
            this.serviceRegistrar.updateService(serviceInstance);
            return true;
        } catch (Exception e) {
            // ignore
        }
        return false;
    }
    
    public ServiceDiscovery<HuskyInstanceDetails> getServiceDiscovery() {
        return this.serviceRegistrar.getServiceDiscovery();
    }
    
    public String getBasePath() {
        return this.serviceRegistrar.getBasePath();
    }
    
    public void close() {
        this.serviceRegistrar.close();
    }

    public String getZkConnectString() {
        return zkConnectString;
    }

    public void setZkConnectString(String zkConnectString) {
        this.zkConnectString = zkConnectString;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
        if (this.serverDetailData != null) {
            this.serverDetailData.setListenAddress(serverIp);
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
        if (this.serverDetailData != null) {
            this.serverDetailData.setListenPort(serverPort);
        }
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        if (this.serverDetailData != null) {
            this.serverDetailData.setInterfaceName(interfaceName);
        }
    }

    public String getServerUriSpec() {
        return serverUriSpec;
    }

    public void setServerUriSpec(String serverUriSpec) {
        this.serverUriSpec = serverUriSpec;
    }

    public HuskyInstanceDetails getServerDetailData() {
        return serverDetailData;
    }

    public void setServerDetailData(HuskyInstanceDetails serverDetailData) {
        this.serverDetailData = serverDetailData;
    }
}
