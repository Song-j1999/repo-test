# FastDFS核心概念与架构原理解析

* 为什么要使用分布式文件系统

  * 海量文件数据存储
  * 文件数据高可用（冗余备份）
  * 读写性能和负载均衡
  * 以上3点都是我们之前使用tomcat或nginx所不能够实现的，这也是我们为什么要使用分布式文件系统的原因

* FastDFS 与 HDFS

  * Hadoop中的文件系统HDFS主要解决并行计算中分布式存储数据的问题。其单个数据文件通常很大，采用了分块（切分）存储的方式，所以是大数据大文件存储来使用的场景
  * FastDFS主要用于互联网网站，为文件上传和下载提供在线服务。所以在负载均衡、动态扩容等方面都支持得比较好，FastDFS不会对文件进行分快存储。FastDFS用于存储中小文件都是不错的，比如用户头像啊，一些较小的音视频文件啊等等都行

* FastDFS常见术语

  * tracker：追踪者服务器，主要用于协调调度，可以起到负载均衡的作用，记录storage的相关状态信息。
  * storage：存储服务器，用于保存文件以及文件的元数据信息。
  * group：组，同组节点提供冗余备份，不同组用于扩容。
  * mata data：文件的元数据信息，比如长宽信息，图片后缀，视频的帧数等。

* FastDFS架构

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200503211236476.png" alt="image-20200503211236476" style="zoom:50%;" />

* FastDFS上传过程

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200503211339625.png" alt="image-20200503211339625" style="zoom:50%;" />

*  FastDFS下载过程

  <img src="/Users/liuyuyan/Library/Application Support/typora-user-images/image-20200503211704632.png" alt="image-20200503211704632" style="zoom:50%;" />

  

# FastDFS配置

* **配置FastDFS环境准备工作**

  * 参考文献

    https://github.com/happyfish100/
    https://github.com/happyfish100/fastdfs/wiki
    https://www.cnblogs.com/leechenxiang/p/5406548.html
    https://www.cnblogs.com/leechenxiang/p/7089778.html

  * 环境准备

    * Centos7.x 两台，分别安装tracker与storage
    * 下载安装包：
      * libfatscommon：FastDFS分离出的一些公用函数包
      * FastDFS：FastDFS本体
      * fastdfs-nginx-module：FastDFS和nginx的关联模块
      * nginx：发布访问服务

  * 安装步骤（tracker和storage都要执行）

    * 安装基础环境

      ```nginx
      yum install -y gcc gcc-c++
      yum -y install libevent
      ```

    * 安装libfatscommon函数库

      ```nginx
      # 解压
      tar -zxvf libfastcommon-1.0.42.tar.gz
      ```

    * 进入libfastcommon文件夹，编译并且安装

      ```nginx
      ./make.sh
      ./make.sh install
      ```

    * 安装fastdfs主程序文件

      ```nginx
      # 解压
      tar -zxvf fastdfs-6.04.tar.gz
      ```

      进入到fastdfs目录，查看fastdfs安装配置

      ```nginx
      cd fastdfs-6.04/
      vim make.sh
      ```

      ```nginx
      TARGET_PREFIX=$DESTDIR/usr
      TARGET_CONF_PATH=$DESTDIR/etc/fdfs
      TARGET_INIT_PATH=$DESTDIR/etc/init.d
      ```

      安装fastdfs

      ```nginx
      ./make.sh
      ./make.sh install 
      ```

      <img src="https://climg.mukewang.com/5e0ef484082a831c05840194.jpg" alt="图片描述" style="zoom:50%;" />

      如上图，

      * `/usr/bin` 中包含了可执行文件；

      * `/etc/fdfs` 包含了配置文件；

        <img src="https://climg.mukewang.com/5e0ef4aa082ddd3610940470.jpg" alt="图片描述" style="zoom:50%;" />

      <img src="https://climg.mukewang.com/5e0ef4bc083b7c0109940238.jpg" alt="图片描述" style="zoom:50%;" />

    * 拷贝配置文件如下

      ```nginx
      cp /home/software/FastDFS/fastdfs-6.04/conf/* /etc/fdfs/
      ```

      <img src="https://climg.mukewang.com/5e0ef4cf08b65f3b13560470.jpg" alt="图片描述" style="zoom:50%;" />

* **配置tracker服务**

  * 说明

    * tracker和storage都是同一个fastdfs的主程序的两个不同概念，配置不同的配置文件就可以设定为tracker或者storage

  * 配置tracker

    `/etc/fdfs`下都是一些配置文件，配置tracker即可

    ```nginx
    vim tracker.conf
    ```

    <img src="https://climg.mukewang.com/5e0ef55e08c034fc11160388.jpg" alt="图片描述" style="zoom:50%;" />

    * 修改tracker配置文件，此为tracker的工作目录，保存数据以及日志

      ```nginx
      base_path=/usr/local/fastdfs/tracker
      ```

      ```nginx
      mkdir /usr/local/fastdfs/tracker -p
      ```

  * 启动tracker服务 

    ```nginx
    /usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf
    ```

    检查进程如下：

    ```nginx
    ps -ef|grep tracker
    ```

  * 停止tracker

    ```nginx
    /usr/bin/stop.sh /etc/fdfs/tracker.conf
    ```

* 配置storage服务

  * 修改该storage.conf配置文件

    <img src="https://climg.mukewang.com/5e0ef5c108c15a6116000535.jpg" alt="图片描述" style="zoom:50%;" />

    ```nginx
    # 修改组名
    group_name=imooc
    # 修改storage的工作空间
    base_path=/usr/local/fastdfs/storage
    # 修改storage的存储空间
    store_path0=/usr/local/fastdfs/storage
    # 修改tracker的地址和端口号，用于心跳
    tracker_server=192.168.1.153:22122
    
    # 后续结合nginx的一个对外服务端口号
    http.server_port=8888
    ```

  * 创建目录

    ```nginx
    mkdir /usr/local/fastdfs/storage -p
    ```

  * 启动storage

    **前提：必须首先启动tracker**

    ```nginx
    /usr/bin/fdfs_storaged /etc/fdfs/storage.conf
    ```

    检查进程如下：

    ```nginx
    ps -ef|grep storage
    ```

    <img src="https://climg.mukewang.com/5e0ef61c08e61f1d16000147.jpg" alt="图片描述" style="zoom:50%;" />

  * 测试上传

    * 修改的client配置文件

      <img src="https://climg.mukewang.com/5e0ef62b089e938e15020712.jpg" alt="图片描述" style="zoom:50%;" />

      ```nginx
       base_path=/usr/local/fastdfs/client
       tracker_server=192.168.1.153:22122
      ```

      ```nginx
        mkdir /usr/local/fastdfs/client
      ```

    * 测试：

      ```nginx
      wget https://www.imooc.com/static/img/index/logo.png
      ./fdfs_test /etc/fdfs/client.conf upload /home/logo.png
      ```

      <img src="https://climg.mukewang.com/5e0ef64008dfab1e16000857.jpg" alt="图片描述" style="zoom:50%;" />

* 配置nginx fastdfs实现文件服务器

  	> 引子
    fastdfs安装好以后是无法通过http访问的，这个时候就需要借助nginx了，所以需要安装fastdfs的第三方模块到nginx中，就能使用了。
  	> 
  	>注：nginx需要和storage在同一个节点。
  
* 安装nginx插件
  
  * 解压nginx的fastdfs压缩包
  
    ```nginx
      tar -zxvf fastdfs-nginx-module-1.22.tar.gz
      ```
  
  * 复制配置文件如下：
  
    ```nginx
      cp mod_fastdfs.conf /etc/fdfs
      ```
  
    <img src="https://climg.mukewang.com/5e0ef6d608a9590613200494.jpg" alt="图片描述" style="zoom:50%;" />
  
  * 修改/fastdfs-nginx-module/src/config文件，主要是修改路径，把`local`删除，因为fastdfs安装的时候没有修改路径，原路径是`/usr`：
  
    <img src="https://climg.mukewang.com/5e0ef6e508e482c828261280.png" alt="图片描述" style="zoom:50%;" />
  
* 安装nginx（略）
  
  * 其中配置如下：
  
    ```nginx
      ./configure \
      --prefix=/usr/local/nginx \
      --pid-path=/var/run/nginx/nginx.pid \
      --lock-path=/var/lock/nginx.lock \
      --error-log-path=/var/log/nginx/error.log \
      --http-log-path=/var/log/nginx/access.log \
      --with-http_gzip_static_module \
      --http-client-body-temp-path=/var/temp/nginx/client \
      --http-proxy-temp-path=/var/temp/nginx/proxy \
      --http-fastcgi-temp-path=/var/temp/nginx/fastcgi \
      --http-uwsgi-temp-path=/var/temp/nginx/uwsgi \
      --http-scgi-temp-path=/var/temp/nginx/scgi \
      --add-module=/home/software/fdfs/fastdfs-nginx-module-1.22/src
      ```
  
  * 主要新增一个第三方模块，修改 mod_fastdfs.conf 配置文件：
  
    ```nginx
      base_path=/usr/local/fastdfs/tmp
      tracker_server=192.168.1.153:22122
      group_name=imooc
      url_have_group_name = true
      store0_path=/usr/local/fastdfs/storage
      ```
  
    ```nginx
      mkdir /usr/local/fastdfs/tmp
      ```
  
  * 修改nginx.conf，添加如下虚拟主机：
  
    ```nginx
      server {
          listen       8888;
          server_name  localhost;
      
          location /imooc/M00 {
                  ngx_fastdfs_module;
          }
      
      }
      ```

# FastDFS整合SpringBoot技术落地

* pom依赖

  ```xml
  <!-- fastdfs依赖 -->
  <dependency>
      <groupId>com.github.tobato</groupId>
      <artifactId>fastdfs-client</artifactId>
      <version>1.26.7</version>
  </dependency>
  ```

* 上传接口

  ```java
  public String upload(MultipartFile file, String fileExtName) throws Exception;
  ```

* 接口实现类

  ```java
  @Autowired
  private FastFileStorageClient fastFileStorageClient;
  
  @Override
  public String upload(MultipartFile file, String fileExtName) throws Exception {
  
      StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(),
              file.getSize(),
              fileExtName,
              null);
  
      String path = storePath.getFullPath();
  
      return path;
  }
  ```

* 上传controller

  ```java
  		@ApiOperation(value = "用户头像修改", notes = "用户头像修改", httpMethod = "POST")
      @PostMapping("uploadFace")
      public IMOOCJSONResult uploadFace(
              @ApiParam(name = "userId", value = "用户id", required = true)
                      String userId,
              @ApiParam(name = "file", value = "用户头像", required = true)
                      MultipartFile file,
              HttpServletRequest request,
              HttpServletResponse response) throws Exception {
  
          String path = "";
          // 开始文件上传
          if (file != null) {
              // 获得文件上传的文件名称
              String fileName = file.getOriginalFilename();
              if (StringUtils.isNotBlank(fileName)) {
  
                  // 文件重命名  imooc-face.png -> ["imooc-face", "png"]
                  String fileNameArr[] = fileName.split("\\.");
  
                  // 获取文件的后缀名
                  String suffix = fileNameArr[fileNameArr.length - 1];
  
                  if (!suffix.equalsIgnoreCase("png") &&
                          !suffix.equalsIgnoreCase("jpg") &&
                          !suffix.equalsIgnoreCase("jpeg")) {
                      return IMOOCJSONResult.errorMsg("图片格式不正确！");
                  }
                  path = fileService.upload(file, suffix);
              }
          } else {
              return IMOOCJSONResult.errorMsg("文件不能为空！");
          }
  
          if (StringUtils.isNotBlank(path)) {
              String finalUserFaceUrl = fileResource.getOssHost() + path;
  
              Users userResult = centerUserService.updateUserFace(userId, finalUserFaceUrl);
              userResult = setNullProperty(userResult);
  
              CookieUtils.setCookie(request, response, "user",
                      JsonUtils.objectToJson(userResult), true);
          } else {
              return IMOOCJSONResult.errorMsg("上传头像失败");
          }
          return IMOOCJSONResult.ok();
      }
  ```

# 第三方云存储解决方案-OSS

* 使用OSS优点

  * SDK使用简单
  * 提供强大的文件处理功能
  * 零运维成本
  * 图形化管理控制台
  * CDN加速

* OSS基本配置

  ```properties
  file.host=http://192.168.1.156:8888/
  file.endpoint=oss-cn-beijing.aliyuncs.com
  file.accessKeyId=LTAI4Fo5zZe
  file.accessKeySecret=uTVnXbuTTg
  file.bucketName=imooctest
  file.objectName=foodie-dev/images
  file.ossHost=https://imooctest.oss-cn-beijing.aliyuncs.com/
  ```

* OSS实现文件上传

  ```java
  @Override
  public String uploadOSS(MultipartFile file, String userId, String fileExtName) throws Exception {
  
      // 构建ossClient
      OSS ossClient = new OSSClientBuilder()
              .build(fileResource.getEndpoint(),
                      fileResource.getAccessKeyId(),
                      fileResource.getAccessKeySecret());
  
      InputStream inputStream = file.getInputStream();
  
      String myObjectName = fileResource.getObjectName() + "/" + userId + "/" + userId + "." + fileExtName;
  
      ossClient.putObject(fileResource.getBucketName(), myObjectName, inputStream);
      ossClient.shutdown();
  
      return myObjectName;
  }
  ```

  

