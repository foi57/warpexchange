package com.itranswarp.exchange.message.event;

import com.itranswarp.exchange.model.support.EntitySupport;
import jakarta.persistence.*;

@Entity
@Table(name = "events",uniqueConstraints = @UniqueConstraint(name = "UNI_PREV_ID",columnNames = {"previousId"}))
public class EventEntity implements EntitySupport {
    @Id
    @Column(nullable = false,updatable = false)
    public long sequenceId;

    @Column(nullable = false,updatable = false)
    public long previousId;

    @Column(nullable = false,updatable = false,length = VAR_CHAR_1000)
    public String data;

    @Column(nullable = false,updatable = false)
    public long createdAt;

    @Override
    public String toString() {
        return "EventEntity [sequenceId=" + sequenceId + ", previousId=" + previousId + ", data=" + data
                + ", createdAt=" + createdAt + "]";
    }
}
