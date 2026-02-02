package com.cdcrane.cloudary;


import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTest {

    @Test
    public void testModularity() {

        ApplicationModules modules = ApplicationModules.of(CloudaryApplication.class);

        modules.verify();

        System.out.println(modules);

    }
}
