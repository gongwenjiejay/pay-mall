package cn.jay.controller;

import cn.jay.common.constants.Constants;
import cn.jay.common.response.Response;
import cn.jay.domain.po.Product;
import cn.jay.domain.vo.ProductVO;
import cn.jay.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/product/")
public class ProductController {

    @Resource
    private IProductService productService;

    /**
     * 商品列表接口
     * http://localhost:8080/api/v1/product/list
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public Response<List<ProductVO>> queryProductList() {
        try {
            log.info("查询商品列表开始");
            List<Product> products = productService.queryProductList();

            List<ProductVO> productVOList = products.stream().map(product -> {
                ProductVO vo = new ProductVO();
                vo.setProductId(product.getProductId());
                vo.setProductName(product.getProductName());
                vo.setProductDesc(product.getProductDesc());
                vo.setPrice(product.getPrice());
                vo.setImg(product.getProductImg()); // 数据库中的图片URL
                return vo;
            }).collect(Collectors.toList());

            log.info("查询商品列表完成，数量: {}", productVOList.size());
            return Response.<List<ProductVO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(productVOList)
                    .build();
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return Response.<List<ProductVO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
