package com.raisedeveloper.server.domain.exercise.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.exercise.consumer.AlarmConsumerConstants;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaConsumerLagMonitor {

	private static final String METRIC_NAME = "kafka.consumer.group.lag";

	private final AdminClient adminClient;
	private final AtomicLong pushConsumerLag = new AtomicLong(0);

	private final String pushGroupId = AlarmConsumerConstants.PUSH_GROUP_ID;
	private final String pushTopic = ExerciseKafkaTopics.ALARM_SESSION_CREATED_V1;

	public KafkaConsumerLagMonitor(
		@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
		MeterRegistry meterRegistry
	) {
		this.adminClient = AdminClient.create(Map.of(
			AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
		));
		meterRegistry.gauge(
			METRIC_NAME,
			io.micrometer.core.instrument.Tags.of(
				"group", pushGroupId,
				"topic", pushTopic
			),
			pushConsumerLag
		);
	}

	@Scheduled(fixedDelayString = "${app.kafka.lag-monitor.fixed-delay-ms:10000}")
	public void collectPushConsumerLag() {
		pushConsumerLag.set(calculateLag(pushGroupId, pushTopic));
	}

	private long calculateLag(String groupId, String topic) {
		try {
			Set<TopicPartition> partitions = adminClient.describeTopics(Set.of(topic))
				.allTopicNames()
				.get()
				.get(topic)
				.partitions()
				.stream()
				.map(p -> new TopicPartition(topic, p.partition()))
				.collect(java.util.stream.Collectors.toSet());

			Map<TopicPartition, Long> committed = new HashMap<>();
			adminClient.listConsumerGroupOffsets(groupId)
				.partitionsToOffsetAndMetadata()
				.get()
				.forEach((tp, metadata) -> {
					if (topic.equals(tp.topic()) && metadata != null) {
						committed.put(tp, metadata.offset());
					}
				});

			Map<TopicPartition, OffsetSpec> request = new HashMap<>();
			for (TopicPartition partition : partitions) {
				request.put(partition, OffsetSpec.latest());
			}

			Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets = adminClient.listOffsets(request)
				.all()
				.get();

			long totalLag = 0L;
			for (TopicPartition partition : partitions) {
				long latest = latestOffsets.get(partition).offset();
				long commit = committed.getOrDefault(partition, 0L);
				totalLag += Math.max(0L, latest - commit);
			}
			return totalLag;
		} catch (Exception e) {
			log.warn("Kafka lag 수집 실패 - groupId: {}, topic: {}", groupId, topic, e);
			return 0L;
		}
	}

	@PreDestroy
	public void shutdown() {
		adminClient.close();
	}
}
