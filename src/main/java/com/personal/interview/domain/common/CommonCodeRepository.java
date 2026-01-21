package com.personal.interview.domain.common;

import org.springframework.data.repository.Repository;

public interface CommonCodeRepository extends Repository<CommonCode, String> {
	boolean existsByCommonCode(String commonCode);

	CommonCode save(CommonCode commonCode);
}
