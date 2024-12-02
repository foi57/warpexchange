package com.itranswarp.exchange.web.api;

import com.itranswarp.exchange.bean.TransferRequentBean;
import com.itranswarp.exchange.enums.UserType;
import com.itranswarp.exchange.message.event.TransferEvent;
import com.itranswarp.exchange.service.SendEventService;
import com.itranswarp.exchange.support.AbstractApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class TradingInternalApiController extends AbstractApiController{
    @Autowired
    SendEventService sendEventService;


    @PostMapping("/transfer")
    public Map<String,Boolean> transferIn(@RequestBody TransferRequentBean transferRequest){
        logger.info("transfer request: transferId={}, fromUserId={}, toUserId={}, asset={}, amount={}",
                transferRequest.transferId, transferRequest.fromUserId, transferRequest.toUserId, transferRequest.asset,
                transferRequest.amount);
        transferRequest.validate();

        var message =new  TransferEvent();
        message.uniqueId = transferRequest.transferId;
        message.fromUserId = transferRequest.fromUserId;
        message.toUserId = transferRequest.toUserId;
        message.asset = transferRequest.asset;
        message.amount = transferRequest.amount;
        message.sufficient = transferRequest.fromUserId.longValue() != UserType.DEBT.getInternalIdUserId();
        this.sendEventService.sendMessage(message);
        logger.info("transfer event sent: {}", message);
        return Map.of("result",Boolean.TRUE);
    }
}
