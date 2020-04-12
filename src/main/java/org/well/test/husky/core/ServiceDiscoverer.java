package org.well.test.husky.core;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

import com.google.common.collect.Maps;

/**
 * @ClassName:ServiceDiscoverer
 * @Description:服务发现
 * @author well
 * @date:2020年4月8日
 *
 * @param <T>
 */
public class ServiceDiscoverer<T> {

    private String basePath = ServiceGlobalParam.SERVICES_BASE_PATH;
    private CuratorFramework client;
    private ServiceDiscovery<T> serviceDiscovery;
    private Map<String, ServiceCache<T>> serviceCaches = Maps.newConcurrentMap();

    private Object lock = new Object();

    private volatile ProviderStrategy<T> providerStrategy;

    public ServiceDiscoverer(CuratorFramework client, Class<T> payloadClass) throws Exception {
        this(client, ServiceGlobalParam.SERVICES_BASE_PATH, payloadClass);
    }

    public ServiceDiscoverer(CuratorFramework client, String basePath, Class<T> payloadClass) throws Exception {
        this.basePath = basePath;
        this.client = client;
        JsonInstanceSerializer<T> serializer = new JsonInstanceSerializer<>(payloadClass);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(payloadClass)
                .serializer(serializer)
                .client(this.client)
                .basePath(this.basePath)
                .build();
        
        serviceDiscovery.start();
    }

    public ServiceInstance<T> getInstanceByName(String serviceName, ServiceCacheListener... cacheListeners) throws Exception {
        if (this.providerStrategy == null) {
            this.providerStrategy = new RandomStrategy<>();
        }
        ServiceCache<T> serviceCache = getServiceCache(serviceName, cacheListeners);
        return this.providerStrategy.getInstance(serviceCache);
    }
    
    public List<ServiceInstance<T>> getInstancesByName(String serviceName, ServiceCacheListener... cacheListeners) throws Exception {
        ServiceCache<T> serviceCache = getServiceCache(serviceName, cacheListeners);
        return serviceCache.getInstances();
    }
    
    private ServiceCache<T> getServiceCache(String serviceName, ServiceCacheListener... cacheListeners) throws Exception {
        ServiceCache<T> serviceCache = serviceCaches.get(serviceName);
        if (serviceCache == null) {
            synchronized (lock) {
                serviceCache = serviceCaches.get(serviceName);
                if (serviceCache == null) {
                    serviceCache = serviceDiscovery.serviceCacheBuilder()
                            .name(serviceName)
                            .build();
                    if (cacheListeners != null) {
                        for (ServiceCacheListener serviceCacheListener : cacheListeners) {
                            serviceCache.addListener(serviceCacheListener);
                        }
                    }
                    serviceCache.start();
                    serviceCaches.put(serviceName, serviceCache);
                }
            }
        }
        return serviceCache;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public ServiceDiscovery<T> getServiceDiscovery() {
        return this.serviceDiscovery;
    }

    public void setProviderStrategy(ProviderStrategy<T> providerStrategy) {
        this.providerStrategy = providerStrategy;
    }
    
    public synchronized void close() {
        for (Closeable item : serviceCaches.values()) {
            CloseableUtils.closeQuietly(item);
        }
        serviceCaches.clear();
        CloseableUtils.closeQuietly(serviceDiscovery);
    }
}
