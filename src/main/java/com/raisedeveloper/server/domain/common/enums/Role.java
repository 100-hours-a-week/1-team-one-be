package com.raisedeveloper.server.domain.common.enums;

public enum Role {

	ADMIN,
	USER;

	public String toAuthority() {
		return "ROLE_" + name();
	}
}
