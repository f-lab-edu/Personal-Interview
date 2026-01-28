package com.personal.interview.domain.common;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.personal.interview.domain.user.entity.vo.JobCategoryName;

class CommonCodeTypeTest {


	@Test
	void test(){
		CommonCodeType type = JobCategoryName.BACKEND;

		assertEquals("JOB_CATEGORY", type.getGroupName());
		assertThat(type.getCode()).isEqualTo("BACKEND");
	}

}