package com.zuehlke.poc.aws.txt2speech;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuehlke.poc.aws.txt2speech.SpeechSynthesizer.AudioData;

@Component
public class Text2SpeechMessageListener { 
	public final static Logger log = LoggerFactory.getLogger(Text2SpeechMessageListener.class);
	
	private final ObjectMapper om = new ObjectMapper();
	
	@Autowired
	private ResultBucket s3;
	
	@Autowired
	private SpeechSynthesizer polly;
	
	@Autowired
	private MetricsPublisher metricsPublisher;
	
	@Autowired
	private ResourceIdResolver idResolver;

	private String requestQueue;
	
	@Value("${msgProcessingDebugSleepMillis:250}")
	private int msgProcessingDebugSleepMillis;
	
	@PostConstruct
	public void init() {
		requestQueue = idResolver.resolveToPhysicalResourceId("txt2speechRequestQueue"); 
	}

	@SqsListener("txt2speechRequestQueue")
	private void receiveMessage(String sqsMessage) throws JsonParseException, JsonMappingException, IOException {
		log.debug("Processing: {}", sqsMessage);
		
		Text2SpechRequest req = om.readValue(sqsMessage, Text2SpechRequest.class);
		
		AudioData audio = polly.synthesizeSpeech(req.getText());

		try (ByteArrayInputStream audioIn = new ByteArrayInputStream(audio.getAudioData())) {
			s3.putPublicReadObject(req.getId(), "audio/mpeg3", audio.getAudioData().length, audioIn);
		}
	    
		metricsPublisher.putCountMetricData("Txt2Speech", "MessagesProcessed", "queue", requestQueue, 1.0);
		
		try {
			Thread.sleep(msgProcessingDebugSleepMillis); //simulate slow IO
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
	    log.info("Processed: {}: {} audio-bytes", sqsMessage, audio.getAudioData().length);
	}
}
