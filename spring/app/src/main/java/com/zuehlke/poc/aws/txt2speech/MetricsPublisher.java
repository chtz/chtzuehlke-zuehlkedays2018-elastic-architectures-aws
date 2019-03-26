package com.zuehlke.poc.aws.txt2speech;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class MetricsPublisher {
	private AmazonCloudWatch cloudwatch;

	private Map<String,List<MetricsData>> data = new HashMap<>();
	
	@PostConstruct
	public void init() {
		cloudwatch = AmazonCloudWatchClientBuilder.defaultClient();
	}

	public void putCountMetricData(String namespace, String metricName, String dimensionName, String dimensionValue, Double metricValue) {
		synchronized (this) {
			MetricsData d = new MetricsData(namespace, metricName, dimensionName, dimensionValue, metricValue);
			if (!data.containsKey(d.key())) {
				data.put(d.key(), new LinkedList<>());
			}
			data.get(d.key()).add(d);
		}
	}
	
	@Scheduled(fixedRate = 30000, initialDelay = 30000)
	public void cloudwatchPutBatch() {
		Map<String,List<MetricsData>> data;
		synchronized (this) {
			data = this.data;
			this.data = new HashMap<>();
		}
		
		for (List<MetricsData> ds : data.values()) {
			double metricValue = 0;
			for (MetricsData d : ds) {
				metricValue += d.metricValue;
			}
			
			MetricsData first = ds.get(0);
			immediatePutCountMetricData(first.namespace, first.metricName, first.dimensionName, first.dimensionValue, metricValue);
		}
	}
	
	private void immediatePutCountMetricData(String namespace, String metricName, String dimensionName, String dimensionValue, Double metricValue) {
		Dimension dimension = new Dimension()
				.withName(dimensionName)
				.withValue(dimensionValue);

		MetricDatum datum = new MetricDatum()
				.withMetricName(metricName)
				.withUnit(StandardUnit.Count)
				.withValue(metricValue)
				.withDimensions(dimension);

		PutMetricDataRequest request = new PutMetricDataRequest()
				.withNamespace(namespace)
				.withMetricData(datum);

		cloudwatch.putMetricData(request);
	}
	
	static class MetricsData {
		private final String namespace;
		private final String metricName;
		private final String dimensionName;
		private final String dimensionValue;
		private Double metricValue;
		
		public MetricsData(String namespace, String metricName, String dimensionName, String dimensionValue, Double metricValue) {
			this.namespace = namespace;
			this.metricName = metricName;
			this.dimensionName = dimensionName;
			this.dimensionValue = dimensionValue;
			this.metricValue = metricValue;
		}

		public String key() {
			return namespace + metricName + dimensionName + dimensionValue;
		}
	}
}
