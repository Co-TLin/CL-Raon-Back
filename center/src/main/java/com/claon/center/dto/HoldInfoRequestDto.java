package com.claon.center.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HoldInfoRequestDto {
    @NotBlank(message = "홀드 이름을 입력 해주세요.")
    private String name;
    @NotBlank(message = "홀드 이미지를 입력 해주세요.")
    private String img;
}