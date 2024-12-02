package com.itranswarp.exchange.model.trade;

import com.itranswarp.exchange.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "unique_event")
public class UniqueEventEntity implements EntitySupport {
    @Id
    @Column(nullable = false,updatable = false,length = VAR_CHAR_50)
    public String uniqueId;

    @Column(nullable = false,updatable = false)
    public long sequenceId;

    @Column(nullable = false,updatable = false)
    public long createdAt;

    @Override
    public String toString() {
        return "UniqueEventEntity{" +
                "uniqueId='" + uniqueId + '\'' +
                ", sequenceId=" + sequenceId +
                ", createdAt=" + createdAt +
                '}';
    }
}
