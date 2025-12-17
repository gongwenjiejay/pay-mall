package cn.jay.controller;


import cn.jay.common.constants.Constants;
import cn.jay.common.response.Response;
import cn.jay.service.ILoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController {

    @Resource
    private ILoginService loginService;

    /**
     * http://gwj-mall.natapp1.cc/api/v1/login/weixin_qrcode_ticket
     * @return
     */
    @RequestMapping(value = "weixin_qrcode_ticket", method = RequestMethod.GET)
    public Response<String> weixinQrCodeTicket() {
        try {
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("生成微信扫码登录，ticket：{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();
        } catch (Exception e) {
            log.error("创建微信二维码ticket失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * http://gwj-mall.natapp1.cc/api/v1/login/check_login
     */
    @RequestMapping(value = "check_login", method = RequestMethod.GET)
    public Response<String> checkLogin(@RequestParam String ticket) {
        try {
            String openidToken = loginService.checkLogin(ticket);
            log.info("微信扫码登录结果，ticket：{}，openidToken：{}", ticket, openidToken);
            if (StringUtils.isNotBlank(openidToken)) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(openidToken)
                        .build();
            }else {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }
        } catch (Exception e) {
            log.error("微信扫码登录失败 ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
