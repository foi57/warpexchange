package com.itranswarp.exchange.support;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterConfig;

public abstract class AbstractFilter extends LoggerSupport implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws SecurityException{
        logger.info("init filter: {}...", getClass().getName());
    }

    @Override
    public void destroy() {
        logger.info("destroy filter: {}...", getClass().getName());
    }
}
