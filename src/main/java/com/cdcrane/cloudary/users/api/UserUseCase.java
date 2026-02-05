package com.cdcrane.cloudary.users.api;

import com.cdcrane.cloudary.users.dto.RegisterAccountRequest;
import com.cdcrane.cloudary.users.dto.UserDTO;
import com.cdcrane.cloudary.users.dto.VerifyEmailRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserUseCase extends UserDetailsService {

    UserDTO findById(UUID id);

    UserDTO findByUsernameOrEmail(String usernameOrEmail);

    void registerUser(RegisterAccountRequest request);

    void handleEmailVerification(VerifyEmailRequest request);

    Map<UUID, Boolean> checkUsersExistByIds(List<UUID> ids);
}
