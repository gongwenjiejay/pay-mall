package cn.jay.service;

import cn.jay.domain.req.ShopCartReq;
import cn.jay.domain.res.PayOrderRes;

public interface IOrderService {

    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;
}
