package com.itranswarp.exchange;

import com.itranswarp.exchange.assets.AssetService;
import com.itranswarp.exchange.clearing.ClearingService;
import com.itranswarp.exchange.enums.AssetEnum;
import com.itranswarp.exchange.enums.Direction;
import com.itranswarp.exchange.enums.UserType;
import com.itranswarp.exchange.match.MatchEngine;
import com.itranswarp.exchange.message.event.AbstractEvent;
import com.itranswarp.exchange.message.event.OrderCancelEvent;
import com.itranswarp.exchange.message.event.OrderRequestEvent;
import com.itranswarp.exchange.message.event.TransferEvent;
import com.itranswarp.exchange.order.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class testTradingEngine {
    static final Long USER_A = 11111L;
    static final Long USER_B = 22222L;
    static final Long USER_C = 33333L;
    static final Long USER_D = 44444L;

    static final Long[] USERS = {USER_A,USER_B,USER_C,USER_D};

    @Test
    public void testTradingEngine(){
        var engine = createTradingEngine();

        engine.processEvent(depositEvent(USER_A, AssetEnum.USD, bd("58000")));
        engine.processEvent(depositEvent(USER_B, AssetEnum.USD, bd("126700")));
        engine.processEvent(depositEvent(USER_C, AssetEnum.BTC, bd("5.5")));
        engine.processEvent(depositEvent(USER_D, AssetEnum.BTC, bd("8.6")));

        engine.debug();
        engine.validate();

        engine.processEvent(orderRequestEvent(USER_A, Direction.BUY, bd("2207.33"), bd("1.2")));
        engine.processEvent(orderRequestEvent(USER_C, Direction.SELL, bd("2215.6"), bd("0.8")));
        engine.processEvent(orderRequestEvent(USER_C, Direction.SELL, bd("2921.1"), bd("0.3")));


        engine.debug();
        engine.validate();

        engine.processEvent(orderRequestEvent(USER_D, Direction.SELL, bd("2206"), bd("0.3")));

        engine.debug();
        engine.validate();

        engine.processEvent(orderRequestEvent(USER_B, Direction.BUY, bd("2219.6"), bd("2.4")));

        engine.debug();
        engine.validate();

        engine.processEvent(orderCancelEvent(USER_A, 1L));

        engine.debug();
        engine.validate();
    }

    OrderRequestEvent orderRequestEvent(Long userId, Direction direction,BigDecimal price,BigDecimal quantity){
        var event = createEvent(OrderRequestEvent.class);
        event.userId = userId;
        event.direction = direction;
        event.price = price;
        event.quantity = quantity;
        return  event;
    }

    OrderCancelEvent orderCancelEvent(Long userId,Long orderId){
        var event = createEvent(OrderCancelEvent.class);
        event.userId = userId;
        event.refOrderId = orderId;
        return event;
    }

    TransferEvent depositEvent(Long userId, AssetEnum asset, BigDecimal amount){
        var event = createEvent(TransferEvent.class);
        event.fromUserId = UserType.DEBT.getInternalIdUserId();
        event.toUserId = userId;
        event.amount =amount;
        event.asset = asset;
        event.sufficient = false;
        return event;
    }

    private  long currentSequenceId= 0;

    <T extends AbstractEvent> T createEvent(Class<T> clazz){
        T event;
        try {
            event = clazz.getConstructor().newInstance();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        event.previousId = this.currentSequenceId;
        this.currentSequenceId++;
        event.sequenceId = this.currentSequenceId;
        event.createdAt = LocalDateTime.parse("2022-02-22T22:22:22").atZone(ZoneId.of("Z")).toEpochSecond() * 1000 + this.currentSequenceId;
        return  event;
    }
    TradingEngineService createTradingEngine(){
        var matchEngine = new MatchEngine();
        var assetService = new AssetService();
        var orderService = new OrderService(assetService);
        var clearingService = new ClearingService(assetService,orderService);
        var engine = new  TradingEngineService();
        engine.assetService = assetService;
        engine.orderService = orderService;
        engine.matchEngine = matchEngine;
        engine.clearingService = clearingService;
        return engine;
    }
    BigDecimal bd(String s){
        return new BigDecimal(s);
    }
}
