package org.well.test.husky.sample.high;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.curator.x.discovery.ServiceInstance;
import org.well.test.husky.HuskyInstanceDetails;
import org.well.test.husky.HuskyServiceDiscoverer;

/**
 * @ClassName:HuskyClientApp
 * @Description:服务消费者测试
 * @author well
 * @date:2020年4月9日
 *
 */
public class HuskyClientApp {

    private static final String serverName = "service1";

    public static void main(String[] args) throws Exception {
        
        HuskyServiceDiscoverer huskyServiceDiscoverer = new HuskyServiceDiscoverer();
        List<ServiceInstance<HuskyInstanceDetails>> instance1s = huskyServiceDiscoverer.init().registContinuousOperation(HuskyClientApp::operation).getServicesByName(serverName);
        
        instance1s = instance1s.stream().filter(item -> item != null).collect(Collectors.toList());
        print(instance1s, false);
        
        int i = 0;
        while (++i < 60) {
            Thread.sleep(1000);
        }
        
        System.out.println("Client App Closing...");
        huskyServiceDiscoverer.close();
        System.out.println("Client App Closed.");
    }

    public static void operation(HuskyServiceDiscoverer huskyServiceDiscoverer) {
        List<ServiceInstance<HuskyInstanceDetails>> tempInstance1s = huskyServiceDiscoverer.getInstancesByName(serverName).stream()
                .filter(item -> item != null).collect(Collectors.toList());
        
        print(tempInstance1s, true);
    }

    private static void print(List<ServiceInstance<HuskyInstanceDetails>> instances, boolean isListener) {
        if (isListener) {
            System.out.println("instance1 listener ======================================================================");
        } else {
            System.out.println("instance1 ==============================================================================");
        }
        for (ServiceInstance<HuskyInstanceDetails> serviceInstance : instances) {
            System.out.println(serviceInstance.buildUriSpec());
            System.out.println(serviceInstance.getPayload());
        }
        if (instances.isEmpty()) {
            System.out.println("empty!!");
        }
    }
}
