package com.zuehlke.poc.aws.txt2speech;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;

@Component
public class SpeechSynthesizer {
	private final static Logger log = LoggerFactory.getLogger(SpeechSynthesizer.class);
	
	private AmazonPolly polly;
	
	@PostConstruct
	public void init() {
		polly = AmazonPollyClientBuilder.defaultClient();
	}

	@Cacheable("audio")
	public AudioData synthesizeSpeech(String text) { 
		log.info("Converting: {}", text);
		
		SynthesizeSpeechResult synthResult = polly.synthesizeSpeech(new SynthesizeSpeechRequest() //throttling: ~80 TPS!!!
				.withOutputFormat(OutputFormat.Mp3)
				.withText(text)
				.withVoiceId("Marlene"));
		
		try {
			return new AudioData(text, synthResult.getAudioStream());
		} catch (IOException e) {
			throw new RuntimeException("Cannot fetch audio data", e);
		}
	}
	
	static class AudioData implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final String text;
		private final byte[] audioData;
		
		public AudioData(String text, InputStream audioStream) throws IOException {
			this.text = text;
			try {
				byte[] buf = new byte[4096];
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					for (int l = audioStream.read(buf); l != -1; l = audioStream.read(buf)) {
						out.write(buf, 0, l);
					}
					this.audioData = out.toByteArray();
				}
			}
			finally {
				audioStream.close();
			}
		}
		
		public String getText() {
			return text;
		}

		public byte[] getAudioData() {
			return audioData;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AudioData other = (AudioData) obj;
			if (text == null) {
				if (other.text != null)
					return false;
			} else if (!text.equals(other.text))
				return false;
			return true;
		}
	}
}
