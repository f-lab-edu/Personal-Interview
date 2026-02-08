package com.personal.interview.domain.common;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.user.entity.vo.JobCategoryName;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommonCodeInitializer {

	private final GroupCodeRepository groupCodeRepository;
	private final CommonCodeRepository commonCodeRepository;
	private final Environment env;

	private final List<Class<? extends Enum<? extends CommonCodeType>>> codeEnums = List.of(
		JobCategoryName.class
	);

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void init() {
		boolean isAutoInsertProfile = isProfileActive("local", "test");


		for (Class<? extends Enum<? extends CommonCodeType>> enumClass : codeEnums) {
			CommonCodeType[] constants = (CommonCodeType[]) enumClass.getEnumConstants();
			if (constants == null || constants.length == 0) continue;

			// 1. 그룹 코드 검증 및 처리
			String groupCodeId = constants[0].getGroupName();
			String groupName = constants[0].getGroupDescription();

			GroupCode groupCode = groupCodeRepository.findByGroupCode(groupCodeId)
				.orElseGet(() -> {
					if (isAutoInsertProfile) {
						log.info("Auto Insert GroupCode: {}", groupCodeId);
						return groupCodeRepository.save(new GroupCode(groupCodeId, groupName));
					}
					throw new IllegalStateException("DB에 GroupCode가 누락되었습니다: " + groupCodeId);
				});

			// 2. 개별 커먼 코드 검증 및 처리
			for (CommonCodeType type : constants) {
				if (!commonCodeRepository.existsByCommonCode(type.getCode())) {
					if (isAutoInsertProfile) {
						log.info("Auto Insert CommonCode: {}", type.getCode());
						commonCodeRepository.save(new CommonCode(type.getCode(), groupCode));
					} else {
						throw new IllegalStateException("DB에 CommonCode가 누락되었습니다: " + type.getCode());
					}
				}
			}
		}
		log.info("공통 코드 동기화 완료");
	}

	private boolean isProfileActive(String... profiles) {
		List<String> activeProfiles = Arrays.asList(env.getActiveProfiles());

		return Arrays.stream(profiles).anyMatch(activeProfiles::contains);
	}
}