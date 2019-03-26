package com.zuehlke.poc.aws.txt2speech;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.amazonaws.services.sqs.AmazonSQSAsync;

@SpringBootApplication
@Configuration
@EnableCaching
@EnableScheduling
public class Text2SpeechApplication {
	@Value("${sqsMaxMsg:10}")
	private int sqsMaxMsg;
	
	@Bean
	public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
		SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
		factory.setAmazonSqs(amazonSqs);
		factory.setAutoStartup(true);
		factory.setMaxNumberOfMessages(sqsMaxMsg);
		factory.setVisibilityTimeout(5);
		factory.setWaitTimeOut(20);

		return factory;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Text2SpeechApplication.class, args);
	}
}
