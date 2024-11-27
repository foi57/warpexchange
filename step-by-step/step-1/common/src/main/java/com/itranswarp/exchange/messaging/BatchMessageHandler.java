package com.itranswarp.exchange.messaging;

import com.itranswarp.exchange.message.AbstractMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

@FunctionalInterface
public interface BatchMessageHandler<T extends AbstractMessage> {

    void processMessages(List<T> messages);

}
