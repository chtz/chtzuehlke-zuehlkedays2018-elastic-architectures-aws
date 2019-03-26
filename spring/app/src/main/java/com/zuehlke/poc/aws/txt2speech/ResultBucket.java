package com.zuehlke.poc.aws.txt2speech;

import java.io.InputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Component
public class ResultBucket {
	private final static Logger log = LoggerFactory.getLogger(ResultBucket.class);
	
	@Autowired
	private ResourceIdResolver idResolver;
	
	@Value("${resultBucketLogicalName:txt2speechResponseBucket}")
	private String resultBucketLogicalName;
	
	private String resultBucket;
	
	private AmazonS3 s3;
	
	@PostConstruct
	public void init() {
		resultBucket = idResolver.resolveToPhysicalResourceId(resultBucketLogicalName); //logical name of a AWS::S3::Bucket cloud formation resource
		
		s3 = AmazonS3ClientBuilder.defaultClient(); //implicit: InstanceProfileCredentialsProvider
	}

	@Cacheable(value="writeApproxOnce", key="#id")
	public PutResult putPublicReadObject(String id, String contentType, long audioStreamLength, InputStream audioStream) {
		log.info("Uploading: {}", id);
		
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType(contentType);
		meta.setContentLength(audioStreamLength);
		
		PutObjectRequest putRequest = new PutObjectRequest(resultBucket, id, audioStream, meta)
				.withCannedAcl(CannedAccessControlList.PublicRead);
		
		return new PutResult(id, s3.putObject(putRequest).getContentMd5());
	}
	
	static class PutResult implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final String id;
		private final String md5;
		
		public PutResult(String id, String md5) {
			this.id = id;
			this.md5 = md5;
		}

		public String getId() {
			return id;
		}

		public String getMd5() {
			return md5;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
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
			PutResult other = (PutResult) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
	}
}
