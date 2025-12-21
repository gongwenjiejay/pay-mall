package cn.jay.service.impl;

import cn.jay.dao.IProductDao;
import cn.jay.domain.po.Product;
import cn.jay.service.IProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Resource
    private IProductDao productDao;

    @Override
    public List<Product> queryProductList() {
        return productDao.queryProductList();
    }
}

