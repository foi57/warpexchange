package com.itranswarp.exchange.model.support;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
@MappedSuperclass
public class AbstractBarEntity implements EntitySupport{

    @Id
    @Column(nullable = false,updatable = false)
    public long startTime;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal openPrice;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal highPrice;

    @Column(nullable = false,updatable = false,precision =  PRECISION,scale = SCALE)
    public BigDecimal lowPrice;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal closePrice;

    @Column(nullable = false,updatable = false,precision = PRECISION,scale = SCALE)
    public BigDecimal quantity;

    @Transient
    public Number[] getBarData(){
        return new Number[]{startTime,openPrice,highPrice,lowPrice,closePrice,quantity};
    }

    public String toString(ZoneId zoneId){
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.startTime),zoneId);
        String time = FORMATTERS.get(getClass().getSimpleName()).format(zdt);
        return String.format("{%s:startTime=%s,O=%s,H=%s,L=%s,C=%S,qty=%s}",this.getClass().getSimpleName(),time,this.openPrice,this.highPrice,this.lowPrice,this.closePrice,this.quantity);
    }

    static final Map<String, DateTimeFormatter> FORMATTERS = Map.of(
            "SecBarEntity",DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US),
            "MinBarEntity",DateTimeFormatter.ofPattern("dd HH:mm", Locale.US),
            "HourBarEntity",DateTimeFormatter.ofPattern("MM-dd HH",Locale.US),
            "DayBarEntity",DateTimeFormatter.ofPattern("yy-MM-dd",Locale.US)
    );
}
