package com.coffeeshop.backend.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.data.domain.PageImpl;

public class PageModule extends SimpleModule {
    public PageModule() {
        super("PageModule");
        addDeserializer(PageImpl.class, new PageImplDeserializer());
    }
}
