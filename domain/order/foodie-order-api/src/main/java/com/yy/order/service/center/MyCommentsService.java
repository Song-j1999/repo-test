package com.yy.order.service.center;

import com.yy.order.pojo.OrderItems;
import com.yy.order.pojo.bo.center.OrderItemsCommentBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("foodie-order-service")
@RequestMapping("order-comments-api")
public interface MyCommentsService {

    /**
     * 根据订单id查询关联的商品
     * @param orderId
     * @return
     */
    @GetMapping("orderItems")
    public List<OrderItems> queryPendingComment(@RequestParam("orderId") String orderId);

    /**
     * 保存用户的评论
     * @param orderId
     * @param userId
     * @param commentList
     */
    @PostMapping("saveOrderComments")
    public void saveComments(@RequestParam("orderId") String orderId,
                             @RequestParam("userId") String userId,
                             @RequestBody List<OrderItemsCommentBO> commentList);


    // TODO 移到了itemCommentsService里
//    /**
//     * 我的评价查询 分页
//     * @param userId
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize);
}
