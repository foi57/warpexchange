package com.itranswarp.exchange.db;

import java.util.Arrays;

public final class Select extends CriteriaQuery{

    Select(Criteria criteria,String... selectFields){
        super(criteria);
        if (selectFields.length > 0){
            this.criteria.select = Arrays.asList(selectFields);
        }
    }
    public <T> From<T> from(Class<T> entityClass) {
        return new From<T>(this.criteria, this.criteria.db.getMapper(entityClass));
    }
}

