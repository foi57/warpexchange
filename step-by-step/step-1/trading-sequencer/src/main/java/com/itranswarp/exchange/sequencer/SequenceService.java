package com.itranswarp.exchange.sequencer;

import com.itranswarp.exchange.message.event.AbstractEvent;
import com.itranswarp.exchange.messaging.*;
import com.itranswarp.exchange.support.LoggerSupport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SequenceService extends LoggerSupport  implements CommonErrorHandler {

    private static final String GROUP_ID = "SequencerGroup";

    @Autowired
    private SequenceHandler sequenceHandler;

    @Autowired
    private MessagingFactory messagingFactory;

    @Autowired
    private MessageTypes messageTypes;

    private MessageProducer<AbstractEvent> messageProducer;

    private AtomicLong sequence;

    private Thread jobThread;

    private boolean running = false;

    private boolean crash = false;
    @PostConstruct
    public void init(){
        Thread thread = new Thread(() -> {
            logger.info("start sequence job...");
            this.messageProducer = this.messagingFactory.createMessageProducer(Messaging.Topic.TRADE,AbstractEvent.class);

            this.sequence = new AtomicLong(this.sequenceHandler.getMaxSequence());

            logger.info("create message consumer for {}...", getClass().getName());
            MessageConsumer consumer = this.messagingFactory.createBatchMessageListener(Messaging.Topic.SEQUENCE,GROUP_ID,this::processMessages,this);

            this.running = true;

            while (running){
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    break;
                }
            }
            logger.info("close message consumer for {}...", getClass().getName());
            consumer.stop();
            System.exit(1);
        });
        this.jobThread = thread;
        this.jobThread.start();
    }

    @PreDestroy
    public void shutdown(){
        logger.info("shutdown sequence service...");
        running = false;
        if (jobThread !=null){
            jobThread.interrupt();
            try {
                jobThread.join(5000);
            }catch (InterruptedException e){
                logger.error("interrupt job thread failed", e);
            }
            jobThread = null;
        }
    }

    private void sendMessages(List<AbstractEvent> messages) {
        this.messageProducer.sendMessages(messages);
    }

    private void processMessages(List<AbstractEvent> messages){
        if (!running || crash){
            panic();
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("do sequence for {} messages...", messages.size());
        }
        long start = System.currentTimeMillis();
        List<AbstractEvent> sequenced = null;
        try {
            sequenced = sequenceHandler.sequenceMessages(this.messageTypes, this.sequence,messages);
        }catch (Throwable e){
            logger.error("exception when do sequence", e);
            shutdown();
            panic();
            throw new Error(e);
        }
        if (logger.isInfoEnabled()) {
            long end = System.currentTimeMillis();
            logger.info("sequenced {} messages in {} ms. current sequence id: {}", messages.size(), (end - start),
                    this.sequence.get());
        }
        sendMessages(sequenced);
    }

    private void panic(){
        this.crash = true;
        this.running = false;
        System.exit(1);
    }
}
