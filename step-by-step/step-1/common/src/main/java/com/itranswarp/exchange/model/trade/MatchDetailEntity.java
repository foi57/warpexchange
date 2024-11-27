package com.itranswarp.exchange.model.trade;

import com.itranswarp.exchange.enums.Direction;
import com.itranswarp.exchange.enums.MatchType;
import com.itranswarp.exchange.model.support.EntitySupport;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "match_detail",uniqueConstraints = @UniqueConstraint(name = "UNI_OID_COID",columnNames = {"orderId","counterOrderId"}), indexes = @Index(name = "IDX_OID_CT",columnList = "orderId,createdAt"))
public class MatchDetailEntity implements EntitySupport,Comparable<MatchDetailEntity>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false,updatable = false)
    public long id;

    @Column(nullable = false,updatable = false)
    public  long sequenceId;

    @Column(nullable = false,updatable = false)
    public Long orderId;

    @Column(nullable = false,updatable = false)
    public Long counterOrderId;

    @Column(nullable = false,updatable = false)
    public Long userId;

    @Column(nullable = false,updatable = false)
    public Long counterUserId;

    @Column(nullable = false,updatable = false,length = VAR_ENUM)
    public MatchType type;

    @Column(nullable = false,updatable = false,length = VAR_ENUM)
    public Direction direction;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal price;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal quantity;

    @Column(nullable = false,updatable = false)
    public long createdAt;

    @Override
    public int compareTo(MatchDetailEntity o){
        int cmp = Long.compare(this.orderId.longValue(),o.orderId.longValue());
        if (cmp == 0){
            cmp =Long.compare(this.counterOrderId.longValue(),o.orderId.longValue());
            return cmp;
        }
        return cmp;
    }
}
