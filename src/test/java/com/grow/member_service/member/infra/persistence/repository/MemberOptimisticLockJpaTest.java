package com.grow.member_service.member.infra.persistence.repository;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@DataJpaTest
class MemberOptimisticLockJpaTest {

	@Autowired MemberJpaRepository repo;
	@Autowired jakarta.persistence.EntityManager em;
	@Autowired EntityManagerFactory emf;

	@Test
	@DisplayName("동시 수정 시 2번째 트랜잭션이 낙관락 예외를 던진다")
	void optimisticLock_conflict() {
		// 1) 시드: 별도 트랜잭션에서 커밋
		EntityManager seeder = emf.createEntityManager();
		seeder.getTransaction().begin();
		MemberJpaEntity seed = MemberJpaEntity.builder()
			.email("a@grow.com").nickname("A")
			.platform(Platform.KAKAO).platformId("pid-1")
			.phoneNumber("010").phoneVerified(false)
			.address("Seoul")
			.createAt(LocalDateTime.now())
			.totalPoint(0).score(36.5).matchingEnabled(true)
			.build();
		seeder.persist(seed);
		seeder.getTransaction().commit();
		Long id = seed.getMemberId();
		seeder.close();

		// 2) 서로 다른 영속성 컨텍스트
		EntityManager em1 = emf.createEntityManager();
		EntityManager em2 = emf.createEntityManager();
		em1.getTransaction().begin();
		em2.getTransaction().begin();

		MemberJpaEntity e1 = em1.find(MemberJpaEntity.class, id);
		MemberJpaEntity e2 = em2.find(MemberJpaEntity.class, id);

		// 3) 1번이 먼저 업데이트/커밋 → version +1
		org.springframework.test.util.ReflectionTestUtils.setField(e1, "nickname", "A1");
		em1.flush();
		em1.getTransaction().commit();
		em1.close();

		ReflectionTestUtils.setField(e2, "nickname", "A2");

		assertThatThrownBy(() -> {
			// flush 호출하지 말고 commit만 호출
			em2.getTransaction().commit();
		}).isInstanceOf(jakarta.persistence.RollbackException.class)
			.hasCauseInstanceOf(jakarta.persistence.OptimisticLockException.class);

		em2.close();
	}

	@Test
	@DisplayName("업데이트 시 @Version이 증가한다")
	void version_increments_on_update() {
		// 1) insert
		MemberJpaEntity m = repo.saveAndFlush(MemberJpaEntity.builder()
			.email("b@grow.com").nickname("B")
			.platform(Platform.GOOGLE).platformId("pid-2")
			.phoneNumber("010").phoneVerified(false)
			.address("Seoul")
			.createAt(LocalDateTime.now())
			.totalPoint(0).score(36.5).matchingEnabled(true)
			.build());

		Long v0 = m.getVersion(); // 보통 0

		// 2) 변경
		org.springframework.test.util.ReflectionTestUtils.setField(m, "nickname", "B2");
		repo.saveAndFlush(m);      // ★ flush까지 보장
		em.clear();                // 캐시 비우고

		// 3) 재조회 → 버전 증가 확인
		MemberJpaEntity reloaded = repo.findById(m.getMemberId()).orElseThrow();
		org.assertj.core.api.Assertions.assertThat(reloaded.getVersion())
			.isNotNull()
			.isGreaterThan(v0);    // 0 -> 1 기대
	}
}