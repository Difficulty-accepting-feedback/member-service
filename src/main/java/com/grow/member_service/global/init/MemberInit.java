package com.grow.member_service.global.init;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class MemberInit implements CommandLineRunner {

    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) throws Exception {
        List<String> regions = List.of(
                "서울특별시 강남구",
                "서울특별시 서초구",
                "서울특별시 노원구",
                "서울특별시 성북구",
                "서울특별시 강북구",
                "서울특별시 강동구",
                "서울특별시 도봉구",
                "서울특별시 송파구",
                "경기도 구리시",
                "경기도 성남시"
        );

        int created = 0;
        for (int i = 0; i < regions.size(); i++) {
            String region = regions.get(i);
            String platformId = "dummy-platform-" + (i + 1);
            Platform platform = (i % 2 == 0) ? Platform.GOOGLE : Platform.KAKAO;
            String email = "dummy" + (i + 1) + "@example.com";
            String nickname = "더미" + (i + 1);

            Member existing = memberRepository.findByPlatformId(platformId, platform).orElse(null);
            if (existing == null) {
                MemberProfile p = new MemberProfile(email, nickname, null, platform, platformId);
                MemberAdditionalInfo a = new MemberAdditionalInfo(null, region);
                Member m = new Member(p, a, Clock.systemUTC());
                existing = memberRepository.save(m);
                created++;
                log.info("[INIT] 더미 멤버 생성 - memberId={}, nickname={}, region='{}'",
                        existing.getMemberId(), nickname, region);
            } else {
                log.info("[INIT] 더미 멤버 존재 - memberId={}, nickname={}, region(현재)='{}'",
                        existing.getMemberId(), existing.getMemberProfile().getNickname(),
                        existing.getAdditionalInfo().getAddress());
            }

            log.info("[INIT] 더미 멤버 생성 완료 - 신규 {}명 / 총 시도 {}건", created, regions.size());
        }
    }
}
