package com.personal.interview.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupCode {

	@Id
	@Column(name = "group_code", length = 255)
	private String groupCode;

	@Column(name = "description", nullable = false)
	private String description;

	public GroupCode(String groupCode, String description) {
		this.groupCode = groupCode;
		this.description = description;
	}
}
