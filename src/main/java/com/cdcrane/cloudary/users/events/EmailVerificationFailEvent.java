package com.cdcrane.cloudary.users.events;

public record EmailVerificationFailEvent(String email, String username, Integer newVerificationCode) {
}
