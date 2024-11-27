package com.itranswarp.exchange.clearing;

import com.itranswarp.exchange.assets.AssetService;
import com.itranswarp.exchange.assets.Transfer;
import com.itranswarp.exchange.enums.AssetEnum;
import com.itranswarp.exchange.match.MatchDetailRecord;
import com.itranswarp.exchange.match.MatchResult;
import com.itranswarp.exchange.model.trade.OrderEntity;
import com.itranswarp.exchange.order.OrderService;
import com.itranswarp.exchange.support.LoggerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ClearingService  extends LoggerSupport {
    final AssetService assetService;

    final OrderService orderService;
    @Autowired
    public ClearingService(AssetService assetService,OrderService orderService){
        this.assetService = assetService;
        this.orderService = orderService;
    }

    public void clearMatchResult(MatchResult result){
        OrderEntity taker = result.takerOrder;
        switch (taker.direction){
            case BUY -> {
                for (MatchDetailRecord detail : result.matchDetails){
                    if (logger.isDebugEnabled()){
                        logger.debug( "clear buy matched detail: price = {}, quantity = {}, takerOrderId = {}, makerOrderId = {}, takerUserId = {}, makerUserId = {}",
                                detail.price(), detail.quantity(), detail.takerOrder().id, detail.makerOrder().id,
                                detail.takerOrder().userId, detail.makerOrder().userId);
                    }
                    OrderEntity maker = detail.makerOrder();
                    BigDecimal matched = detail.quantity();
                    if (taker.price.compareTo(maker.price) > 0){
                        BigDecimal unfreezeQuote = taker.price.subtract(maker.price).multiply(matched);
                        logger.debug("unfree extra unused quote {} back to taker user {}", unfreezeQuote, taker.userId);
                        assetService.unfreeze(taker.userId, AssetEnum.USD, unfreezeQuote);
                    }

                    assetService.transfer(Transfer.FROZEN_TO_AVAILABLE,taker.userId,maker.userId,AssetEnum.USD,maker.price.multiply(matched));
                    assetService.transfer(Transfer.FROZEN_TO_AVAILABLE,maker.userId,taker.userId,AssetEnum.BTC,matched);

                    if (maker.unfilledQuantity.signum() == 0){
                        orderService.removeOrder(maker.id);
                    }
                }
                if (taker.unfilledQuantity.signum() == 0){
                    orderService.removeOrder(taker.id);
                }
            }
            case SELL -> {
                for (MatchDetailRecord detail : result.matchDetails){
                    if (logger.isDebugEnabled()){
                        logger.debug(
                                "clear sell matched detail: price = {}, quantity = {}, takerOrderId = {}, makerOrderId = {}, takerUserId = {}, makerUserId = {}",
                                detail.price(), detail.quantity(), detail.takerOrder().id, detail.makerOrder().id,
                                detail.takerOrder().userId, detail.makerOrder().userId);
                    }
                    OrderEntity maker = detail.makerOrder();
                    BigDecimal matched = detail.quantity();

                    assetService.transfer(Transfer.FROZEN_TO_AVAILABLE,taker.userId,maker.userId,AssetEnum.BTC,matched);
                    assetService.transfer(Transfer.FROZEN_TO_AVAILABLE,maker.userId,taker.userId,AssetEnum.USD,maker.price.multiply(matched));

                    if (maker.unfilledQuantity.signum() == 0){
                        orderService.removeOrder(taker.id);
                    }
                }
                if (taker.unfilledQuantity.signum() == 0){
                    orderService.removeOrder(taker.id);
                }
            }
            default -> throw new IllegalArgumentException("Invalid direction");
        }
    }

    public void clearCancelOrder(OrderEntity order){
        switch (order.direction){
            case BUY -> {
                assetService.unfreeze(order.userId,AssetEnum.USD,order.price.multiply(order.unfilledQuantity));
            }
            case SELL -> {
                assetService.unfreeze(order.userId,AssetEnum.BTC,order.unfilledQuantity);
            }
            default -> throw new IllegalArgumentException("Invalid direction");
        }
        orderService.removeOrder(order.id);
    }
}
