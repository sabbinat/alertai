package com.sbact1.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryDto(@NotBlank String name) {
	
}
