package com.itranswarp.exchange.model.ui;

import com.itranswarp.exchange.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name =  "password_auths")
public class PasswordAuthEntity implements EntitySupport {
    @Id
    @Column(nullable = false,updatable = false)
    public Long userId;

    @Column(nullable = false,updatable = false,length = VAR_ENUM)
    public String random;

    @Column(nullable = false,length = VAR_CHAR_100)
    public String passwd;
}
