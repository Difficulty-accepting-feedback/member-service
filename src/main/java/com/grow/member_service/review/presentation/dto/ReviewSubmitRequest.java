package com.grow.member_service.review.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "리뷰 작성 요청 DTO")
public class ReviewSubmitRequest {

	@NotBlank(message = "리뷰 내용을 입력해주세요.")
	@Size(max = 1000, message = "리뷰 내용은 1000자를 초과할 수 없습니다.")
	private String content;

	@NotNull(message = "성실성 점수를 입력해주세요.")
	@Min(value = 0)
	@Max(value = 5)
	private Integer sincerityScore;

	@NotNull(message = "적극성 점수를 입력해주세요.")
	@Min(value = 0)
	@Max(value = 5)
	private Integer enthusiasmScore;

	@NotNull(message = "커뮤니케이션 점수를 입력해주세요.")
	@Min(value = 0)
	@Max(value = 5)
	private Integer communicationScore;
}