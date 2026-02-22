package com.ls.lease.web.app;

import com.ls.lease.web.app.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmsTest {

    @Autowired
    private SmsService smsService;

    @Test
    public void testSendSms() throws Exception {
        // 1. 把这里的手机号改成你自己的
        // 2. 确保你已经在 C:\Users\用户名\.aliyun\credentials 配置好了密钥
        smsService.sendSmsVerifyCode("13031982763", "123456");
    }
}