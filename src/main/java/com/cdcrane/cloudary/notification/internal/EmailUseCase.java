package com.cdcrane.cloudary.notification.internal;

public interface EmailUseCase {

    void sendVerificationEmail(String email, String firstName, Integer verificationCode);
}
