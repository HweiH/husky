package org.well.test.husky.core;

import java.io.Closeable;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.google.common.collect.Maps;

/**
 * @ClassName:ServiceRegistrar
 * @Description:服务注册
 * @author well
 * @date:2020年4月8日
 *
 * @param <T>
 */
public class ServiceRegistrar<T> {

    private String basePath = ServiceGlobalParam.SERVICES_BASE_PATH;
    private CuratorFramework client;
    private ServiceDiscovery<T> serviceDiscovery;
    private Map<ServiceInstance<T>, NodeCache> nodeCaches = Maps.newConcurrentMap();

    private Object lock = new Object();

    public ServiceRegistrar(CuratorFramework client, Class<T> payloadClass) throws Exception {
        this(client, ServiceGlobalParam.SERVICES_BASE_PATH, payloadClass);
    }

    public ServiceRegistrar(CuratorFramework client, String basePath, Class<T> payloadClass) throws Exception {
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
    
    public void registerService(ServiceInstance<T> serviceInstance) throws Exception {
        serviceDiscovery.registerService(serviceInstance);
        // 跟踪 Zookeeper 上对应节点
        stickToZookeeper(serviceInstance);
    }
    
    public void unregisterService(ServiceInstance<T> serviceInstance) throws Exception {
        NodeCache cache = nodeCaches.remove(serviceInstance);
        if (cache != null) {
            CloseableUtils.closeQuietly(cache);
        }
        serviceDiscovery.unregisterService(serviceInstance);
    }
    
    public void updateService(ServiceInstance<T> serviceInstance) throws Exception {
        serviceDiscovery.updateService(serviceInstance);
    }

    private NodeCache stickToZookeeper(ServiceInstance<T> serviceInstance) throws Exception  {
        NodeCache cache = nodeCaches.get(serviceInstance);
        if (cache == null) {
            synchronized (lock) {
                cache = nodeCaches.get(serviceInstance);
                if (cache == null) {
                    cache = new NodeCache(client, ZKPaths.makePath(ZKPaths.PATH_SEPARATOR, this.getBasePath(), serviceInstance.getName(), serviceInstance.getId()));
                    final NodeCache currCache = cache;
                    currCache.getListenable().addListener(new NodeCacheListener() {
                        
                        @Override
                        public void nodeChanged() throws Exception {
                            // 当 Zookeeper 上的节点删除而对应实例仍然存活时，重新注册该实例
                            if (currCache.getCurrentData() == null 
                                    && nodeCaches.containsKey(serviceInstance)) {
                                ServiceRegistrar.this.registerService(serviceInstance);
                            }
                        }
                    });
                    currCache.start(false);
                    nodeCaches.put(serviceInstance, currCache);
                }
            }
        }
        return cache;
    }
    
    public ServiceDiscovery<T> getServiceDiscovery() {
        return this.serviceDiscovery;
    }

    public String getBasePath() {
        return this.basePath;
    }
    
    public synchronized void close() {
        for (Closeable item : nodeCaches.values()) {
            CloseableUtils.closeQuietly(item);
        }
        nodeCaches.clear();
        CloseableUtils.closeQuietly(serviceDiscovery);
    }
}
