1. 为什么很多公司都选择RabbitMQ？
2. RabbitMQ的高性能是如何实现的？
3. AMQP高级协议&核心概念
4. RabbitMQ整体架构是怎样的
5. RabbitMQ中的消息是如何流转？
6. 安装&使用
7. 消息的生产者&消费者
8. RabbitMQ独有的 Exchange 交换机
9. Queue队列、Binding绑定、Virtual Host虚拟主机、Message消息
10. 如何保障消息的成功投递？
11. 幂等性概念
12. 在海量订单产生的业务高峰期，如何避免消息的重复消费
13. Confirm确认消息 & Return返回消息
14. 自定义消费者
15. 消息的ACK与重回队列
16. 消息的限流
17. TTL消息
18. 死信队列

#RabbitMQ是什么？

主要是一个开源的消息代理和队列服务器，用来通过普通协议在完全不同的应用之间共享数据，它主要是使用Erlang语言进行编写的，并且还基于AMQP协议。

优点：

与SpringAMQP完美结合，拥有丰富的API

集群模式相当丰富，提供表达式配置，HA模式，镜像队列模型



# AMQP协议

Advanced Message Queueing Protocol 高级消息协议

![image-20200428185628774](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200428185628774.png)



# RabbitMQ架构

![image-20200428190716703](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200428190716703.png)

# RabbitMQ消息流转

![image-20200428191746673](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200428191746673.png)

# RabbitMQ的安装与使用

官网：http://www.rabbitmq.com

【思路】

获取连接工厂 ConnectionFactory

通过工厂，获取一个Connection

通过Connection，获取信道Channel，主要用于发送和接收消息

将消息存储到Message Queue队列中

两个角色：生产者Producer & 消费者Consumer 

# Exchange 交换机

Exchange：接收消息，并根据路由key转发消息到绑定的队列

交换机属性：

* Name：交换机名称
* Type：交换机类型direct、topic、fanout、headers
  * Direct  Exchage
  * Topic Exchange
  * Fanout Exchange
    * 不需要处理路由键，只需要简单的将队列绑定到交换机
* Duraility：是否需要持久化，true为持久化
* Auto Delate：当最后一个绑定到Exchange上的队列删除后，自动删除该Exchange
* Interal：当前Exchange是否用于RabbitMQ内部使用，默认为False
* Arguments：扩展参数，用于扩展AMQP协议自定制化使用

<img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429164005881.png" alt="image-20200429164005881" style="zoom:50%;" />

发送端发送消息到交换机，也可以指定Routing Key

交换机和队列绑定

消费端监听队列

# Binding绑定

* Exchange和Exchange、Queue之间的连接关系

* Binding中可以包含RoutingKey或者参数

# Queue消息队列

* 消息队列，实际存储消息数据
* Durability：是否持久化，Durable：是，Transient：否
* Auto Delete：如选yes，代表当最后一个监听被移除之后，该Queue会自动被删除

# Message消息

* 服务器和应用程序之间传递的数据
* 本质上就是一段数据，由Properties和Payload（Body）组成

# Virtual host虚拟主机

* 虚拟主机，用于进行逻辑隔离，最上层的消息路由
* 一个Virtual Host里面可以有若干个Exchange和Queue
* 同一个Virtual Host里面不能有相同名称的Exchange和Queue

# 如何保障消息的成功投递

什么是生产端的可靠性投递？

* 保证消息的成功发出
* 保障MQ节点的成功接收
* 发送端接收到MQ节点确认应答
* 完善的消息进行补偿机制

生产端-可靠性投递，常见解决方案

* 方案一：消息信心落库，对消息状态进行打标

  ![image-20200429100652055](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429100652055.png)

  这种方式不适合高并发场景

  * 有两次数据持久化操作，第一次保存业务消息，第二次对数据进行记录
  * 数据IO磁盘，每次都需要读两次，数据库容易遭到瓶颈
  * 解决方法：只需要对业务数据进行入库即可

* 方案二：消息延迟投递，做二次确认，回调检查

  ![image-20200429103133052](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429103133052.png)

  互联网大公司常用的方式；也不一定能100%保证可靠性投递；极端情况，需要人工进行补偿

  主要目的：减少数据库的操作

# 幂等性概念

幂等性是什么？

​    执行多次操作，操作结果相同，这个是幂等性保障

消费端-幂等性保障

​    在海量订单产生的业务高峰期，如何避免消息的重复消费？

# 如何避免消息的重复消费？

在高并发情况下，会有很多消息到达MQ，消费端可能要监听大量的消息，难免会出现消息的重复投递，或者网络闪断，导致Broker端重发消息

消息端实现幂等性，就意味着，消息永远不会被消费多次，即使收到了多条一样的消息

有可能代码会执行多次，但数据库只会执行这一步操作

业界主流的幂等性操作

* 唯一ID+指纹码机制，利用数据库主键去重

  * 有些用户可能在某一瞬间就进行多次消费，比如刚刚转了一笔钱，接着又马上转了一笔

  * 指纹码：某些业务规则或者生成的信息拼接而成

  * select count(1) from tb_order where id = 唯一ID+指纹码，如果已经有记录，代表已经被操作了
  * 好处：实现简单
  * 坏处：高并发下有数据库写入的性能瓶颈
  * 解决方案：跟进ID进行分库分表进行算法路由

* 利用Redis的原子性去实现

  * 使用Redis进行幂等，需要考虑的问题

    set一个key，第二次还set，就会更新为最新值

    也可以做一个预先判断，exsit()操作，存在就不更新了

    最简单的自增，也是可以保障的

  * 是否要进行数据落库，如果落库，关键的问题是数据库和缓存如何做到原子性？

  * 如果不落库，都存储到缓存中，如何设置定时同步的策略？

# Confirm确认消息

* 理解Confirm消息确认机制

  * 消息的确认，指生产者投递消息后，如果broker收到消息，会给生产者一个应答

  * 生产者进行接收应答，用来确定这条消息是否正常的发送到broker，这种方式是消息可靠性投递的核心保障

  * 确认机制流程，是异步操作

    ![image-20200429140947275](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429140947275.png)

* 如何实现Confirm确认消息？

  1. 在channel上开启确认模式：channel.confirmSelect()

  2. 在channel上添加监听：addConfirmListener

     监听成功和失败的返回结果，根据具体的结果对消息进行重新发送、或记录日志等后续处理

     ```java
     public class Sender4ConfirmListener {
     
     	
     	public static void main(String[] args) throws Exception {
     		
     		//1 创建ConnectionFactory
     		ConnectionFactory connectionFactory = new ConnectionFactory();
     		connectionFactory.setHost("192.168.11.71");
     		connectionFactory.setPort(5672);
     		connectionFactory.setVirtualHost("/");
     		
     		//2 创建Connection
     		Connection connection = connectionFactory.newConnection();
     		//3 创建Channel
     		Channel channel = connection.createChannel();  
     		
     		//4 声明
     		String exchangeName = "test_confirmlistener_exchange";
     		String routingKey1 = "confirm.save";
     		
         	//5 发送
     		String msg = "Hello World RabbitMQ 4 Confirm Listener Message ...";
     		
     		channel.confirmSelect();
         // confirm确认消息监听
         channel.addConfirmListener(new ConfirmListener() {
     			@Override
     			public void handleNack(long deliveryTag, boolean multiple) throws IOException {
     				System.err.println("------- error ---------");
     			}
     			@Override
     			public void handleAck(long deliveryTag, boolean multiple) throws IOException {
     				System.err.println("------- ok ---------");
     			}
     		});
         // 发送消息    
     		channel.basicPublish(exchangeName, routingKey1 , null , msg.getBytes());
     	}
     	
     }
     ```

     

# Return返回消息

* Return消息机制

  * Return Listener用于处理一些不可路由的消息

  * 消息生产者，通过指定一个Exchange和Routing Key，把消息送达到某一个队列中去，消费者监听队列，进行消费处理操作

  * 在某些情况下，在发送消息的时候，当前的Exchange不存在或者指定的路由Key路由不到，这个时候如果需要监听这种不可到达的消息，就要使用Return Listener

  * 在基础 API 中有一个关键的配置项Mandatory，如果为 true，则监听器会接收到路由不可达的消息，然后进行后续处理。如果为 false，那么 broker 端自动删除该消息。

    ![image-20200429140905240](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429140905240.png)

* 如何实现Return返回消息？

  ```java
  public class Sender4ReturnListener {
  
  	
  	public static void main(String[] args) throws Exception {
  		
  		//1 创建ConnectionFactory
  		ConnectionFactory connectionFactory = new ConnectionFactory();
  		connectionFactory.setHost("192.168.11.71");
  		connectionFactory.setPort(5672);
  		connectionFactory.setVirtualHost("/");
  		
  		//2 创建Connection
  		Connection connection = connectionFactory.newConnection();
  		//3 创建Channel
  		Channel channel = connection.createChannel();  
  		
  		//4 声明
  		String exchangeName = "test_returnlistener_exchange";
  		String routingKey1 = "abcd.save";
  		String routingKey2 = "return.save";
  		String routingKey3 = "return.delete.abc";
  		
  		//5 监听
      	channel.addReturnListener(new ReturnListener() {
  			public void handleReturn(int replyCode,
  						            String replyText,
  						            String exchange,
  						            String routingKey,
  						            BasicProperties properties,
  						            byte[] body)
  					throws IOException {
  				System.out.println("**************handleReturn**********");
  				System.out.println("replyCode: " + replyCode);
  				System.out.println("replyText: " + replyText);
  				System.out.println("exchange: " + exchange);
  				System.out.println("routingKey: " + routingKey);
  				System.out.println("body: " + new String(body));
  			}
      	});
      	
      	//6 发送
  		String msg = "Hello World RabbitMQ 4 Return Listener Message ...";
  		
  		boolean mandatory = true;
  		channel.basicPublish(exchangeName, routingKey1 , mandatory, null , msg.getBytes()); 
  //		channel.basicPublish(exchangeName, routingKey2 , null , msg.getBytes()); 	
  ///		channel.basicPublish(exchangeName, routingKey3 , null , msg.getBytes());
  	}
  }
  ```

  

# 自定义消费者

#消费端限流

* 什么是消费端限流？
  * 假设一个场景，RabbitMQ 服务器上有上万条未处理的消息，随便打开一个消费者客户端，会出现下面的情况：
  * 巨大量的消息瞬间全部推送过来，但是单个客户端无法同时处理这么多数据
* RabbitMQ 提供了一种 qos（服务质量保证）功能，即在非自动确认消息的前提下，如果一定数目的消息（通过基于 consumer 或者 channel 设置 Qos 的值）未被确认，不进行消费新的消息
* void BasicQos(uint prefetchSize, ushort prefetchCount, bool global);
  *  prefetchSize：0
  * prefetchCount：告诉 RabbitMQ 不要同时给一个消费者推送多于 N 个消息，即一旦有 N 个消息还没有 ack，则该 consumer 将 block 掉，直到有消息 ack
  * global：true / false 是否将上面设置应用于 channel，简单说，就是上面限制是 channel 级别的还是 consumer 级别
  * prefetchSize 和 global 这两项，rabbitmq 没有实现，暂且不研究。prefetch_count 在 no_ask = false 的情况下生效，即在自动应答的情况下这两个值是不生效的

```java
// 限流
channel.basicQos(0, 1, false);
```



# 消费端ACK与重回队列

<img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429163522491.png" alt="image-20200429163522491" style="zoom:50%;" />

* 消费端手工ack和nack
  * 消费端进行消费的时候，由于业务异常，我们可以进行日志的记录，然后进行补偿
  * 如果由于服务器宕机等严重问题，就需要手工进行ack保障消费端消费成功
* 消费端的重回队列
  * 消费端重回队列是为了对没有处理成功的消息，把消息重新传给Broker
  * 一般在实际应用中，都会关闭重回队列，也就是autoAck设置为false

![image-20200429143912933](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429143912933.png)

```java
        //	参数：队列名称、是否自动ACK、Consumer 
        channel.basicConsume(queueName, false, consumer);  
        //	循环获取消息  
        while(true){  
            //	获取消息，如果没有消息，这一步将会一直阻塞  
            Delivery delivery = consumer.nextDelivery();  
            String msg = new String(delivery.getBody());    
            System.out.println("收到消息：" + msg);  
            Thread.sleep(1000);
            
            if((Integer)delivery.getProperties().getHeaders().get("flag") == 0) {
            	//throw new RuntimeException("异常");
            	channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            } else {
            	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } 
```



# TTL队列/消息

* TTL是Time To Live的缩写，也就是生存时间
* RabbitMQ支持消息的过期时间，在消息发送时可以进行指定
* RabbitMQ支持队列的过期时间，从消息入队列开始计算，只要超过了队列的超时时间配置，消息会自动的清除

# 死信队列

* 死信队列DLX，Dead-Letter-Exchange，利用DLX，当消息在一个队列中变成死信（dead message）之后，它能重新publish到另一个Exchange，这个Exchange就是DLX
* 消息变成死信队列的几种情况
  * 消息被拒绝（basic.reject / basic.nack）并且 requeue = false 
  * 消息TTL过期
  * 队列达到最大长度
* DLX也是一个正常的Exchange，和一般的Exchange没有区别，它能在任何的队列上被指定，实际上就是设置这个队列的属性
* 当这个队列中有死信时，RabbitMQ会自动的将这个消息重新发布到设置的Exchange上去，进而被路由到另一个队列
* 可以监听这个队列中消息做响应的处理，这个特性可以弥补 RabbitMQ 3.0 以前支持的 imediate 参数的功能
* 死信队列设置
  * 首先需要设置死信队列的 exchange 和 queue，然后进行绑定
    * Exchange：dlx.exchange
    * Queue：dlx.queue
    * RoutingKey：\#
  * 然后进行正常声明交换机
    *  队列、绑定，只不过需要在队列上加一个参数
    * arguments.put("x-dead-letter-exchange", "dlx.exchange");
  * 这样消息在过期、requeue、队列在达到最大长度时，消息就可以直接路由到死信队列

```java
public class Receiver4DLXtExchange {

	public static void main(String[] args) throws Exception {
		
        ConnectionFactory connectionFactory = new ConnectionFactory() ;  
        
        connectionFactory.setHost("192.168.11.71");
        connectionFactory.setPort(5672);
		connectionFactory.setVirtualHost("/");
		
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(3000);
        Connection connection = connectionFactory.newConnection();
        
        Channel channel = connection.createChannel();  
		//4 声明正常的 exchange queue 路由规则
		String queueName = "test_dlx_queue";
		String exchangeName = "test_dlx_exchange";
		String exchangeType = "topic";
		String routingKey = "group.*";
		//	声明 exchange
		channel.exchangeDeclare(exchangeName, exchangeType, true, false, false, null);
		
		
		//	注意在这里要加一个特殊的属性arguments: x-dead-letter-exchange
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("x-dead-letter-exchange", "dlx.exchange");
		//arguments.put("x-dead-letter-routing-key", "dlx.*");
		//arguments.put("x-message-ttl", 6000);
		channel.queueDeclare(queueName, false, false, false, arguments);
		channel.queueBind(queueName, exchangeName, routingKey);
		
		
		//dlx declare:
		channel.exchangeDeclare("dlx.exchange", exchangeType, true, false, false, null);
		channel.queueDeclare("dlx.queue", false, false, false, null);
		channel.queueBind("dlx.queue", "dlx.exchange", "#");
		
		
        //	durable 是否持久化消息
        QueueingConsumer consumer = new QueueingConsumer(channel);
        //	参数：队列名称、是否自动ACK、Consumer
        channel.basicConsume(queueName, true, consumer);  
        //	循环获取消息  
        while(true){  
            //	获取消息，如果没有消息，这一步将会一直阻塞  
            Delivery delivery = consumer.nextDelivery();  
            String msg = new String(delivery.getBody());    
            System.out.println("收到消息：" + msg);  
        } 
	}
}
```



# SET部署

- 单元化：

  ​	把一个大的集群，拆分开，不要直接做成一个太大的集群，如果集群太大的话，一旦出现问题，整个业务线都会崩溃

- 概述

  1. 了解SET架构的演进
  2. 大企中SET化架构是如何推进的
  3. 理解SET化架构的设计和具体的解决方案是怎么实现的？

* 主要避免多个业务线，在某个功能出了问题之后，导致整个业务线产生一个非常巨大的影响。

* 如何避免？

  调整架构设计

* 巨大的订单量，在高峰期会导致几个问题

  1. 容灾问题

     * 核心的业务挂了

     * 如果是主机房挂掉了，无法快速恢复或切换

  2. 资源扩展问题

     * 可能影响的地方
       * 服务端
       * 前端
       * 核心的链路
       * 数据库
     * 跨机房
       * 延迟

  3. 大集群中拆分

* 同城“双活”

  * 比如部署了两套中心、两个机房，相互切换
  * 分担了流量，在业务的高峰期就可以去做一个分流
  * 数据持久层，任务缓存、持久化、持久层数据分析

* 两地三中心

# RabbitMQ集群架构模式

**镜像模式**

<img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429175113864.png" alt="image-20200429175113864" style="zoom:50%;" />

**集群搭建**

1. 集群节点安装

   1. 安装依赖包 PS:安装rabbitmq所需要的依赖包

      ```nginx
      yum install build-essential openssl openssl-devel unixODBC unixODBC-devel make gcc gcc-c++ kernel-devel m4 ncurses-devel tk tc xz
      ```

   2. 下载安装包

      ```nginx
      wget www.rabbitmq.com/releases/erlang/erlang-18.3-1.el7.centos.x86_64.rpm
      wget http://repo.iotti.biz/CentOS/7/x86_64/socat-1.7.3.2-5.el7.lux.x86_64.rpm
      wget www.rabbitmq.com/releases/rabbitmq-server/v3.6.5/rabbitmq-server-3.6.5-1.noarch.rpm
      ```

   3. 安装服务命令

      ```nginx
      rpm -ivh erlang-18.3-1.el7.centos.x86_64.rpm 
      rpm -ivh socat-1.7.3.2-1.1.el7.x86_64.rpm
      rpm -ivh rabbitmq-server-3.6.5-1.noarch.rpm
      //卸载
      rpm -qa | grep rabbitmq
      rpm -e --allmatches rabbitmq-server-3.6.5-1.noarch
      rpm -qa | grep erlang
      rpm -e --allmatches erlang-18.3-1.el7.centos.x86_64
      rpm -qa | grep socat
      rpm -e --allmatches socat-1.7.3.2-5.el7.lux.x86_64
      rm -rf /usr/lib/rabbitmq/ /etc/rabbitmq/ /var/lib/rabbitmq/
      ```

   4. 修改集群用户与连接心跳检测

      ```nginx
      注意修改vim /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.5/ebin/rabbit.app文件
      修改：loopback_users 中的 <<"guest">>,只保留guest
      修改：heartbeat 为10
      ```

   5. 安装管理插件

      ```java
      //首先启动服务
      /etc/init.d/rabbitmq-server start stop status restart
      //查看服务有没有启动： lsof -i:5672
      rabbitmq-plugins enable rabbitmq_management
      //可查看管理端口有没有启动： lsof -i:15672 或者 netstat -tnlp|grep 15672
      ```

   6. 服务指令

      ```nginx
      /etc/init.d/rabbitmq-server start stop status restart
      ```

      验证单个节点是否安装成功：http://192.168.11.71:15672/

       Ps：以上操作三个节点（71、72、73）同时进行操作

2. 文件同步步骤

   PS:选择71、72、73任意一个节点为Master（这里选择71为Master），也就是说我们需要把71的Cookie文件同步到72、73节点上去，进入/var/lib/rabbitmq目录下，把/var/lib/rabbitmq/.erlang.cookie文件的权限修改为777，原来是400；然后把.erlang.cookie文件copy到各个节点下；最后把所有cookie文件权限还原为400即可。

   ```nginx
   //进入目录修改权限；远程copy72、73节点
   scp /var/lib/rabbitmq/.erlang.cookie 192.168.11.72:/var/lib/rabbitmq/
   scp /var/lib/rabbitmq/.erlang.cookie 192.168.11.73:/var/lib/rabbitmq/
   ```

3. 组成集群步骤

   1. 停止MQ服务

      PS:我们首先停止3个节点的服务：(这里不能使用原来的命令：/etc/init.d/rabbitmq-server stop)

      ```nginx
      rabbitmqctl stop
      ```

   2. 组成集群操作

      PS:接下来我们就可以使用集群命令，配置71、72、73为集群模式，3个节点（71、72、73）执行启动命令，后续启动集群使用此命令即可。

      ```nginx
      rabbitmq-server -detached
      ```

   3. slave加入集群操作（重新加入集群也是如此，以最开始的主节点为加入节点）

      ```nginx
      //注意做这个步骤的时候：需要配置/etc/hosts 必须相互能够寻址到
      bhz72：rabbitmqctl stop_app
      bhz72：rabbitmqctl join_cluster --ram rabbit@bhz71
      bhz72：rabbitmqctl start_app
      bhz73：rabbitmqctl stop_app
      bhz73：rabbitmqctl join_cluster rabbit@bhz71
      bhz73：rabbitmqctl start_app
      //在另外其他节点上操作要移除的集群节点
      rabbitmqctl forget_cluster_node rabbit@bhz71
      ```

   4. 修改集群名称

      PS:修改集群名称（默认为第一个node名称）：

      ```nginx
      rabbitmqctl set_cluster_name rabbitmq_cluster1
      ```

   5. 查看集群状态

      PS:最后在集群的任意一个节点执行命令：查看集群状态

      ```nginx
      rabbitmqctl cluster_status
      ```

   6. 管控台界面

      PS: 访问任意一个管控台节点：http://192.168.11.71:15672 如图所示

      <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429180715918.png" alt="image-20200429180715918" style="zoom:50%;" />

4. 配置镜像队列

   PS:设置镜像队列策略（在任意一个节点上执行）

   PS:将所有队列设置为镜像队列，即队列会被复制到各个节点，各个节点状态一致，RabbitMQ高可用集群就已经搭建好了,我们可以重启服务，查看其队列是否在从节点同步。

   ```nginx
   rabbitmqctl set_policy ha-all "^" '{"ha-mode":"all"}'
   ```

5. 消息一致性问题

   在使用rabbitmq中，消息的一致性是非常重要的一个话题。下面我们来研究一下，在数据一致性方面，有哪些需要关注的。发送者发送消息出来，在数据一致性的要求下，我们通常认为必须达到以下条件

   broker持久化消息

   publisher知道消息已经成功持久化

   首先，我们可以采用事务来解决此问题。每个消息都必须经历以上两个步骤，就算一次事务成功。

   事务是同步的。因此，如果采用事务，发送性能必然很差。官方给出来的性能是：

   ![image-20200429181357168](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429181357168.png)

   <font color=red>异步的方法的效率是事务方法效率的100倍。</font>

   我们可以采用异步的方式来解决此问题。publisher发送消息后，不进行等待，而是异步监听是否成功。这种方式又分为两种模式，一种是return，另一种是confirm. 前一种是publisher发送到exchange后，异步收到消息。第二种是publisher发送消息到exchange,queue,consumer收到消息后才会收到异步收到消息。可见，第二种方式更加安全可靠。

   但是，异步也存在些局限性。如果一旦出现broker挂机或者网络不稳定，broker已经成功接收消息，但是publisher并没有收到confirm或return.这时，对于publisher来说，只能重发消息解决问题。而在这里面，我们会发生重复消息的问题。当然，如果业务类型要求数据一致性非常高，可以采用低效率的事务型解决方案：引用：http://www.rabbitmq.com/blog/2011/02/10/introducing-publisher-confirms/

6. 安装Ha-Proxy

   1. Haproxy简介

      HAProxy是一款提供高可用性、负载均衡以及基于TCP和HTTP应用的代理软件，HAProxy是完全免费的、借助HAProxy可以快速并且可靠的提供基于TCP和HTTP应用的代理解决方案。

      HAProxy适用于那些负载较大的web站点，这些站点通常又需要会话保持或七层处理。 

      HAProxy可以支持数以万计的并发连接,并且HAProxy的运行模式使得它可以很简单安全的整合进架构中，同时可以保护web服务器不被暴露到网络上。

      PS:haproxy学习博客：https://www.cnblogs.com/f-ck-need-u/p/8540805.html

   2. Haproxy安装

      PS:74、75节点同时安装Haproxy，下面步骤统一

      ```nginx
      /下载依赖包
      //下载haproxy
      wget http://www.haproxy.org/download/1.6/src/haproxy-1.6.5.tar.gz
      //解压
      tar -zxvf haproxy-1.6.5.tar.gz -C /usr/local
      //进入目录、进行编译、安装
      cd /usr/local/haproxy-1.6.5
      make TARGET=linux31 PREFIX=/usr/local/haproxy
      make install PREFIX=/usr/local/haproxy
      mkdir /etc/haproxy
      //赋权
      groupadd -r -g 149 haproxy
      useradd -g haproxy -r -s /sbin/nologin -u 149 haproxy
      //创建haproxy配置文件
      touch /etc/haproxy/haproxy.cfg
      ```

   3. Haproxy配置

      PS:haproxy 配置文件haproxy.cfg详解

      ```nginx
      vim /etc/haproxy/haproxy.cfg
      ```

      ```nginx
      #logging options
      global
      	log 127.0.0.1 local0 info
      	maxconn 5120
      	chroot /usr/local/haproxy
      	uid 99
      	gid 99
      	daemon
      	quiet
      	nbproc 20
      	pidfile /var/run/haproxy.pid
      
      defaults
      	log global
      	#使用4层代理模式，”mode http”为7层代理模式
      	mode tcp
      	#if you set mode to tcp,then you nust change tcplog into httplog
      	option tcplog
      	option dontlognull
      	retries 3
      	option redispatch
      	maxconn 2000
      	contimeout 10s
           ##客户端空闲超时时间为 60秒 则HA 发起重连机制
           clitimeout 10s
           ##服务器端链接超时时间为 15秒 则HA 发起重连机制
           srvtimeout 10s	
      #front-end IP for consumers and producters
      
      listen rabbitmq_cluster
      	bind 0.0.0.0:5672
      	#配置TCP模式
      	mode tcp
      	#balance url_param userid
      	#balance url_param session_id check_post 64
      	#balance hdr(User-Agent)
      	#balance hdr(host)
      	#balance hdr(Host) use_domain_only
      	#balance rdp-cookie
      	#balance leastconn
      	#balance source //ip
      	#简单的轮询
      	balance roundrobin
      	#rabbitmq集群节点配置 #inter 每隔五秒对mq集群做健康检查， 2次正确证明服务器可用，2次失败证明服务器不可用，并且配置主备机制
              server bhz71 192.168.11.71:5672 check inter 5000 rise 2 fall 2
              server bhz72 192.168.11.72:5672 check inter 5000 rise 2 fall 2
              server bhz73 192.168.11.73:5672 check inter 5000 rise 2 fall 2
      #配置haproxy web监控，查看统计信息
      listen stats
      	bind 192.168.11.74:8100
      	mode http
      	option httplog
      	stats enable
      	#设置haproxy监控地址为http://localhost:8100/rabbitmq-stats
      	stats uri /rabbitmq-stats
      	stats refresh 5s
      ```

   4. 启动haproxy

      ```nginx
      /usr/local/haproxy/sbin/haproxy -f /etc/haproxy/haproxy.cfg
      //查看haproxy进程状态
      ```

   5. 访问haproxy

      PS:访问如下地址可以对rmq节点进行监控：[http://192.168.11.74:8100/rabbitmq-stats](http://192.168.1.27:8100/rabbitmq-stats)

   6. 关闭haproxy

      ```nginx
      killall haproxy
      ps -ef | grep haproxy
      netstat -tunpl | grep haproxy
      ps -ef |grep haproxy |awk '{print $2}'|xargs kill -9
      ```

7. 安装KeepAlived

   1. Keepalived简介

      Keepalived，它是一个高性能的服务器高可用或热备解决方案，Keepalived主要来防止服务器单点故障的发生问题，可以通过其与Nginx、Haproxy等反向代理的负载均衡服务器配合实现web服务端的高可用。Keepalived以VRRP协议为实现基础，用VRRP协议来实现高可用性（HA）.VRRP（Virtual Router Redundancy Protocol）协议是用于实现路由器冗余的协议，VRRP协议将两台或多台路由器设备虚拟成一个设备，对外提供虚拟路由器IP（一个或多个）。

   2. Keepalived安装

      PS:下载地址：http://www.keepalived.org/download.html

      ```nginx
      //安装所需软件包
      yum install -y openssl openssl-devel
      //下载
      wget http://www.keepalived.org/software/keepalived-1.2.18.tar.gz
      //解压、编译、安装
      tar -zxvf keepalived-1.2.18.tar.gz -C /usr/local/
      cd ..
      cd keepalived-1.2.18/ && ./configure --prefix=/usr/local/keepalived
      make && make install
      //将keepalived安装成Linux系统服务，因为没有使用keepalived的默认安装路径（默认路径：/usr/local）,安装完成之后，需要做一些修改工作
      //首先创建文件夹，将keepalived配置文件进行复制：
      mkdir /etc/keepalived
      cp /usr/local/keepalived/etc/keepalived/keepalived.conf /etc/keepalived/
      //然后复制keepalived脚本文件：
      cp /usr/local/keepalived/etc/rc.d/init.d/keepalived /etc/init.d/
      cp /usr/local/keepalived/etc/sysconfig/keepalived /etc/sysconfig/
      ln -s /usr/local/sbin/keepalived /usr/sbin/
      //如果存在则进行删除: rm /sbin/keepalived
      ln -s /usr/local/keepalived/sbin/keepalived /sbin/
      //可以设置开机启动：chkconfig keepalived on，到此我们安装完毕!
      chkconfig keepalived on
      ```

   3. Keepalived配置

      PS:修改keepalived.conf配置文件

      ```nginx
      vim /etc/keepalived/keepalived.conf
      ```

      PS: 79节点（Master）配置如下

      ```nginx
      ! Configuration File for keepalived
      
      global_defs {
         router_id bhz74  ##标识节点的字符串，通常为hostname
      
      }
      
      vrrp_script chk_haproxy {
          script "/etc/keepalived/haproxy_check.sh"  ##执行脚本位置
          interval 2  ##检测时间间隔
          weight -20  ##如果条件成立则权重减20
      }
      
      vrrp_instance VI_1 {
          state MASTER  ## 主节点为MASTER，备份节点为BACKUP
          interface eno16777736 ## 绑定虚拟IP的网络接口（网卡），与本机IP地址所在的网络接口相同（我这里是eth0）
          virtual_router_id 74  ## 虚拟路由ID号（主备节点一定要相同）
          mcast_src_ip 192.168.11.74 ## 本机ip地址
          priority 100  ##优先级配置（0-254的值）
          nopreempt
          advert_int 1  ## 组播信息发送间隔，俩个节点必须配置一致，默认1s
      authentication {  ## 认证匹配
              auth_type PASS
              auth_pass bhz
          }
      
          track_script {
              chk_haproxy
          }
      
          virtual_ipaddress {
              192.168.11.70  ## 虚拟ip，可以指定多个
          }
      }
      ```

      PS: 80节点（backup）配置如下

      ```nginx
      ! Configuration File for keepalived
      
      global_defs {
         router_id bhz75  ##标识节点的字符串，通常为hostname
      
      }
      
      vrrp_script chk_haproxy {
          script "/etc/keepalived/haproxy_check.sh"  ##执行脚本位置
          interval 2  ##检测时间间隔
          weight -20  ##如果条件成立则权重减20
      }
      
      vrrp_instance VI_1 {
          state BACKUP  ## 主节点为MASTER，备份节点为BACKUP
          interface eno16777736 ## 绑定虚拟IP的网络接口（网卡），与本机IP地址所在的网络接口相同（我这里是eno16777736）
          virtual_router_id 74  ## 虚拟路由ID号（主备节点一定要相同）
          mcast_src_ip 192.168.11.75  ## 本机ip地址
          priority 90  ##优先级配置（0-254的值）
          nopreempt
          advert_int 1  ## 组播信息发送间隔，俩个节点必须配置一致，默认1s
      authentication {  ## 认证匹配
              auth_type PASS
              auth_pass bhz
          }
      
          track_script {
              chk_haproxy
          }
      
          virtual_ipaddress {
              192.168.1.70  ## 虚拟ip，可以指定多个
          }
      }
      ```

   4. 执行脚本编写

      PS:添加文件位置为/etc/keepalived/haproxy_check.sh（74、75两个节点文件内容一致即可）

      ```nginx
      #!/bin/bash
      COUNT=`ps -C haproxy --no-header |wc -l`
      if [ $COUNT -eq 0 ];then
          /usr/local/haproxy/sbin/haproxy -f /etc/haproxy/haproxy.cfg
          sleep 2
          if [ `ps -C haproxy --no-header |wc -l` -eq 0 ];then
              killall keepalived
          fi
      fi
      ```

   5. 执行脚本赋权

      PS:haproxy_check.sh脚本授权,赋予可执行权限.

      ```nginx
      chmod +x /etc/keepalived/haproxy_check.sh
      ```

   6. 启动keepalived

      PS:当我们启动俩个haproxy节点以后，我们可以启动keepalived服务程序：

      ```nginx
      //如果74、75的haproxy没有启动则执行启动脚本
      /usr/local/haproxy/sbin/haproxy -f /etc/haproxy/haproxy.cfg
      //查看haproxy进程状态
      ps -ef | grep haproxy
      //启动两台机器的keepalived
      service keepalived start | stop | status | restart
      //查看状态
      ps -ef | grep haproxy
      ps -ef | grep keepalived
      ```

   7. 高可用测试

      PS:vip在27节点上

      PS:27节点宕机测试：停掉27的keepalived服务即可。

      ![image-20200429195226280](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429195226280.png)

      PS:查看28节点状态：我们发现VIP漂移到了28节点上，那么28节点的haproxy可以继续对外提供服务！

      ![image-20200429195305938](/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429195305938.png)

8. 集群配置文件

   创建如下配置文件位于：/etc/rabbitmq目录下（这个目录需要自己创建）

   环境变量配置文件：rabbitmq-env.conf 

   配置信息配置文件：rabbitmq.config（可以不创建和配置，修改）

   rabbitmq-env.conf配置文件：

   ```nginx
   配置参考参数如下：
   RABBITMQ_NODENAME=FZTEC-240088 节点名称
   RABBITMQ_NODE_IP_ADDRESS=127.0.0.1 监听IP
   RABBITMQ_NODE_PORT=5672 监听端口
   RABBITMQ_LOG_BASE=/data/rabbitmq/log 日志目录
   RABBITMQ_PLUGINS_DIR=/data/rabbitmq/plugins 插件目录
   RABBITMQ_MNESIA_BASE=/data/rabbitmq/mnesia 后端存储目录
   更详细的配置参见： http://www.rabbitmq.com/configure.html#configuration-file
   ```

9. 服务测试运行

   1. 集群启动

      rabbitMQ集群启动：

      ```nginx
      /启动各个MQ节点
      rabbitmq-server -detached
      //查看集群状态
      rabbitmqctl cluster_status
      ```

      rabbitMQ集群关闭：

      ```nginx
      //各节点停止MQ集群命令
      rabbitmqctl stop_app | start_app | cluster_status | reset
      //各节点停止MQ服务
      /etc/init.d/rabbitmq-server stop | start | restart | status
      ```

      设置keepalived开机启动后，则会直接运行chk_haproxy.sh脚本，从而启动haproxy服务，所以对于负载均衡和高可用层我们无需任何配置。

      PS:由《2.2章节 MQ服务架构图》所示。我们的虚拟VIP节点为192.168.1.20，所以我们进行MQ服务生产消费消息测试。

   2. 测试代码

      MQ Sender代码

      ```java
      package bhz.rabbitmq.helloword;
      
      import java.util.concurrent.ExecutorService;
      import java.util.concurrent.Executors;
      import java.util.concurrent.TimeUnit;
      
      import com.rabbitmq.client.Address;
      import com.rabbitmq.client.Channel;
      import com.rabbitmq.client.Connection;
      import com.rabbitmq.client.ConnectionFactory;
        
      public class Sender {  
           
          public static void main(String[] args) throws Exception {  
                
          	ConnectionFactory connectionFactory = new ConnectionFactory() ;  
          	
              //RabbitMQ-Server安装在本机，所以直接用127.0.0.1  
              connectionFactory.setHost("192.168.1.20");
              connectionFactory.setPort(5672);
              Connection connection = connectionFactory.newConnection();
              Channel channel = connection.createChannel() ;  
              //定义Queue名称  
              String queueName = "queue01" ;  
              //为channel定义queue的属性，queueName为Queue名称  
              channel.queueDeclare(queueName , false, false, false, null) ;  
              for(int i =0; i < 100000; i ++){
                  //发送消息  
              	String msg = "Hello World RabbitMQ " + i;
                  channel.basicPublish("", queueName , null , msg.getBytes());         	
                  System.out.println("发送数据：" + msg);
                  TimeUnit.SECONDS.sleep(1);
              }
              channel.close();   
              connection.close();   
          }  
      }  
      ```

      MQ Receiver代码

      ```java
      package bhz.rabbitmq.helloword;
      
      import java.util.concurrent.ExecutorService;
      import java.util.concurrent.Executors;
      
      import com.rabbitmq.client.Address;
      import com.rabbitmq.client.Channel;
      import com.rabbitmq.client.Connection;
      import com.rabbitmq.client.ConnectionFactory;
      import com.rabbitmq.client.QueueingConsumer;
      import com.rabbitmq.client.QueueingConsumer.Delivery;
      
      public class Receiver {  
          public static void main(String[] args) throws Exception {  
              ConnectionFactory connectionFactory = new ConnectionFactory() ;  
              connectionFactory.setHost("192.168.1.20");
              connectionFactory.setPort(5672);
              Connection connection = connectionFactory.newConnection();
              Channel channel = connection.createChannel() ;  
              String queueName = "queue01";  
              channel.queueDeclare(queueName, false, false, false, null) ;     
              //上面的部分，与Sender01是一样的  
              //配置好获取消息的方式  
              QueueingConsumer consumer = new QueueingConsumer(channel) ;  
              channel.basicConsume(queueName, true, consumer) ;  
              //循环获取消息  
              while(true){  
                  //获取消息，如果没有消息，这一步将会一直阻塞  
                  Delivery delivery = consumer.nextDelivery() ;  
                  String msg = new String(delivery.getBody()) ;    
                  System.out.println("收到消息：" + msg);  
              }  
          }  
      }  
      ```

      

# RabbitMQ整合SpringBoot2.X

* 生产端核心配置

  ```properties
  spring.rabbitmq.publisher-confirms=true
  spring.rabbitmq.publisher-returns=true
  spring.rabbitmq.template.mandatory=true
  ```

* 消费端核心配置

  ```properties
  spring.rabbitmq.listener.simple.acknowledge-mode=MANUAL
  spring.rabbitmq.listener.simple.concurrency=1
  spring.rabbbitmq.listener.simple.max-concurrency=5
  ```

* @RabbitListener注解使用

  消费端监听@RabbitListener注解

  @QueueBing @Queue @Exchange

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200429200926703.png" alt="image-20200429200926703" style="zoom:50%;" />

  

#RabbitMQ基础组件封装

基础组件实现关键点

* 一线大厂的MQ组件实现思路和架构设计方案
* 基础组件封装设计-迅速消息发送
* 基础组件封装设计-确认消息发送
* 基础组件封装设计-延迟消息发送

基础组件实现功能点

* 迅速、延迟、可靠
* 消息异步化序列化
* 链接池化、高性能
* 完备的补偿机制