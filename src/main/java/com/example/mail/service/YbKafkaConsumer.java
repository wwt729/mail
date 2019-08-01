package com.example.mail.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.mail.config.MQConstants;
import com.example.mail.dto.MailDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 监听Kafka Topic,从里面取数据
 **/
@Service
@Slf4j
public class YbKafkaConsumer {


    @Autowired
    private SendMessageService sendMessageService;
    private static Gson gson = new GsonBuilder().create();

    /**
     * 监听一个Kafka 主题
     **/
    @KafkaListener(topics = MQConstants.Topic.ITEM_EXCHANGE_NAME)
    public void receiveMessageFromKafka(ConsumerRecord<?, ?> record, Acknowledgment ack) {
        log.info("监听消息,MailUid:{}", gson.fromJson(String.valueOf(record.value()), MailDTO.class).getMailUid());

        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            sendMessageService.sendMessages(gson.fromJson(String.valueOf(record.value()), MailDTO.class));
        }
        ack.acknowledge();
    }


}