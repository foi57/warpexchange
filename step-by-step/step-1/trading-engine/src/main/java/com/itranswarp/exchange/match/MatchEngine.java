package com.itranswarp.exchange.match;

import com.itranswarp.exchange.bean.OrderBookBean;
import com.itranswarp.exchange.enums.Direction;
import com.itranswarp.exchange.enums.OrderStatus;
import com.itranswarp.exchange.model.trade.OrderEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MatchEngine {

    public final OrderBook buyBook = new OrderBook(Direction.BUY);
    public final OrderBook sellBook = new OrderBook(Direction.SELL);
    public BigDecimal marketPrice = BigDecimal.ZERO;
    private  long sequenceId;

    public MatchResult processOrder(long sequenceId, OrderEntity order){
        return switch (order.direction){
            case BUY -> processOrder(sequenceId,order,this.sellBook,this.buyBook);
            case SELL -> processOrder(sequenceId,order,this.buyBook,this.sellBook);
            default -> throw new IllegalArgumentException("Invalid direction");
        };
    }

    private MatchResult processOrder(long sequenceId,OrderEntity takerOrder,OrderBook makerBook,
                                     OrderBook anotherBook){
        this.sequenceId=sequenceId;
        long ts =takerOrder.createdAt;
        MatchResult matchResult = new MatchResult(takerOrder);
        BigDecimal takerUnfilledQuantity = takerOrder.quantity;
        for (;;){
            OrderEntity makerOrder = makerBook.getFirst();
            if (makerOrder == null){
                break;
            }
            if (takerOrder.direction == Direction.BUY && takerOrder.price.compareTo(makerOrder.price) < 0){
                break;
            }else if (takerOrder.direction == Direction.SELL && takerOrder.price.compareTo(makerOrder.price) > 0){
                break;
            }
            this.marketPrice = makerOrder.price;

            BigDecimal matchedQuantity = takerUnfilledQuantity.min(makerOrder.unfilledQuantity);

            matchResult.add(makerOrder.price,matchedQuantity,makerOrder);

            takerUnfilledQuantity = takerUnfilledQuantity.subtract(matchedQuantity);
            BigDecimal makerUnfilledQuantity = makerOrder.unfilledQuantity.subtract(matchedQuantity);
            if (makerUnfilledQuantity.signum() == 0){
                makerOrder.updateOrder(makerUnfilledQuantity, OrderStatus.FULLY_CANCELLED,ts);
                makerBook.remove(makerOrder);
            }else {
                makerOrder.updateOrder(makerUnfilledQuantity,OrderStatus.FULLY_FILLED,ts);
            }
            if (takerUnfilledQuantity.signum() == 0){
                takerOrder.updateOrder(takerUnfilledQuantity,OrderStatus.FULLY_CANCELLED,ts);
                break;
            }
        }
        if (takerUnfilledQuantity.signum() > 0){
            takerOrder.updateOrder(takerUnfilledQuantity,takerUnfilledQuantity.compareTo(takerOrder.quantity) == 0 ? OrderStatus.PENDING :
                    OrderStatus.PARTIAL_FILLED,ts);
            anotherBook.add(takerOrder);
        }
        return matchResult;
    }

    public void cancel(long ts,OrderEntity order){
        OrderBook book = order.direction == Direction.BUY ? this.buyBook : this.sellBook;
        if (!book.remove(order)){
            throw new IllegalArgumentException("Order not found in order book");
        }
        OrderStatus status = order.unfilledQuantity.compareTo(order.quantity) == 0 ? OrderStatus.FULLY_CANCELLED : OrderStatus.PARTIAL_CANCELLED;
        order.updateOrder(order.unfilledQuantity,status,ts);
    }

    public OrderBookBean getOrderBook(int maxDepth) {
        return new OrderBookBean(this.sequenceId,this.marketPrice,this.buyBook.getOrderBook(maxDepth),this.sellBook.getOrderBook(maxDepth));
    }

    public void debug() {
        System.out.println("---------- match engine ----------");
        System.out.println(this.sellBook);
        System.out.println("  ----------");
        System.out.println("  " + this.marketPrice);
        System.out.println("  ----------");
        System.out.println(this.buyBook);
        System.out.println("---------- // match engine ----------");
    }

}
