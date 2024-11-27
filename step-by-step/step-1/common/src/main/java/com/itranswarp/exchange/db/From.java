package com.itranswarp.exchange.db;

import java.util.List;

public final class From<T> extends CriteriaQuery<T>{
    From(Criteria<T> criteria ,Mapper<T> mapper){
        super(criteria);
        this.criteria.mapper = mapper;
        this.criteria.clazz = mapper.entityClass;
        this.criteria.table = mapper.tableName;
    }

    public Where<T> where(String clause, Object... args) {
        return new Where<>(this.criteria, clause, args);
    }


    public OrderBy<T> orderBy(String orderBy) {
        return new OrderBy<>(this.criteria, orderBy);
    }


    public Limit<T> limit(int maxResults) {
        return limit(0, maxResults);
    }


    public Limit<T> limit(int offset, int maxResults) {
        return new Limit<>(this.criteria, offset, maxResults);
    }


    public List<T> list() {
        return this.criteria.list();
    }


    public T first() {
        return this.criteria.first();
    }


    public T unique() {
        return this.criteria.unique();
    }
}
