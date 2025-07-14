package com.grow.member_service.member.infra.adapter;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import net.datafaker.Faker;

import com.grow.member_service.member.application.port.NicknameGeneratorPort;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatafakerNicknameGenerator implements NicknameGeneratorPort {

	private final MemberRepository memberRepository;
	private final Faker faker = new Faker(new Locale("ko"));

	@Override
	public String generate(String base) {
		String nickname = comboAdjAndNoun();
		while (memberRepository.findByNickname(nickname).isPresent()) {
			nickname = comboAdjAndNoun();
		}
		return nickname;
	}

	/** 색상+명사(Kpop 그룹명) 콤보를 만들어 반환 */
	private String comboAdjAndNoun() {
		String adj  = faker.color().name().replaceAll("\\s+", "");
		String noun = randomKoreanNoun();
		return adj + noun;
	}

	/**
	 * Kpop provider의 모든 메서드(iGroups, iiGroups, iiiGroups, girlGroups, boyBands, solo)
	 * 중 하나를 랜덤으로 선택해 한글만 남기고 반환
	 */
	private String randomKoreanNoun() {
		// 사용할 Kpop provider 메서드를 Supplier로 등록
		List<Supplier<String>> providers = List.of(
			() -> faker.kpop().iGroups(),
			() -> faker.kpop().iiGroups(),
			() -> faker.kpop().iiiGroups(),
			() -> faker.kpop().girlGroups(),
			() -> faker.kpop().boyBands(),
			() -> faker.kpop().solo()
		);

		String noun = "";
		// 중복 시도 방지를 위해 최대 3번까지 재시도
		for (int attempt = 0; attempt < 3; attempt++) {
			// 랜덤 provider 선택
			Supplier<String> provider = providers.get(
				ThreadLocalRandom.current().nextInt(providers.size())
			);
			String raw = provider.get();
			// 공백 제거 + 한글 외 문자 필터링
			noun = raw.replaceAll("\\s+", "")
				.replaceAll("[^가-힣]", "");
			if (!noun.isEmpty()) {
				break;
			}
		}
		return noun;
	}
}