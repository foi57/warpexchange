package com.itranswarp.exchange.support;

import com.itranswarp.exchange.db.DbTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDbService extends LoggerSupport{
    @Autowired
    protected DbTemplate db;
}
