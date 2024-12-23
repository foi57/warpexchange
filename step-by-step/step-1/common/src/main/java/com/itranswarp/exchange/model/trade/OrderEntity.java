package com.itranswarp.exchange.model.trade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itranswarp.exchange.enums.Direction;
import com.itranswarp.exchange.enums.OrderStatus;
import com.itranswarp.exchange.model.support.EntitySupport;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order")
public class OrderEntity implements EntitySupport,Comparable<OrderEntity> {

    @Id
    @Column(nullable = false,updatable = false)
    public Long id;

    @Column(nullable = false,updatable = false)
    public long sequenceId;

    @Column(nullable = false,updatable = false,length = VAR_ENUM)
    public Direction direction;

    @Column(nullable = false,updatable = false)
    public Long userId;

    @Column(nullable = false,updatable = false,length = VAR_ENUM)
    public OrderStatus status;

    public void updateOrder(BigDecimal unfilledQuantity, OrderStatus status, long updatedAt) {
        this.version++;
        this.unfilledQuantity = unfilledQuantity;
        this.status = status;
        this.updatedAt = updatedAt;
        this.version++;
    }

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal price;

    @Column(nullable = false,updatable = false)
    public long createdAt;

    @Column(nullable = false,updatable = false)
    public long updatedAt;

    private int version;

    @Transient
    @JsonIgnore
    public int getVersion() {
        return version;
    }

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal quantity;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal unfilledQuantity;

    @Nullable
    public OrderEntity copy(){
        OrderEntity entity =new OrderEntity();
        int ver = this.version;
        entity.status = this.status;
        entity.unfilledQuantity=this.unfilledQuantity;
        entity.updatedAt = this.updatedAt;
        if (ver !=this.version){
            return null;
        }
        entity.createdAt = this.createdAt;
        entity.direction = this.direction;
        entity.price = this.price;
        entity.quantity = this.quantity;
        entity.sequenceId = this.sequenceId;
        entity.userId = this.userId;
        entity.id = this.id;
        return entity;
    }
    public boolean equals(Object o){
        if (this == o) return true;
        if (o instanceof OrderEntity) {
            OrderEntity e=(OrderEntity) o;
            return this.id.longValue() == e.id.longValue();
        }
        return false;
    }

    @Override
    public String toString() {
        return "OrderEntity{" +
                "id=" + id +
                ", sequenceId=" + sequenceId +
                ", direction=" + direction +
                ", userId=" + userId +
                ", status=" + status +
                ", price=" + price +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                ", quantity=" + quantity +
                ", unfilledQuantity=" + unfilledQuantity +
                '}';
    }
    public int compareTo(OrderEntity o) {
        return Long.compare(this.id.longValue(),o.id.longValue());
    }
}
