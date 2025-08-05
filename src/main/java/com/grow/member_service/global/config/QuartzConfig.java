package com.grow.member_service.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.grow.member_service.history.subscription.infra.batch.SubscriptionExpiryJob;
import com.grow.member_service.history.subscription.infra.batch.SubscriptionExpiryJobListener;

@Configuration
public class QuartzConfig {

	private final SubscriptionExpiryJobListener expiryListener;

	public QuartzConfig(SubscriptionExpiryJobListener expiryListener) {
		this.expiryListener = expiryListener;
	}

	/** 구독 만료 JobDetail Bean */
	@Bean
	public JobDetail subscriptionExpiryJobDetail() {
		return JobBuilder.newJob(SubscriptionExpiryJob.class)
			.withIdentity("subscriptionExpiryJob")
			.storeDurably()
			.usingJobData("retryCount", 0)
			.usingJobData("maxRetry", 3)
			.build();
	}

	/** 매일 0시 구독만료 실행 Trigger */
	@Bean
	public Trigger subscriptionExpiryTrigger(JobDetail subscriptionExpiryJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(subscriptionExpiryJobDetail)
			.withIdentity("subscriptionExpiryTrigger")
			.withSchedule(
				CronScheduleBuilder.cronSchedule("0 0 0 * * ?")
					.withMisfireHandlingInstructionFireAndProceed()
			)
			.build();
	}

	/** SchedulerFactoryBean 설정: JobDetail, Trigger, Global Listener 등록 */
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(
		JobDetail subscriptionExpiryJobDetail,
		Trigger subscriptionExpiryTrigger
	) {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setJobDetails(subscriptionExpiryJobDetail);
		factory.setTriggers(subscriptionExpiryTrigger);
		factory.setGlobalJobListeners(expiryListener);
		return factory;
	}
}