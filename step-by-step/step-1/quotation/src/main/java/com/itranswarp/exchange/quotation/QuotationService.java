package com.itranswarp.exchange.quotation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.itranswarp.exchange.enums.BarType;
import com.itranswarp.exchange.message.AbstractMessage;
import com.itranswarp.exchange.message.TickMessage;
import com.itranswarp.exchange.messaging.MessageConsumer;
import com.itranswarp.exchange.messaging.Messaging;
import com.itranswarp.exchange.messaging.MessagingFactory;
import com.itranswarp.exchange.model.quotation.*;
import com.itranswarp.exchange.model.support.AbstractBarEntity;
import com.itranswarp.exchange.redis.RedisCache;
import com.itranswarp.exchange.redis.RedisService;
import com.itranswarp.exchange.support.LoggerSupport;
import com.itranswarp.exchange.util.IpUtil;
import com.itranswarp.exchange.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;
@Component
public class QuotationService extends LoggerSupport {

    @Autowired
    private ZoneId zoneId;

    @Autowired
    private RedisService redisService;

    @Autowired
    QuotationDbService quotationDbService;

    @Autowired
    private MessagingFactory messagingFactory;


    private MessageConsumer tickConsumer;

    private String shaUpdateRecentTicksLua = null;

    private String shaUpdateBarLua = null;

    private long sequenceId;

    @PostConstruct
    public void init() throws Exception{
        this.shaUpdateRecentTicksLua = this.redisService.loadScriptFromClassPath("/redis/update-recent-ticks.lua");
        this.shaUpdateBarLua = this.redisService.loadScriptFromClassPath("/redis/update-bar.lua");

        String groupId = Messaging.Topic.TICK.name() + "-" + IpUtil.getHostId();
        this.tickConsumer =  messagingFactory.createBatchMessageListener(Messaging.Topic.TICK,groupId,this::processMessages);
    }

    @PreDestroy
    public void shutdown(){
        if (this.tickConsumer !=null){
            this.tickConsumer.stop();
            this.tickConsumer = null;
        }
    }

    public void processMessages(List<AbstractMessage> messages){
        for (AbstractMessage message : messages){
            processMessages((TickMessage)messages);
        }
    }

    void processMessages(TickMessage message){
        if (message.sequenceId < this.sequenceId){
            return;
        }
        if (logger.isDebugEnabled()){
            logger.debug("process ticks: sequenceId = {}, {} ticks...", message.sequenceId, message.ticks.size());
        }

        this.sequenceId = message.sequenceId;
        final long createdAt = message.createdAt;
        StringJoiner tickStrJoiner = new StringJoiner(",","[","]");
        StringJoiner ticksJoiner = new StringJoiner(",","[","]");
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal highPrice = BigDecimal.ZERO;
        BigDecimal lowPrice = BigDecimal.ZERO;
        BigDecimal closePrice = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;
        for (TickEntity tick : message.ticks){
            String json = tick.toJson();
            tickStrJoiner.add("\"" + json + "\"");
            ticksJoiner.add(json);
            if (openPrice.signum() == 0){
                openPrice = tick.price;
                closePrice = tick.price;
                highPrice = tick.price;
                lowPrice = tick.price;
            }else {
                closePrice = tick.price;
                highPrice = highPrice.max(tick.price);
                lowPrice = lowPrice.min(tick.price);
            }
            quantity = quantity.add(tick.quantity);
        }

        long sec = createdAt / 1000;
        long min = sec / 60;
        long hour = min / 60;
        long secStartTime = sec * 1000;
        long minStartTime = min * 60 * 1000;
        long hourStartTime = hour * 60 * 60 * 1000;
        long datStartTime = Instant.ofEpochMilli(hourStartTime).atZone(zoneId).withHour(0).toEpochSecond() * 1000;

        String ticksData = ticksJoiner.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("generated ticks data: {}", ticksData);
        }
        Boolean tickOk = redisService.executeScriptReturnBoolean(this.shaUpdateRecentTicksLua,new String[]{RedisCache.Key.RECENT_TICKS},
                new String[] {String.valueOf(this.sequenceId), ticksData,tickStrJoiner.toString()});
        if (!tickOk.booleanValue()){
            logger.warn("ticks are ignored by Redis.");
            return;
        }

        this.quotationDbService.saveTicks(message.ticks);

        String strCreatedBars = redisService.executeScriptReturnString(this.shaUpdateBarLua,new String[] { RedisCache.Key.SEC_BARS,RedisCache.Key.MIN_BARS,RedisCache.Key.HOUR_BARS,RedisCache.Key.DAY_BARS},
                new String[]{
                        String.valueOf(this.sequenceId),
                        String.valueOf(secStartTime),
                        String.valueOf(minStartTime),
                        String.valueOf(hourStartTime),
                        String.valueOf(datStartTime),
                        String.valueOf(openPrice),
                        String.valueOf(highPrice),
                        String.valueOf(lowPrice),
                        String.valueOf(closePrice),
                        String.valueOf(quantity)
                });
        logger.info("returned created bars: " + strCreatedBars);
        Map<BarType,BigDecimal[]> barMap = JsonUtil.readJson(strCreatedBars,TYPE_BARS);
        if (!barMap.isEmpty()){
            SecBarEntity secBar = createBar(SecBarEntity::new,barMap.get(BarType.SEC));
            MinBarEntity minBar = createBar(MinBarEntity::new,barMap.get(BarType.MIN));
            HourBarEntity hourBar = createBar(HourBarEntity::new,barMap.get(BarType.HOUR));
            DayBarEntity dayBar = createBar(DayBarEntity::new,barMap.get(BarType.DAY));
            this.quotationDbService.saveBars(secBar,minBar,hourBar,dayBar);
        }
    }

    static <T extends AbstractBarEntity> T createBar(Supplier<T> fn,BigDecimal[] data){
        if (data == null){
            return null;
        }
        T t = fn.get();
        t.startTime = data[0].longValue();
        t.openPrice = data[1];
        t.lowPrice = data[2];
        t.closePrice = data[3];
        t.closePrice = data[4];
        t.quantity = data[5];
        return t;
    }

    private static final TypeReference<Map<BarType,BigDecimal[]>> TYPE_BARS =new TypeReference<>() {
    };
}
