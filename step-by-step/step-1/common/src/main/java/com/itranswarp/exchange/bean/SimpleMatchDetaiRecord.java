package com.itranswarp.exchange.bean;

import com.itranswarp.exchange.enums.MatchType;

import java.math.BigDecimal;

public record SimpleMatchDetaiRecord(BigDecimal price, BigDecimal quantity, MatchType type) {
}
