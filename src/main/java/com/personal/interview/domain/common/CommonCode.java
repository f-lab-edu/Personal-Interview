package com.personal.interview.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "common_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCode {

	@Id
	@Column(name = "common_code", length = 255)
	private String commonCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_code", nullable = false)
	private GroupCode groupCode;

	public CommonCode(String commonCode, GroupCode groupCode) {
		this.commonCode = commonCode;
		this.groupCode = groupCode;
	}
}
