package cn.jay.service;

import cn.jay.domain.po.Product;

import java.util.List;

public interface IProductService {

    /**
     * 查询所有可售商品
     */
    List<Product> queryProductList();
}