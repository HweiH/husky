package org.well.test.husky.sample.low;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceInstance;
import org.well.test.husky.HuskyInstanceDetails;
import org.well.test.husky.HuskyServiceDiscoverer.HuskyServiceCacheListenerAdapter;
import org.well.test.husky.core.ServiceDiscoverer;

/**
 * @ClassName:ClientApp
 * @Description:服务消费者测试
 * @author well
 * @date:2020年4月9日
 *
 */
public class ClientApp {

    private static String zkConnectString = "127.0.0.1:2181";
    
    private static String microServerName_1 = "service1";
    private static String microServerName_2 = "service2";
    
    public static void main(String[] args) throws Exception {
        
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnectString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(60, TimeUnit.SECONDS);
        
        ServiceDiscoverer<HuskyInstanceDetails> serviceDiscoverer = new ServiceDiscoverer<>(client, HuskyInstanceDetails.class);
        
        List<ServiceInstance<HuskyInstanceDetails>> instance1s = serviceDiscoverer.getInstancesByName(microServerName_1, new HuskyServiceCacheListenerAdapter() {

            @Override
            public void cacheChanged() {
                
                try {
                    List<ServiceInstance<HuskyInstanceDetails>> tempInstance1s = serviceDiscoverer.getInstancesByName(microServerName_1)
                            .stream().filter(item -> item != null).collect(Collectors.toList());
                    System.out.println("instance1 listener ======================================================================");
                    for (ServiceInstance<HuskyInstanceDetails> serviceInstance : tempInstance1s) {
                        System.out.println(serviceInstance.buildUriSpec());
                        System.out.println(serviceInstance.getPayload());
                    }
                    if (tempInstance1s.isEmpty()) {
                        System.out.println("empty!!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        instance1s = instance1s.stream().filter(item -> item != null).collect(Collectors.toList());
        System.out.println("instance1 ==============================================================================");
        for (ServiceInstance<HuskyInstanceDetails> serviceInstance : instance1s) {
            System.out.println(serviceInstance.buildUriSpec());
            System.out.println(serviceInstance.getPayload());
        }
        if (instance1s.isEmpty()) {
            System.out.println("empty!!");
        }
        
        ServiceInstance<HuskyInstanceDetails> instance2 = serviceDiscoverer.getInstanceByName(microServerName_2, new HuskyServiceCacheListenerAdapter() {

            @Override
            public void cacheChanged() {
                
                try {
                    ServiceInstance<HuskyInstanceDetails> tempInstance2 = serviceDiscoverer.getInstanceByName(microServerName_2);
                    System.out.println("instance2 listener ======================================================================");
                    if (tempInstance2 != null) {
                        System.out.println(tempInstance2.buildUriSpec());
                        System.out.println(tempInstance2.getPayload());
                    } else {
                        System.out.println("empty!!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("instance2 ==============================================================================");
        if (instance2 != null) {
            System.out.println(instance2.buildUriSpec());
            System.out.println(instance2.getPayload());
        } else {
            System.out.println("empty!!");
        }
        
        int i = 0;
        while (++i < 1000) {
            Thread.sleep(1000);
        }
        
        System.out.println("Client App Closing...");
        serviceDiscoverer.close();
        System.out.println("Client App Closed.");
    }
}
