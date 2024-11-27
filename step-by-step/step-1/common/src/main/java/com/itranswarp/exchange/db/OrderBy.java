package com.itranswarp.exchange.db;

import java.util.ArrayList;
import java.util.List;

public final class OrderBy<T> extends CriteriaQuery<T>{

    public OrderBy(Criteria<T> criteria,String orderBy) {
        super(criteria);
        orderBy(orderBy);
    }

    public OrderBy<T> orderBy(String orderBy){
        if (criteria.orderBy == null) {
            criteria.orderBy = new ArrayList<String>();
        }
        criteria.orderBy.add(orderBy);
        return this;
    }

    public OrderBy<T> desc(){
        int last = this.criteria.orderBy.size()-1;
        String s = criteria.orderBy.get(last);
        if (!s.toUpperCase().endsWith(" DESC")){
            s = s + " DESC";
        }
        criteria.orderBy.set(last,s);
        return this;
    }


    public Limit<T> limit(int maxResults) {
        return limit(0, maxResults);
    }
    {
        criteria.orderBy = new ArrayList<>();
    }

    public Limit<T> limit(int offset, int maxResults) {
        return new Limit<>(this.criteria, offset, maxResults);
    }


    public List<T> list() {
        return criteria.list();
    }


    public T first() {
        return criteria.first();
    }
}
