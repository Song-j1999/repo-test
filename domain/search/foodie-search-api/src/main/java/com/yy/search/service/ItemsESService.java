package com.yy.search.service;


import com.yy.pojo.PagedGridResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("search-api")
public interface ItemsESService {

    @GetMapping("searhItems")
    public PagedGridResult searhItems(@RequestParam("keywords") String keywords,
                                      @RequestParam("sort") String sort,
                                      @RequestParam("page") Integer page,
                                      @RequestParam("pageSize") Integer pageSize);

}
