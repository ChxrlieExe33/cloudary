package com.cdcrane.cloudary.search;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {
        "files::events",
        "files::api"
})
public class SearchModule {
}
