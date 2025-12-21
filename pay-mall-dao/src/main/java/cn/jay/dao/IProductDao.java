package cn.jay.dao;

import cn.jay.domain.po.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IProductDao {

    /**
     * 根据商品ID查询商品（仅查询上架商品）
     */
    Product queryProductByProductId(@Param("productId") String productId);

    List<Product> queryProductList();
}
