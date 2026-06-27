package com.tamp.auth.repository;

import com.tamp.auth.entity.SmsVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsCodeRepository extends JpaRepository<SmsVerificationCode, Long> {

    Optional<SmsVerificationCode> findFirstByPhoneAndTypeOrderByCreatedAtDesc(String phone, String type);
}
