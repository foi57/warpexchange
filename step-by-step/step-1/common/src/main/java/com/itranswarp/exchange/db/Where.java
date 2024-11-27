package com.itranswarp.exchange.db;

import java.util.ArrayList;
import java.util.List;

public final class Where<T> extends CriteriaQuery<T>{

    Where(Criteria<T> criteria,String clause,Object... params) {
        super(criteria);
        this.criteria.where = clause;
        this.criteria.whereParams = new ArrayList<>();
        for (Object param : params) {
            this.criteria.whereParams.add(param);
        }
    }
    public Limit<T> limit(int maxResults) {
        return limit(0, maxResults);
    }

    public Limit<T> limit(int offset, int maxResults) {
        return new Limit<>(this.criteria, offset, maxResults);
    }

    public OrderBy<T> orderBy(String orderBy) {
        return new OrderBy<>(this.criteria, orderBy);
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
