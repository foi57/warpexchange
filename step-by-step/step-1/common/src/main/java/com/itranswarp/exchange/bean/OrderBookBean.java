package com.itranswarp.exchange.bean;

import java.math.BigDecimal;
import java.util.List;

public class OrderBookBean {

    public long sequenceId;

    public BigDecimal price;

    public List<OrderBookItemBean> buy;

    public List<OrderBookItemBean> sell;

    public  OrderBookBean(long sequenceId,BigDecimal price,List<OrderBookItemBean> buy,List<OrderBookItemBean> sell){
        this.sequenceId = sequenceId;
        this.price = price;
        this.buy = buy;
        this.sell =sell;
    }
}
