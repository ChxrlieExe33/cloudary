package com.cdcrane.cloudary.auth;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "users::api",
        "users::principal",
        "users::dto"
})
public class AuthModule {
}
