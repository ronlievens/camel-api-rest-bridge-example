package com.github.ronlievens.examples.camel.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
class ApplicationConfiguration implements CamelContextConfiguration {

    @Override
    public void beforeApplicationStart(final CamelContext camelContext) {
        log.info("Starting Camel application");
        camelContext.setTracing(true);
        camelContext.setUseMDCLogging(true);
        camelContext.setUseBreadcrumb(true);
    }

    @Override
    public void afterApplicationStart(final CamelContext camelContext) {
        log.info("Camel application started");
    }
}
