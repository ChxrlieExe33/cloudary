package com.cdcrane.cloudary.search;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "files::events",
        "files::api",
        "users::principal"
})
public class SearchModule {
}
