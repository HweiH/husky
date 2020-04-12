# Husky Framework
Husky Framework is distributed service registration and discovery framework. like Dubbo, but smaller. You can refer to the architecture diagram of Dubbo. [Dubbo Architecture Diagram](https://img2018.cnblogs.com/blog/683560/201811/683560-20181103101749327-1518446079.png)

Husky Framework is distributed service registration and discovery framework base on [Apache Curator](http://curator.apache.org/) extension package [curator-x-discovery](http://curator.apache.org/curator-x-discovery/index.html).

### <u>Building from Source</u>
1. The project is base on Maven.
2. Import into the development tool, IDEA or Eclipse.
3. org.well.test.husky.core: Inside the package is the core class, mainly close to the Apache Curator bottom of the operation, also known as low-level API, direct use well be more complex, but provided a very free way of operation.
4. org.well.test.husky: Inside the package is encapsulation operation, called high-level API, can be used directly, and more convenient.
5. org.well.test.husky.sample: There are some examples of operations inside the package, which are divided into a high-level API and a low-level API operation example.
6. When using the sample, open the local [Zookeeper](https://zookeeper.apache.org/) service, [localhost:2181](http://localhost:2181).

### <u>Getting Started</u>
1. Service register ServiceRegisterr: As used in Spring or Spring Boot. It is recommended that the **Start Complete** life cycle of the Spring framework be completed, and the **Service Registration Interface** of our Husky framework is recommended to be embedded in the InitializingBean. At the same time, you can add your own business logic at the time of registration, and finally the **Start Complete** life cycle of the Spring framework, it is recommended that the DisposableBean be embedded in the **Registration Close Interface** of our Husky framework, and the service registration and shutdown can be found with reference to the sample.
2. Service discovery ServiceDiscoverer: As used in Sping or Spring Boot. It is recommended that the **Start Complete** life cycle of the Spring framework be completed, and the **Service Discovery Interface** of our Husky framework is recommended to be embedded in the InitializingBean. At the same time, you can add your own business logic at the time of registration, and finally the **Start Complete** life cycle of the Spring framework, it is recommended that the DisposableBean be embedded in the **Discovery Close Interface** of our Husky framework, and the service registration and shutdown can be found with reference to the sample.
