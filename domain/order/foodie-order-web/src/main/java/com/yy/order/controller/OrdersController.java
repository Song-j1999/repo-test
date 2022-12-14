package com.yy.order.controller;

import com.yy.controller.BaseController;
import com.yy.enums.OrderStatusEnum;
import com.yy.enums.PayMethod;
import com.yy.order.pojo.OrderStatus;
import com.yy.order.pojo.bo.OrderStatusCheckBO;
import com.yy.order.pojo.bo.PlaceOrderBO;
import com.yy.order.pojo.bo.SubmitOrderBO;
import com.yy.order.pojo.vo.MerchantOrdersVO;
import com.yy.order.pojo.vo.OrderVO;
import com.yy.order.service.OrderService;
import com.yy.order.stream.CheckOrderTopic;
import com.yy.pojo.IMOOCJSONResult;
import com.yy.pojo.ShopcartBO;
import com.yy.utils.CookieUtils;
import com.yy.utils.JsonUtils;
import com.yy.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Api(value = "订单相关", tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {

    final static Logger logger = LoggerFactory.getLogger(OrdersController.class);

    @Resource
    private OrderService orderService;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RedisOperator redisOperator;

    @Resource
    private CheckOrderTopic orderStatusProducer;

    @Autowired
    private RedissonClient redissonClient;

    @ApiOperation(value = "获取订单token", notes = "获取订单token", httpMethod = "POST")
    @PostMapping("/getOrderToken")
    public IMOOCJSONResult getOrderToken(HttpSession session) {
        String token = UUID.randomUUID().toString();
        redisOperator.set("ORDER_TOKEN" + session.getId(), token, 600);
        return IMOOCJSONResult.ok(token);
    }

    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    public IMOOCJSONResult create(
            @RequestBody SubmitOrderBO submitOrderBO,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 订单key
        String orderTokenKey = "ORDER_TOKEN" + request.getSession().getId();
        // 分布式锁key
        String lockKey = "LOCK_KEY" + request.getSession().getId();
        // 获取分布式锁
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(5, TimeUnit.SECONDS);

        try {
            // 从redis获取token
            String orderToken = redisOperator.get(orderTokenKey);
            if (StringUtils.isEmpty(orderToken)) {
                return IMOOCJSONResult.errorMsg("orderToken不存在！");
            }
            if (!orderToken.equals(submitOrderBO.getToken())) {
                return IMOOCJSONResult.errorMsg("orderToken不正确！");
            }
            // 获取完 删除redis中的订单token
            redisOperator.del(orderTokenKey);
        } finally {
            try {
                // 释放锁
                lock.unlock();
            } catch (Exception e) {

            }
        }

        if (submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type
                && submitOrderBO.getPayMethod() != PayMethod.ALIPAY.type) {
            return IMOOCJSONResult.errorMsg("支付方式不支持！");
        }


        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
        if (StringUtils.isBlank(shopcartJson)) {
            return IMOOCJSONResult.errorMsg("购物数据不正确");
        }

        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

        // 1. 创建订单
        PlaceOrderBO orderBO = new PlaceOrderBO(submitOrderBO, shopcartList);
        OrderVO orderVO = orderService.createOrder(orderBO);
        String orderId = orderVO.getOrderId();

        // 2. 创建订单以后，移除购物车中已结算（已提交）的商品
        /**
         * 1001
         * 2002 -> 用户购买
         * 3003 -> 用户购买
         * 4004
         */
        // 清理覆盖现有的redis汇总的购物数据
        shopcartList.removeAll(orderVO.getToBeRemovedShopcatdList());
        redisOperator.set(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(), JsonUtils.objectToJson(shopcartList));
        // 整合redis之后，完善购物车中的已结算商品清除，并且同步到前端的cookie
        CookieUtils.setCookie(request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartList), true);

        // order status检查
        OrderStatusCheckBO msg = new OrderStatusCheckBO();
        msg.setOrderID(orderId);
        // 可以采用更短的Delay时间, 在consumer里面重新投递消息
        orderStatusProducer.output().send(
                MessageBuilder.withPayload(msg)
                        .setHeader("x-delay", 3600 * 24 * 1000 + 300 * 1000)
                        .build()
        );

        // 3. 向支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);

        // 为了方便测试购买，所以所有的支付金额都统一改为1分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("imoocUserId", "imooc");
        headers.add("password", "imooc");

        HttpEntity<MerchantOrdersVO> entity =
                new HttpEntity<>(merchantOrdersVO, headers);

        ResponseEntity<IMOOCJSONResult> responseEntity =
                restTemplate.postForEntity(paymentUrl,
                        entity,
                        IMOOCJSONResult.class);
        IMOOCJSONResult paymentResult = responseEntity.getBody();
        if (paymentResult.getStatus() != 200) {
            logger.error("发送错误：{}", paymentResult.getMsg());
            return IMOOCJSONResult.errorMsg("支付中心订单创建失败，请联系管理员！");
        }

        return IMOOCJSONResult.ok(orderId);
    }

    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId) {
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }

    @PostMapping("getPaidOrderInfo")
    public IMOOCJSONResult getPaidOrderInfo(String orderId) {

        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        return IMOOCJSONResult.ok(orderStatus);
    }
}
