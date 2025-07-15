package com.grow.member_service.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeRequest {
	@NotBlank
	private String code;
}