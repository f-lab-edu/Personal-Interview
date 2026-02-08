package com.personal.interview.domain.common;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.personal.interview.base.RepositoryTest;
import com.personal.interview.domain.user.entity.vo.JobCategoryName;

@DataJpaTest
@ActiveProfiles("test")
class CommonCodeRepositoryTest extends RepositoryTest {

	@Autowired
	private CommonCodeRepository commonCodeRepository;

	@Autowired
	private GroupCodeRepository groupCodeRepository;

	private GroupCode savedGroupCode;

	private JobCategoryName jobCategoryName;

	@BeforeEach
	void setUp() {
		jobCategoryName = JobCategoryName.BACKEND;

		savedGroupCode = groupCodeRepository.save(new GroupCode(jobCategoryName.getGroupName(),
			jobCategoryName.getGroupDescription()));
	}

	@Test
	@DisplayName("커먼 코드를 저장하고 존재 여부를 확인할 수 있다.")
	void existsByCommonCode_Success() {
		// given
		CommonCode commonCode = new CommonCode(jobCategoryName.getCode(), savedGroupCode);
		commonCodeRepository.save(commonCode);

		// when
		boolean exists = commonCodeRepository.existsByCommonCode(jobCategoryName.getCode());

		// then
		assertThat(exists).isTrue(); //
	}

	@Test
	@DisplayName("존재하지 않는 커먼 코드는 false를 반환한다.")
	void existsByCommonCode_Fail() {
		// when
		boolean exists = commonCodeRepository.existsByCommonCode("NON_EXIST_CODE");

		// then
		assertThat(exists).isFalse();
	}
}