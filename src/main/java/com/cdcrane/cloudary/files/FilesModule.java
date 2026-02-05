package com.cdcrane.cloudary.files;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "users::api",
        "users::principal"
})
public class FilesModule {
}
