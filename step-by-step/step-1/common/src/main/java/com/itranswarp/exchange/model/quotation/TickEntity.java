package com.itranswarp.exchange.model.quotation;

import com.itranswarp.exchange.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

public class TickEntity implements EntitySupport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false,updatable = false)
    public long id;

    @Column(nullable = false,updatable = false)
    public  long sequenceId;

    @Column(nullable = false,updatable = false)
    public long takerOrderId;

    @Column(nullable = false,updatable = false)
    public long makerOrderId;

    @Column(nullable = false,updatable = false)
    public boolean takerDirection;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal price;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal quantity;

    @Column(nullable = false,updatable = false)
    public long createdAt;

    public String toJson() {
        return "[" + createdAt + "," + (takerDirection ? 1 : 0) + "," + price + "," + quantity + "]";
    }
}
