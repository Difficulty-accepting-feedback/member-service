package com.grow.member_service.member.application.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;
import com.grow.member_service.member.infra.persistence.repository.MemberJpaRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ScoreUpdateServiceTest {

    @Autowired
    private ScoreUpdateService scoreUpdateService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RedisTemplate<String, Double> customDoubleRedisTemplate;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @MockitoBean(name = "defaultRetryTopicKafkaTemplate")
    private KafkaTemplate<Object, Object> defaultRetryTopicKafkaTemplate;

    private static final String TRUST_KEY = "member:trust:score:";

    MemberJpaEntity member1;
    MemberJpaEntity member2;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        customDoubleRedisTemplate.getConnectionFactory().getConnection().flushDb();
        memberJpaRepository.deleteAll();

        // EntityManager로 멤버 엔티티 저장
        member1 = MemberJpaEntity.builder()
                .email("test1@example.com")
                .nickname("test1")
                .platform(Platform.GOOGLE)  // enums.Platform 가정
                .platformId("plat1")
                .createAt(LocalDateTime.now())
                .totalPoint(0)
                .score(85.5)
                .build();
        entityManager.persist(member1);

        member2 = MemberJpaEntity.builder()
                .email("test2@example.com")
                .nickname("test2")
                .platform(Platform.GOOGLE)
                .platformId("plat2")
                .totalPoint(0)
                .createAt(LocalDateTime.now())
                .score(92.0)
                .build();
        entityManager.persist(member2);

        entityManager.flush();  // 즉시 DB 반영
    }

    @Test
    @DisplayName("updateAllMemberScores(): 모든 멤버의 score를 업데이트하는 테스트 코드")
    void updateAllMemberScores_shouldUpdateRedis_whenMembersExist() {
        // Given: 존재하는 멤버들의 score가 DB에 저장되어 있음

        // When: 서비스 메서드 호출
        scoreUpdateService.updateAllMemberScores();

        // Then: Redis에 score가 제대로 저장되었는지 확인
        Double score1 = customDoubleRedisTemplate.opsForValue().get(TRUST_KEY + member1.getMemberId());
        assertThat(score1).isEqualTo(85.5);

        Double score2 = customDoubleRedisTemplate.opsForValue().get(TRUST_KEY + member2.getMemberId());
        assertThat(score2).isEqualTo(92.0);
    }
}