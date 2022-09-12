# 分布式全局ID

* 分库分表系统中，由于id引发的问题

  * 每个表都有唯一标识，通常使用id

  * id通常采用自增的方式

  * 在分库分表情况下，每张表的id都从0开始自增

  * 不同的分片上，id可能重复

  * 导致id在全局不唯一，导致业务上出现问题

  * 两个分片表中存在相同的order_id，导致业务混乱

    <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502102420836.png" alt="image-20200502102420836" style="zoom:50%;" />

#分布式全局ID-UUID

* <font color=red>UUID</font>通过唯一识别码（Universally Unique Identifier）

* 使用UUID，保证每一条记录的id都是不同的

* 缺点：

  * 只是单纯的一个id，没有实际意义
  * 长度32位，太长

* MyCat不支持UUID的方式

* Sharding-Jdbc支持UUID方式

  ```properties
  #指定Order表的order_id主键id生成策略为UUID
  spring.shardingsphere.sharding.tables.t_order.key-generator.column=order_id
  spring.shardingsphere.sharding.tables.t_order.key-generator.type=UUID
  #订单表根据order_id自定义分片规则
  spring.shardingsphere.sharding.tables.t_order.table-strategy.standard.sharding-column=order_id
  #精确分片策略
  spring.shardingsphere.sharding.tables.t_order.table-strategy.standard.precise-algorithm-class-name=com.yy.shardingjdbcdemo.sharding.MySharding
  ```

  ```java
  public class MyShardingString implements PreciseShardingAlgorithm<String> {
      @Override
      public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
          String id = shardingValue.getValue();
  
          // orderId的hashcode值 对 节点个数 取模
          int mode = id.hashCode() % availableTargetNames.size();
  
          // 分片节点数组
          String[] strings = availableTargetNames.toArray(new String[0]);
          mode = Math.abs(mode);
  
          System.out.println(strings[0] + "---------" + strings[1]);
          System.out.println("mode=" + mode);
          return strings[mode];
      }
  }
  ```

# 分布式全局ID-统一ID序列表

* ID的值统一的从一个集中的ID序列生成器中获取

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502112417715.png" alt="image-20200502112417715" style="zoom:50%;" />

* ID序列生成器 MyCat支持，Sharding-Jdbc不支持

* MyCat中有两种方式：

  * 本地文件方式
  * 数据库方式

* 本地文件方式用于测试、数据库方式用于生产

* 优点：ID集中管理，避免重复

* 缺点：并发量大时，ID生成器压力较大

# 分布式全局ID-雪花算法

* SnowFlake是由Twitter提出的分布式ID算法

* 一个64 bit的long型的数字

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502114723111.png" alt="image-20200502114723111" style="zoom:50%;" />

* 引入了时间戳，保持自增

* 基本保持全局唯一，毫秒内并发最大4096个ID

* 时间回调 可能引起ID重复

* MyCat和Sharding-Jdbc均支持雪花算法

* Sharding-Jdbc可设置最大容忍回调时间

* MyCat使用雪花算法

* Sharding-Jdbc使用雪花算法

# 分布式全局ID方案落地

* Java代码定义了一个全局ID生成器

# 分布式事务

* 分布式事务问题

  * 分布式系统中，业务拆分成多个数据库

  * 多个独立的数据库之间，无法统一事务

  * 造成数据不一致的情况

    <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502130836584.png" alt="image-20200502130836584" style="zoom:50%;" />

* CAP原理

* ACID原理与BASE原理

#基于XA协议的两阶段提交

* XA是由X/Open组织提出的分布式事务的规范

* 由一个事务管理器（TM）和多个资源管理器（RM）组成

* 提交分为两个阶段：

  * prepare

    <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502131330922.png" alt="image-20200502131330922" style="zoom:50%;" />

  * commit

    <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502140348474.png" alt="image-20200502140348474" style="zoom:50%;" />

* 保证数据的强一致性

* commit阶段出现问题，事务出现不一致，需人工处理

* 效率低下，性能与本地事务相差10倍

* MySql5.7及以上版本均支持XA协议

* MySql Connector/J 5.0以上 支持XA协议

* Java系统中，数据源采用Atomikos（充当事务管理器）

  * pom依赖

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jta-atomikos</artifactId>
    </dependency>
    ```

  * 定义RM资源管理器

    * db131

      ```java
      Configuration
      @MapperScan(value = "com.yy.xademo.db131.dao", sqlSessionFactoryRef = "sqlSessionFactoryBean131")
      public class ConfigDb131 {
      
          /**
           * 资源XA资源管理器
           *
           * @return
           */
          @Bean("db131")
          public DataSource db131() {
              MysqlXADataSource xaDataSource = new MysqlXADataSource();
              xaDataSource.setUser("root");
              xaDataSource.setPassword("root");
              xaDataSource.setUrl("jdbc:mysql://192.168.73.131:3306/xa_131");
      
              AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
              atomikosDataSourceBean.setXaDataSource(xaDataSource);
      
              return atomikosDataSourceBean;
          }
      
          @Bean("sqlSessionFactoryBean131")
          public SqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("db131") DataSource dataSource) throws IOException {
              SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
              sqlSessionFactoryBean.setDataSource(dataSource);
              ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
              sqlSessionFactoryBean.setMapperLocations(resourceResolver.getResources("mybatis/db131/*.xml"));
              return sqlSessionFactoryBean;
          }
      
      
          /**
           * 配置XA事务管理器
           */
          @Bean("xaTransaction")
          public JtaTransactionManager jtaTransactionManager() {
              UserTransaction userTransaction = new UserTransactionImp();
              UserTransactionManager userTransactionManager = new UserTransactionManager();
      
              return new JtaTransactionManager(userTransaction, userTransactionManager);
          }
      
      }
      ```

    * db132

      ```java
      @Configuration
      @MapperScan(value = "com.yy.xademo.db132.dao",sqlSessionFactoryRef = "sqlSessionFactoryBean132")
      public class ConfigDb132 {
      
          @Bean("db132")
          public DataSource db132(){
              MysqlXADataSource xaDataSource = new MysqlXADataSource();
              xaDataSource.setUser("root");
              xaDataSource.setPassword("root");
              xaDataSource.setUrl("jdbc:mysql://192.168.73.132:3306/xa_132");
      
              AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
              atomikosDataSourceBean.setXaDataSource(xaDataSource);
      
      
              return atomikosDataSourceBean;
          }
      
          @Bean("sqlSessionFactoryBean132")
          public SqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("db132") DataSource dataSource) throws IOException {
              SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
              sqlSessionFactoryBean.setDataSource(dataSource);
              ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
              sqlSessionFactoryBean.setMapperLocations(resourceResolver.getResources("mybatis/db132/*.xml"));
              return sqlSessionFactoryBean;
          }
      }
      ```

    * db131config里配置TM事务管理器

      ```java
      /**
       * 配置XA事务管理器
       */
      @Bean("xaTransaction")
      public JtaTransactionManager jtaTransactionManager() {
          UserTransaction userTransaction = new UserTransactionImp();
          UserTransactionManager userTransactionManager = new UserTransactionManager();
      
          return new JtaTransactionManager(userTransaction, userTransactionManager);
      }
      ```

# MyCat分布式事务

* vim server.xml

  * handleDistributedTransactions
    * 0：支持分布式事务
    * 1：不支持分布式事务

  ```xml
  <!--分布式事务开关，0为不过滤分布式事务，1为过滤分布式事务（如果分布式事务内只涉及全局表，则不过滤），2为不过滤分布式事务,但是记录分布式事务日志-->
  <property name="handleDistributedTransactions">0</property>
  ```

* 代码编写

  ```java
  @Service
  public class UserService {
      @Resource
      private UserMapper userMapper;
  
      @Transactional(rollbackFor = Exception.class)
      public void testUser() {
          User user1 = new User();
          user1.setId(1);
          user1.setUsername("奇数");
          userMapper.insert(user1);
  
          User user2 = new User();
          user2.setId(2);
          user2.setUsername("偶数111");
          userMapper.insert(user2);
      }
  }
  ```

# Sharding-Jdbc分布式事务

* Sharding-Jdbc默认实现分布式事务

#TCC事务补偿机制

* 什么是事务补偿机制？

* 针对每个操作，都要注册一个与其对应的补偿（撤销）操作

* 在执行失败时，调用补偿操作，撤销之前的操作

* A给B转账的例子，A和B在两家不同的银行

* A账户减200元，B账户加200元

* 两个操作要保证原子性，要么全成功、要么全失败

* 由于A和B在两家不同的银行，所以存在分布式事务问题

* 转账接口需要提供补偿机制

* 如果A在扣减的过程出现问题，直接抛出异常，事务回滚

* B在增加余额的过程中，出现问题，要调用A的补偿接口

* A之前的扣减操作，得到了补偿，进行了撤销

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502154356344.png" alt="image-20200502154356344" style="zoom:50%;" />

* 优点：逻辑清晰、流程简单
* 缺点：数据一致性比XA还要差，可能出错的点比较多
* TCC属于应用层的一种补偿方式，程序员需要写大量代码

* 代码示例：

  * db131的数据库配置

    ```java
    @Configuration
    @MapperScan(value = "com.yy.tccdemo.db131.dao",sqlSessionFactoryRef = "factoryBean131")
    public class ConfigDb131 {
    
        @Bean("db131")
        public DataSource db131() {
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("root");
            dataSource.setUrl("jdbc:mysql://192.168.73.131:3306/xa_131");
    
            return dataSource;
        }
    
        @Bean("factoryBean131")
        public SqlSessionFactoryBean factoryBean(@Qualifier("db131") DataSource dataSource) throws IOException {
            SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    
            factoryBean.setDataSource(dataSource);
            ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    
            factoryBean.setMapperLocations(resourceResolver.getResources("mybatis/db131/*.xml"));
            return factoryBean;
        }
    
        @Bean("tm131")
        public PlatformTransactionManager transactionManager(@Qualifier("db131") DataSource dataSource) {
    
            return new DataSourceTransactionManager(dataSource);
        }
    }
    ```

  * db132的数据库配置

  ```java
  @Configuration
  @MapperScan(value = "com.yy.tccdemo.db132.dao",sqlSessionFactoryRef = "factoryBean132")
  public class ConfigDb132 {
  
      @Bean("db132")
      public DataSource db132() {
          MysqlDataSource dataSource = new MysqlDataSource();
          dataSource.setUser("root");
          dataSource.setPassword("root");
          dataSource.setUrl("jdbc:mysql://192.168.73.132:3306/xa_132");
  
          return dataSource;
      }
  
      @Bean("factoryBean132")
      public SqlSessionFactoryBean factoryBean(@Qualifier("db132") DataSource dataSource) throws IOException {
          SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
  
          factoryBean.setDataSource(dataSource);
          ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
  
          factoryBean.setMapperLocations(resourceResolver.getResources("mybatis/db132/*.xml"));
          return factoryBean;
      }
  
      @Bean("tm132")
      public PlatformTransactionManager transactionManager(@Qualifier("db132") DataSource dataSource) {
  
          return new DataSourceTransactionManager(dataSource);
      }
  }
  ```
  * java代码

    ```java
    @Service
    public class AccountService {
        @Resource
        private AccountAMapper accountAMapper;
        @Resource
        private AccountBMapper accountBMapper;
    
        @Transactional(transactionManager = "tm131",rollbackFor = Exception.class)
        public void transferAccount(){
            AccountA accountA = accountAMapper.selectByPrimaryKey(1);
            accountA.setBalance(accountA.getBalance().subtract(new BigDecimal(200)));
            accountAMapper.updateByPrimaryKey(accountA);
    
            AccountB accountB = accountBMapper.selectByPrimaryKey(2);
            accountB.setBalance(accountB.getBalance().add(new BigDecimal(200)));
            accountBMapper.updateByPrimaryKey(accountB);
    
            try{
                int i = 1/0;
            }catch (Exception e){
    
                try{
                    AccountB accountb = accountBMapper.selectByPrimaryKey(2);
                    accountb.setBalance(accountb.getBalance().subtract(new BigDecimal(200)));
                    accountBMapper.updateByPrimaryKey(accountb);
                }catch (Exception e1){
    
                }
                
                throw e;
            }
    
        }
    }
    ```

# 基于本地消息表+定时任务的最终一致性方案

* 采用BASE原理，保证事务最终一致

* 在一致性方面，允许一段时间内的不一致，但最终会一致

* 在实际的系统当中，要根据具体情况，判断是否采用

* 基于本地消息表的方案中，将<font color=red>本事务外操作</font>，记录在消息表中

* 其他事务，提供操作接口

* 定时任务轮询本地消息表，将未执行的消息发送给操作接口

* 操作接口处理成功，返回成功标识，处理失败返回失败标识

* 定时任务接到标识，更新消息的状态

* 定时任务按照一定的周期反复执行

* 对于屡次失败的消息，可以设置最大失败次数

* 超过最大失败次数的消息，不再进行接口调用

* 等待人工处理

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502163815515.png" alt="image-20200502163815515" style="zoom:50%;" />

* 优点：避免了分布式事务，实现了最终一致性
* 缺点：要注意重试时的幂等性操作

* 本地消息表-数据库设计

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502165925620.png" alt="image-20200502165925620" style="zoom:50%;" />

* 本地消息表-接口设计

  ```java
  /**
   * 支付接口
   * @param userId
   * @param orderId
   * @param amount
   * @return 0:成功；1:用户不存在;2:余额不足
   */
  @Transactional(transactionManager = "tm131")
  public int pament(int userId, int orderId, BigDecimal amount){
      //支付操作
      AccountA accountA = accountAMapper.selectByPrimaryKey(userId);
      if (accountA == null) return 1;
      if (accountA.getBalance().compareTo(amount) < 0) return 2;
      accountA.setBalance(accountA.getBalance().subtract(amount));
      accountAMapper.updateByPrimaryKey(accountA);
  
      PaymentMsg paymentMsg = new PaymentMsg();
      paymentMsg.setOrderId(orderId);
      paymentMsg.setStatus(0);//未发送
      paymentMsg.setFalureCnt(0);//失败次数
      paymentMsg.setCreateTime(new Date());
      paymentMsg.setCreateUser(userId);
      paymentMsg.setUpdateTime(new Date());
      paymentMsg.setUpdateUser(userId);
  
      paymentMsgMapper.insertSelective(paymentMsg);
  
      return 0;
  }
  ```

* 本地消息表-订单操作接口

  ```java
  /**
   * 订单回调接口
   *
   * @param orderId
   * @return 0:成功 1:订单不存在
   */
  public int handleOrder(int orderId) {
      Order order = orderMapper.selectByPrimaryKey(orderId);
  
      if (order == null) return 1;
      order.setOrderStatus(1);//已支付
      order.setUpdateTime(new Date());
      order.setUpdateUser(0);//系统更新
      orderMapper.updateByPrimaryKey(order);
  
      return 0;
  }
  ```

* 本地消息表-定时任务

  ```java
  @Service
  public class OrderScheduler {
      @Resource
      private PaymentMsgMapper paymentMsgMapper;
  
      @Scheduled(cron = "0/10 * * * * ?")
      public void orderNotify() throws IOException {
  
          PaymentMsgExample paymentMsgExample = new PaymentMsgExample();
          paymentMsgExample.createCriteria().andStatusEqualTo(0);//未发送
          List<PaymentMsg> paymentMsgs = paymentMsgMapper.selectByExample(paymentMsgExample);
          if (paymentMsgs==null || paymentMsgs.size() ==0) return;
  
          for (PaymentMsg paymentMsg : paymentMsgs) {
              int order = paymentMsg.getOrderId();
  
              CloseableHttpClient httpClient = HttpClientBuilder.create().build();
              HttpPost httpPost = new HttpPost("http://localhost:8080/handleOrder");
              NameValuePair orderIdPair = new BasicNameValuePair("orderId",order+"");
              List<NameValuePair> list = new ArrayList<>();
              list.add(orderIdPair);
              HttpEntity httpEntity = new UrlEncodedFormEntity(list);
              httpPost.setEntity(httpEntity);
  
  
              CloseableHttpResponse response = httpClient.execute(httpPost);
              String s = EntityUtils.toString(response.getEntity());
  
              if ("success".equals(s)){
                  paymentMsg.setStatus(1);//发送成功
                  paymentMsg.setUpdateTime(new Date());
                  paymentMsg.setUpdateUser(0);//系统更新
                  paymentMsgMapper.updateByPrimaryKey(paymentMsg);
              }else {
                  Integer falureCnt = paymentMsg.getFalureCnt();
                  falureCnt++;
                  paymentMsg.setFalureCnt(falureCnt);
                  if (falureCnt > 5){
                      paymentMsg.setStatus(2);//失败
                  }
                  paymentMsg.setUpdateTime(new Date());
                  paymentMsg.setUpdateUser(0);//系统更新
                  paymentMsgMapper.updateByPrimaryKey(paymentMsg);
              }
          }
      }
  
  }
  ```

# 基于MQ消息队列的最终一致性方案

* 原理、流程与本地消息表类似

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200502172730923.png" alt="image-20200502172730923" style="zoom:50%;" />

* 不同点：

  * 本地消息改为MQ
  * 定时任务改为MQ的消费者

* 优点：不依赖定时任务，基于MQ更高效、更可靠

* 适合于公司内的系统

* 不同公司之间无法基于MQ，本地消息表更合适

* Rocketmq下载安装

* 引入pom依赖

  ```xml
  <dependency>
      <groupId>org.apache.rocketmq</groupId>
      <artifactId>rocketmq-client</artifactId>
      <version>4.5.2</version>
  </dependency>
  ```

* 定义生产者

  ```java
  @Bean(initMethod = "start",destroyMethod = "shutdown")
  public DefaultMQProducer producer() {
      DefaultMQProducer producer = new
              DefaultMQProducer("paymentGroup");
      // Specify name server addresses.
      producer.setNamesrvAddr("localhost:9876");
      return producer;
  }
  ```

* 定义消费者

  ```java
  @Bean(initMethod = "start",destroyMethod = "shutdown")
  public DefaultMQPushConsumer consumer(@Qualifier("messageListener") MessageListenerConcurrently messageListener) throws MQClientException {
      DefaultMQPushConsumer consumer = new
              DefaultMQPushConsumer("paymentConsumerGroup");
  
      // Specify name server addresses.
      consumer.setNamesrvAddr("localhost:9876");
  
      // Subscribe one more more topics to consume.
      consumer.subscribe("payment", "*");
  
      consumer.registerMessageListener(messageListener);
  
      return consumer;
  }
  ```

* 支付接口-消息队列

  ```java
  /**
   * 支付接口(消息队列)
   * @param userId
   * @param orderId
   * @param amount
   * @return 0:成功；1:用户不存在;2:余额不足
   */
  @Transactional(transactionManager = "tm131",rollbackFor = Exception.class)
  public int pamentMQ(int userId, int orderId, BigDecimal amount) throws Exception {
      //支付操作
      AccountA accountA = accountAMapper.selectByPrimaryKey(userId);
      if (accountA == null) return 1;
      if (accountA.getBalance().compareTo(amount) < 0) return 2;
      accountA.setBalance(accountA.getBalance().subtract(amount));
      accountAMapper.updateByPrimaryKey(accountA);
  
      Message message = new Message();
      message.setTopic("payment");
      message.setKeys(orderId+"");
      message.setBody("订单已支付".getBytes());
  
      try {
          SendResult result = producer.send(message);
          if (result.getSendStatus() == SendStatus.SEND_OK){
              return 0;
          }else {
              throw new Exception("消息发送失败！");
          }
      } catch (Exception e) {
          e.printStackTrace();
          throw e;
      }
  }
  ```

* 消费者消费

  ```java
  Component("messageListener")
  public class ChangeOrderStatus implements MessageListenerConcurrently {
      @Resource
      private OrderMapper orderMapper;
  
      @Override
      public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,
                                                      ConsumeConcurrentlyContext consumeConcurrentlyContext) {
          if (list == null || list.size()==0) return CONSUME_SUCCESS;
  
          for (MessageExt messageExt : list) {
              String orderId = messageExt.getKeys();
              String msg = new String(messageExt.getBody());
              System.out.println("msg="+msg);
              Order order = orderMapper.selectByPrimaryKey(Integer.parseInt(orderId));
  
              if (order==null) return RECONSUME_LATER;
              try {
                  order.setOrderStatus(1);//已支付
                  order.setUpdateTime(new Date());
                  order.setUpdateUser(0);//系统更新
                  orderMapper.updateByPrimaryKey(order);
              }catch (Exception e){
                  e.printStackTrace();
                  return RECONSUME_LATER;
              }
          }
  
          return CONSUME_SUCCESS;
      }
  }
  ```

# 分布式事务技术落地

* 创建订单减库存，直接使用MyCat分布式事务