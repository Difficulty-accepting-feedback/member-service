package com.grow.member_service.quiz.result.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.repository.QuizResultRepository;
import com.grow.member_service.quiz.result.domain.service.QuizResultService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizResultServiceImpl implements QuizResultService {
	private final QuizResultRepository repository;

	@Override
	public QuizResult recordResult(Long memberId, Long quizId, Boolean isCorrect) {
		return repository.save(new QuizResult(memberId, quizId, isCorrect)); // 중복체크나 예외처리 필요
	}

	@Override
	public List<QuizResult> getResultsForMember(Long memberId) {
		return repository.findByMemberId(memberId);
	}

	@Override
	public long countCorrectAnswers(Long memberId) {
		return repository.findByMemberId(memberId)
			.stream()
			.filter(QuizResult::getIsCorrect)
			.count();
	}

	@Override
	public double getSuccessRate(Long memberId) {
		List<QuizResult> results = repository.findByMemberId(memberId);
		if (results.isEmpty()) {
			return 0.0;
		}
		long correct = results.stream().filter(QuizResult::getIsCorrect).count();
		return (double) correct / results.size();
	}
}

