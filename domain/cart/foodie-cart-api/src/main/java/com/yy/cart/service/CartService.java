package com.yy.cart.service;

import com.yy.pojo.ShopcartBO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("cart-api")
public interface CartService {

    /**
     * 添加商品到购物车
     */
    @PostMapping("addItem")
    public boolean addItemToCart(@RequestParam("userId") String userId,
                                 @RequestBody ShopcartBO shopcartBO);

    /**
     * 删除商品到购物车
     */
    @PostMapping("removeItem")
    public boolean removeItemFromCart(@RequestParam("userId") String userId,
                                      @RequestParam("itemSpecId") String itemSpecId);

    /**
     * 一键清除购物车
     */
    @PostMapping("clearCart")
    public boolean clearCart(@RequestParam("userId") String userId);

}
