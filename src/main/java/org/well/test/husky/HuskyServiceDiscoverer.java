package org.well.test.husky;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.well.test.husky.core.ServiceDiscoverer;

/**
 * @ClassName:HuskyServiceDiscoverer
 * @Description:服务发现
 * @author well
 * @date:2020年4月9日
 *
 */
public class HuskyServiceDiscoverer {

    // Zookeeper 连接
    private String zkConnectString;

    private ServiceDiscoverer<HuskyInstanceDetails> serviceDiscoverer;
    private Consumer<HuskyServiceDiscoverer> operation;

    /**
     * 默认地址：127.0.0.1:2181
     */
    public HuskyServiceDiscoverer() {
        zkConnectString = "127.0.0.1:2181";
    }

    /**
     * 初始化
     *
     * @return
     */
    public HuskyServiceDiscoverer init() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnectString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        try {
            client.blockUntilConnected(60, TimeUnit.SECONDS);
            serviceDiscoverer = new ServiceDiscoverer<>(client, HuskyInstanceDetails.class);
        } catch (Exception e) {
            throw new RuntimeException("Initialize Discoverer error. Try again later...", e);
        }
        return this;
    }
    
    /**
     * 注册服务实例有变化时将执行的操作
     *
     * @param operation
     * @return
     */
    public HuskyServiceDiscoverer registContinuousOperation(Consumer<HuskyServiceDiscoverer> operation) {
        this.operation = operation;
        return this;
    }
    
    /**
     * 根据指定的服务名，获取对应的服务实例。如果设置了持续性操作registContinuousOperation，那么服务实例有变化时将执行该操作
     *
     * @param serverName
     * @return
     */
    public List<ServiceInstance<HuskyInstanceDetails>> getServicesByName(String serverName) {
         return this.getInstancesByName(serverName, newListener());
    }
    
    /**
     * 根据指定的服务名，获取对应的服务实例。如果设置了持续性操作registContinuousOperation，那么服务实例有变化时将执行该操作
     *
     * @param serverName
     * @return
     */
    public ServiceInstance<HuskyInstanceDetails> getServiceByName(String serverName) {
        return this.getInstanceByName(serverName, newListener());
    }

    // 新建服务实例监听器
    private HuskyServiceCacheListenerAdapter newListener() {
        return new HuskyServiceCacheListenerAdapter() {

            @Override
            public void cacheChanged() {
                synchronized (HuskyServiceDiscoverer.this.operation) {
                    if (HuskyServiceDiscoverer.this.operation != null) {
                        HuskyServiceDiscoverer.this.operation.accept(HuskyServiceDiscoverer.this);
                    }
                }
            }
        };
    }
    
    /**
     * 根据指定的服务名，获取对应的服务实例。可以设置服务实例的监听器
     *
     * @param serverName
     * @param serviceCacheListeners
     * @return
     */
    public List<ServiceInstance<HuskyInstanceDetails>> getInstancesByName(String serverName, ServiceCacheListener... serviceCacheListeners ) {
        List<ServiceInstance<HuskyInstanceDetails>> instances = Collections.emptyList();
        try {
            instances = serviceDiscoverer.getInstancesByName(serverName, serviceCacheListeners);
        } catch (Exception e) {
            // ignore
        }
        return instances;
    }
    
    /**
     * 根据指定的服务名，获取对应的服务实例。可以设置服务实例的监听器
     *
     * @param serverName
     * @param serviceCacheListeners
     * @return
     */
    public ServiceInstance<HuskyInstanceDetails> getInstanceByName(String serverName, ServiceCacheListener... serviceCacheListeners ) {
        ServiceInstance<HuskyInstanceDetails> instance = null;
        try {
            instance = serviceDiscoverer.getInstanceByName(serverName, serviceCacheListeners);
        } catch (Exception e) {
            // ignore
        }
        return instance;
    }
    
    /**
     * 获取当前注册器使用的服务发现辅助对象
     *
     * @return
     */
    public ServiceDiscovery<HuskyInstanceDetails> getServiceDiscovery() {
        return serviceDiscoverer.getServiceDiscovery();
    }
    
    /**
     * 设置发现多个服务实例时的选择策略。可用策略有：
     * org.apache.curator.x.discovery.strategies.RandomStrategy
     * org.apache.curator.x.discovery.strategies.RoundRobinStrategy
     * org.apache.curator.x.discovery.strategies.StickyStrategy
     *
     * @param providerStrategy
     */
    public void setProviderStrategy(ProviderStrategy<HuskyInstanceDetails> providerStrategy) {
        serviceDiscoverer.setProviderStrategy(providerStrategy);
    }
    
    /**
     * 获取当前注册根路径
     *
     * @return
     */
    public String getBasePath() {
        return serviceDiscoverer.getBasePath();
    }
    
    /**
     * 关闭资源
     *
     */
    public void close() {
        serviceDiscoverer.close();
    }
    
    public String getZkConnectString() {
        return zkConnectString;
    }

    public void setZkConnectString(String zkConnectString) {
        this.zkConnectString = zkConnectString;
    }

    /**
     * @ClassName:HuskyServiceCacheListenerAdapter
     * @Description:ServiceCacheListener监听器的适配器
     * @author well
     * @date:2020年4月9日
     *
     */
    public static abstract class HuskyServiceCacheListenerAdapter implements ServiceCacheListener {

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            // nothing to do...
        }
    }
}
