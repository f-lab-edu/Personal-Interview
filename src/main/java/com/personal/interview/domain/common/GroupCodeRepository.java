package com.personal.interview.domain.common;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface GroupCodeRepository extends Repository<GroupCode, String> {
	Optional<GroupCode> findByGroupCode(String groupCode);

	GroupCode save(GroupCode groupCode);
}
