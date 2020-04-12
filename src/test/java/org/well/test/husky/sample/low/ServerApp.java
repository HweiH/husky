package org.well.test.husky.sample.low;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.well.test.husky.HuskyInstanceDetails;
import org.well.test.husky.core.ServiceRegistrar;

/**
 * @ClassName:ServerApp
 * @Description:服务提供者测试
 * @author well
 * @date:2020年4月9日
 *
 */
public class ServerApp {

    private static String zkConnectString = "127.0.0.1:2181";

    private static String microServerName_1 = "service1";
    private static String microServerPort_1 = "8080";
    private static String microServerInterfaceName_1 = "*";
    private static String microServerUriSpec_1 = "{scheme}://{address}:{port}";

    private static String microServerName_11 = "service1";
    private static String microServerPort_11 = "8085";
    private static String microServerInterfaceName_11 = "*";
    private static String microServerUriSpec_11 = "{scheme}://{address}:{port}";

    private static String microServerName_2 = "service2";
    private static String microServerPort_2 = "8090";
    private static String microServerInterfaceName_2 = "*";
    private static String microServerUriSpec_2 = "{scheme}://{address}:{port}";

    public static void main(String[] args) throws Exception {

        CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnectString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(60, TimeUnit.SECONDS);

        ServiceRegistrar<HuskyInstanceDetails> serviceRegistrar = new ServiceRegistrar<>(client, HuskyInstanceDetails.class);

        String ip = ServiceInstanceBuilder.getAllLocalIPs().stream().map(InetAddress::getHostAddress).findFirst().orElse("localhost");

        ServiceInstance<HuskyInstanceDetails> instance1 = ServiceInstance.<HuskyInstanceDetails> builder()
                .name(microServerName_1)
                .address(ip)    // address 不写的话，会取本地ip
                .port(Integer.parseInt(microServerPort_1))
                .payload(new HuskyInstanceDetails(UUID.randomUUID().toString(), ip, Integer.parseInt(microServerPort_1), microServerInterfaceName_1))
                .uriSpec(new UriSpec(microServerUriSpec_1))
                .build();
        serviceRegistrar.registerService(instance1);
        
        ServiceInstance<HuskyInstanceDetails> instance11 = ServiceInstance.<HuskyInstanceDetails> builder()
                .name(microServerName_11)
                .address(ip)    // address 不写的话，会取本地ip
                .port(Integer.parseInt(microServerPort_11))
                .payload(new HuskyInstanceDetails(UUID.randomUUID().toString(), ip, Integer.parseInt(microServerPort_11), microServerInterfaceName_11))
                .uriSpec(new UriSpec(microServerUriSpec_11))
                .build();
        serviceRegistrar.registerService(instance11);

        ServiceInstance<HuskyInstanceDetails> instance2 = ServiceInstance.<HuskyInstanceDetails> builder()
                .name(microServerName_2)
                .address(ip)    // address 不写的话，会取本地ip
                .port(Integer.parseInt(microServerPort_2))
                .payload(new HuskyInstanceDetails(UUID.randomUUID().toString(), ip, Integer.parseInt(microServerPort_2), microServerInterfaceName_2))
                .uriSpec(new UriSpec(microServerUriSpec_2))
                .build();
        serviceRegistrar.registerService(instance2);
        
        int i = 0;
        while (++i < 60) {
            Thread.sleep(1000);
        }
        
        System.out.println("service1 unregister!" + instance1.getId());
        serviceRegistrar.unregisterService(instance1);
        
        i = 0;
        while (++i < 30) {
            Thread.sleep(1000);
        }
        
        System.out.println("service1 register again!" + instance1.getId());
        serviceRegistrar.registerService(instance1);
        
        i = 0;
        while (++i < 30) {
            Thread.sleep(1000);
        }
        
        System.out.println("Server App Closing...");
        serviceRegistrar.close();
        System.out.println("Server App Closed.");
    }

}
