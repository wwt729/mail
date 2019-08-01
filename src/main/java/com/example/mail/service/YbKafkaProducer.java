package com.example.mail.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.mail.config.KafkaConfig;
import com.example.mail.config.MQConstants;
import com.example.mail.dto.MailDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class YbKafkaProducer {


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaConfig kafkaConfig;

    private Producer<String, Object> producer;

    private String topicName = MQConstants.Topic.ITEM_EXCHANGE_NAME;

    /******** method ***********************/
    /**
     * 第一种用法:发送普通消息没有回执
     */
    public void sendToKafkaNormalMessage(String message) {

        log.info("sending message='{}' to topic='{}'", message, topicName);

        // 使用Kafka直接向指定的Topic(主题)发送消息
        kafkaTemplate.send(topicName, message);
    }

    /**
     * 第二种用法:发送标准消息没有回执
     */
    public void sendToKafkaStandardMessage(String message) {

        log.info("sending message='{}' to topic='{}'", message, topicName);

        //构造一个标准的消息进行发送
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, message);

        kafkaTemplate.send(record);

    }

    private static Gson gson = new GsonBuilder().create();


    /**
     * 第三种用法:发送标准消息异步无阻塞
     **/
    public void sendToKafkaStandardMessageAsync(MailDTO mailDTO) {

        producer = new KafkaProducer<String, Object>(kafkaConfig.producerConfigs());

        producer.send(new ProducerRecord<String, Object>(topicName, gson.toJson(mailDTO)), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (metadata != null) {
                    log.info("生产消息成功{},发送次数{},checksum：{},offset:{},partition:{},topic:{}", mailDTO.getMailUid(),mailDTO.getSendNum(),metadata.checksum(), metadata.offset(), metadata.partition(), metadata.topic());
                }
                if (exception != null) {
                    log.info("生产消息失败{}", exception.getMessage());
                }
            }
        });
        producer.close();
    }

    /**
     * 第四种用法：发送标准消息同步有阻塞
     */
    public void sendToKafkaStandardMessageSync(String message) {

        //构建发送消息
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, message);
        try {
            // 使用模板发送消息
            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            log.error("InterruptedException:", e.toString());
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            log.debug("ExecutionException:", e.toString());
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            log.debug("TimeoutException:", e.toString());
        }
    }

}