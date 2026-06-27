package com.tamp.common.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 默认短信服务实现 — 日志打印（Mock）
 * <p>
 * 当容器中不存在真实的短信网关实现时，使用此实现。
 * 生产环境对接阿里云/腾讯云后，注册新的 SmsService Bean 并标注 @Primary 即可覆盖此默认实现。
 */
@Service
@Primary
@ConditionalOnMissingBean(name = "realSmsService")
public class LogSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(LogSmsService.class);

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        log.info("[SMS] 验证码短信 | 手机号={} | 验证码={}", phone, code);
        return true;
    }

    @Override
    public boolean sendInitialPassword(String phone, String initialPassword) {
        log.info("[SMS] 初始密码短信 | 手机号={} | 初始密码={}", phone, initialPassword);
        return true;
    }
}
