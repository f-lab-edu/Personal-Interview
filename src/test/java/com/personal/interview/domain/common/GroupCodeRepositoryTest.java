package com.personal.interview.domain.common;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.personal.interview.base.RepositoryTest;
import com.personal.interview.domain.user.entity.vo.JobCategoryName;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test") // 사용자님의 테스트 환경 프로필 반영
class GroupCodeRepositoryTest extends RepositoryTest {

	@Autowired
	private GroupCodeRepository groupCodeRepository;

	@Autowired
	EntityManager em;

	@Test
	@DisplayName("그룹 코드를 저장하고 코드로 조회할 수 있다.")
	void saveAndFindByGroupCode() {
		// given
		JobCategoryName backend = JobCategoryName.BACKEND;
		GroupCode groupCode = new GroupCode(backend.getGroupName(), backend.getGroupDescription());
		groupCodeRepository.save(groupCode);
		em.flush();
		em.clear();

		// when
		Optional<GroupCode> result = groupCodeRepository.findByGroupCode(backend.getGroupName());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getGroupCode()).isEqualTo(backend.getGroupName());
		assertThat(result.get().getDescription()).isEqualTo(backend.getGroupDescription());
	}
}