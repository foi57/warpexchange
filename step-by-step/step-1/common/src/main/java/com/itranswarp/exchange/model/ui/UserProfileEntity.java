package com.itranswarp.exchange.model.ui;

import com.itranswarp.exchange.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class UserProfileEntity implements EntitySupport {
    @Id
    @Column(nullable = false,updatable = false)
    public Long userId;

    @Column(nullable = false,updatable = false,length = VAR_CHAR_100)
    public String email;

    @Column(nullable = false,length = VAR_CHAR_100)
    public String name;

    @Column(nullable = false,updatable = false)
    public long createdAt;

    @Column(nullable = false)
    public long updatedAt;

    @Override
    public String toString() {
        return "UserProfileEntity{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
