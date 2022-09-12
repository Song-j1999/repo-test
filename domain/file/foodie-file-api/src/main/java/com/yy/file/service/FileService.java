package com.yy.file.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@FeignClient("foodie-file")
@RequestMapping("file")
public interface FileService {

    @RequestMapping("upload")
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam("fileExtName") String fileExtName) throws Exception;

    @RequestMapping("uploadOSS")
    public String uploadOSS(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId, @RequestParam("fileExtName") String fileExtName) throws Exception;
}
