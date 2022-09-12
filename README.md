# foodie-cloud

#### 项目文档

#### 项目介绍
foodie-cloud是springcloud微服务版本天天吃货在线购物平台。包括首页门户、商品推荐、商品搜索、商品展示、商品评价、购物车、订单流程、用户中心等功能。SpringCloud Greenwich.SR1+SpringBoot 2.1.5.RELEASE版本。集成Redis缓存、消息队列(RabbitMq、RocketMQ)、分布式搜索ElasticSearch、分布式文件系统（FastDFS、OSS）、分布式锁、分库分表（MyCat、Sharding-Jdbc）、分布式全局ID、分布式事务、分布式限流、监控、ELK日志搜索等

#### 项目演示
项目演示地址：http://www.alianlyy.top/

#### 项目结构

```
foodie-cloud
├── common
│   ├── foodie-cloud-common -- 工具类及通用代码    
│   ├── foodie-cloud-shared-pojo -- 通用类
│   └── foodie-cloud-web-components -- 公用web组件
├── domain -- 业务微服务
│   ├── auth -- 认证中心
│   │   ├── foodie-auth-api 
│   │   └── foodie-auth-service
│   ├── cart -- 购物车
│   │   ├── foodie-cart-api
│   │   ├── foodie-cart-service
│   │   └── foodie-cart-web
│   ├── item -- 商品中心
│   │   ├── foodie-item-api
│   │   ├── foodie-item-mapper
│   │   ├── foodie-item-pojo
│   │   ├── foodie-item-service
│   │   └── foodie-item-web
│   ├── order -- 订单中心
│   │   ├── foodie-order-api
│   │   ├── foodie-order-mapper
│   │   ├── foodie-order-pojo
│   │   ├── foodie-order-service
│   │   └── foodie-order-web
│   └── user -- 用户中心
│       ├── foodie-user-api
│       ├── foodie-user-mapper
│       ├── foodie-user-pojo
│       ├── foodie-user-service
│       └── foodie-user-web
├── platform -- 平台组件微服务
│   ├── config-server -- 分布式配置中心模块
│   ├── gateway -- 服务网关模块
│   ├── hystrix-dashboard -- dashboard监控模块
│   ├── hystrix-turbine -- turbine监控模块
│   ├── registry-center -- 注册中心模块
│   └── zipkin-server -- 链路追踪模块

```

#### 技术选型


#### 架构图
##### 系统架构图
![输入图片说明](https://images.gitee.com/uploads/images/2020/0426/144526_dff4f76c_1185227.jpeg "foodie-cloud系统架构图2.jpg")
##### 业务架构图
![输入图片说明](https://images.gitee.com/uploads/images/2020/0426/151140_08bddc92_1185227.jpeg "foodie-cloud业务架构图.jpg")

