package org.well.test.husky.sample.high;

import org.well.test.husky.HuskyServiceRegistrar;

/**
 * @ClassName:HuskyServerApp
 * @Description:服务提供者测试
 * @author well
 * @date:2020年4月9日
 *
 */
public class HuskyServerApp {
    
    private static final String serverName = "service1";

    public static void main(String[] args) throws Exception {
        
        HuskyServiceRegistrar huskyServiceRegistrar = new HuskyServiceRegistrar();
        huskyServiceRegistrar.setServerName(serverName);
        huskyServiceRegistrar.init().registLocalService();
        
        int i = 0;
        while (++i < 30) {
            Thread.sleep(1000);
        }

        System.out.println("Server App Closing...");
        huskyServiceRegistrar.close();
        System.out.println("Server App Closed.");
    }
}
