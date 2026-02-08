package com.personal.interview.domain.common;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class CommonCodeInitializerTest {

	@InjectMocks
	private CommonCodeInitializer commonCodeInitializer;

	@Mock
	private GroupCodeRepository groupCodeRepository;

	@Mock
	private CommonCodeRepository commonCodeRepository;

	@Mock
	private Environment env;

	@Test
	@DisplayName("local 프로파일일 때 DB에 데이터가 없으면 자동으로 삽입한다.")
	void init_LocalProfile_InsertData() {
		// given
		given(env.getActiveProfiles()).willReturn(new String[]{"local"});
		given(groupCodeRepository.findByGroupCode(anyString())).willReturn(Optional.empty());
		given(commonCodeRepository.existsByCommonCode(anyString())).willReturn(false);

		// when
		commonCodeInitializer.init();

		// then
		verify(groupCodeRepository, atLeastOnce()).save(any(GroupCode.class));
		verify(commonCodeRepository, atLeastOnce()).save(any(CommonCode.class));
	}

	@Test
	@DisplayName("prod 프로파일일 때 DB에 데이터가 없으면 IllegalStateException을 던진다.")
	void init_NonLocalProfile_ThrowException() {
		// given
		given(env.getActiveProfiles()).willReturn(new String[]{"prod"});
		given(groupCodeRepository.findByGroupCode(anyString())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commonCodeInitializer.init())
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	@DisplayName("프로파일에 상관없이 DB에 데이터가 이미 존재하면 아무 작업도 하지 않는다.")
	void init_DataExists_NoAction() {
		// given
		given(env.getActiveProfiles()).willReturn(new String[]{"test"});

		// 데이터가 이미 있다고 가정
		GroupCode mockGroup = new GroupCode("JOBCATEGORY", "직무");
		given(groupCodeRepository.findByGroupCode(anyString())).willReturn(Optional.of(mockGroup));
		given(commonCodeRepository.existsByCommonCode(anyString())).willReturn(true);

		// when
		commonCodeInitializer.init();

		// then
		verify(groupCodeRepository, never()).save(any());
		verify(commonCodeRepository, never()).save(any());
	}
}