package com.tamp.common.sms;

/**
 * 短信发送服务抽象接口
 * <p>
 * 当前为日志实现，后续对接阿里云/腾讯云短信网关时替换实现即可。
 */
public interface SmsService {

    /**
     * 发送验证码短信
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phone, String code);

    /**
     * 发送初始密码通知短信
     *
     * @param phone           手机号
     * @param initialPassword 初始密码
     * @return 是否发送成功
     */
    boolean sendInitialPassword(String phone, String initialPassword);
}
