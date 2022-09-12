package com.yy.nginxlimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Controller {

    // Nginx专用
    // 1. 修改host文件 -> www.alianlyy-training.top = localhost 127.0.0.1
    //    (127.0.0.1	www.alianlyy-training.top)
    // 2. 修改nginx -> 将步骤1中的域名，添加到路由规则当中
    //    配置文件地址： /usr/local/nginx/conf/nginx.conf
    // 3. 添加配置项：参考resources文件夹下面的nginx.conf
    //
    // 重新加载nginx(Nginx处于启动) => sudo /usr/local/nginx/sbin/nginx -s reload
    @GetMapping("/nginx")
    public String nginx() {
        log.info("Nginx success");
        return "success";
    }

    @GetMapping("/nginx-conn")
    public String nginxConn(@RequestParam(defaultValue = "0") int secs) {
        try {
            Thread.sleep(1000 * secs);
        } catch (Exception e) {
        }
        return "success";
    }
}
