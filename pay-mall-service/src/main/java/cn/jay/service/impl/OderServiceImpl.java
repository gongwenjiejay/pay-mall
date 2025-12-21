package cn.jay.service.impl;

import cn.jay.common.constants.Constants;
import cn.jay.dao.IOrderDao;
import cn.jay.dao.IProductDao;
import cn.jay.domain.po.PayOrder;
import cn.jay.domain.po.Product;
import cn.jay.domain.req.ShopCartReq;
import cn.jay.domain.res.PayOrderRes;
import cn.jay.domain.vo.ProductVO;
import cn.jay.service.IOrderService;
import cn.jay.service.rpc.ProductRPC;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class OderServiceImpl implements IOrderService {

    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;

    @Resource
    private IOrderDao orderDao;

    @Resource
    private IProductDao productDao;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private EventBus eventBus;

    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception {
        //1. 查询当前用户是否存在未支付订单或掉单订单
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setUserId(shopCartReq.getUserId());
        payOrderReq.setProductId(shopCartReq.getProductId());

        PayOrder unpaidOrder = orderDao.queryUnPayOrder(payOrderReq);

        if (null !=unpaidOrder && Constants.OrderStatusEnum.PAY_WAIT.getCode().equals(unpaidOrder.getStatus())){
            log.info("创建订单-存在，已存在未支付订单。userId={} productId={} orderId={}",shopCartReq.getUserId(),shopCartReq.getProductId(),unpaidOrder.getOrderId());
            return PayOrderRes.builder()
                    .orderId(unpaidOrder.getOrderId())
                    .payUrl(unpaidOrder.getPayUrl())
                    .build();
        } else if (null != unpaidOrder && Constants.OrderStatusEnum.CREATE.getCode().equals(unpaidOrder.getStatus())){
            PayOrder payOrder = doPrepayOrder(unpaidOrder.getProductId(), unpaidOrder.getProductName(), unpaidOrder.getOrderId(), unpaidOrder.getTotalAmount());
            return PayOrderRes.builder()
                    .orderId(payOrder.getOrderId())
                    .payUrl(payOrder.getPayUrl())
                    .build();
        }

        // 2. 查询商品 & 创建订单
        Product product = productDao.queryProductByProductId(shopCartReq.getProductId());
        log.info("下单商品: id={}, name={}, price={}", product.getProductId(), product.getProductName(), product.getPrice());
        if (product == null) {
            throw new RuntimeException("商品不存在或已下架");
        }

        String orderId = RandomStringUtils.randomNumeric(16);

        orderDao.insert(PayOrder.builder()
                .userId(shopCartReq.getUserId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .orderId(orderId)
                .totalAmount(product.getPrice())
                .orderTime(new Date())
                .status(Constants.OrderStatusEnum.PAY_WAIT.getCode())
                .build());

        // 3. 创建支付单
        PayOrder payOrder = doPrepayOrder(product.getProductId(), product.getProductName(), orderId, product.getPrice());
        return PayOrderRes.builder()
                        .orderId(orderId)
                        .payUrl(payOrder.getPayUrl())
                        .build();

    }

    @Override
    public void changeOrderPaySuccess(String orderId) {
        PayOrder payOrderReq = new PayOrder();
        payOrderReq.setOrderId(orderId);
        payOrderReq.setStatus(Constants.OrderStatusEnum.PAY_SUCCESS.getCode());
        orderDao.changeOrderPaySuccess(payOrderReq);

        eventBus.post(JSON.toJSONString(payOrderReq));
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }

    private PayOrder doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", totalAmount.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setPayUrl(form);
        payOrder.setStatus(Constants.OrderStatusEnum.PAY_WAIT.getCode());

        orderDao.updateOrderPayInfo(payOrder);

        return payOrder;
    }
}
