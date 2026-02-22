package com.atguigu.lease.common.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * 阿里云短信客户端配置
 */
@Configuration
@EnableConfigurationProperties(AliyunSMSProperties.class) // 启用配置属性
public class AliyunSMSConfiguration {

    @Resource
    private AliyunSMSProperties smsProperties;

    /**
     * 创建短信客户端（单例，避免重复创建）
     */
    @Bean
    @ConditionalOnMissingBean // 防止重复注入
    public Client aliyunSmsClient() throws Exception {
        // 1. 初始化配置
        Config config = new Config()
                .setAccessKeyId(smsProperties.getAccessKeyId())
                .setAccessKeySecret(smsProperties.getAccessKeySecret())
                .setEndpoint(smsProperties.getEndpoint());

        // 2. 创建并返回客户端
        return new Client(config);
    }
}
