package com.personal.interview.domain.user.entity.vo;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.personal.interview.domain.common.CommonCodeType;

public enum JobCategoryName implements CommonCodeType {
	BACKEND,
	FRONTEND,
	DEVOPS,
	MOBILE,
	DATA_SCIENCE,
	AI,
	SECURITY,
	ETC;

	@JsonCreator
	public static JobCategoryName from(String val) {
		return Arrays.stream(values())
			.filter(type -> type.name().equals(val))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 직무 카테고리 이름입니다: " + val));
	}


	@Override public String getCode() { return this.name(); }
	@Override public String getGroupName() { return "JOB_CATEGORY"; }
	@Override public String getGroupDescription() { return "직무 카테고리 구분"; }
}
