package com.atguigu.lease.web.app.service;

public interface SmsService {
    void sendSmsVerifyCode(String phone, String code) throws Exception;

}
