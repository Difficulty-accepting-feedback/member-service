package com.grow.member_service.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	/**
	 * "member.notification.requested" 토픽을 생성합니다.
	 * 파티션 수는 3개, 복제본 수는 1개로 설정되어 있습니다.
	 * 이 토픽은 멤버 서비스에서 알림 요청 메시지를 발행하는 데 사용됩니다.
	 * @return 생성된 Kafka 토픽 객체
	 */
	@Bean
	public NewTopic memberNotificationRequested() {
		return TopicBuilder.name("member.notification.requested")
			.partitions(3)
			.replicas(3)
			.build();
	}

	@Bean
	public NewTopic pointNotificationRequested() {
		return TopicBuilder.name("point.notification.requested")
			.partitions(3)
			.replicas(3)
			.build();
	}

	@Bean
	public NewTopic achievementAchieved() {
		return TopicBuilder.name("achievement.achieved")
			.partitions(3)
			.replicas(3)
			.build();
	}

	@Bean
	public NewTopic achievementTrigger() {
		return TopicBuilder.name("achievement.trigger")
			.partitions(3)
			.replicas(3)
			.build();
	}
}