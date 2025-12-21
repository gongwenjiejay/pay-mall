package cn.jay.service;

import cn.jay.domain.req.ShopCartReq;
import cn.jay.domain.res.PayOrderRes;

import java.util.List;

public interface IOrderService {

    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;

    void changeOrderPaySuccess(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);
}
