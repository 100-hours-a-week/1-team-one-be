package com.raisedeveloper.server.domain.user.domain;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.raisedeveloper.server.domain.common.domain.SoftDeleteEntity;
import com.raisedeveloper.server.domain.common.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@SQLDelete(sql = """
	UPDATE users
	SET deleted_at = NOW(), updated_at = NOW()
	WHERE id = ? AND deleted_at IS NULL
	""")
@SQLRestriction("deleted_at IS NULL")
public class User extends SoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(nullable = false)
	private boolean isOnboardingCompleted;

	public User(String email, String password) {
		this.email = email;
		this.password = password;
		this.role = Role.USER;
		this.isOnboardingCompleted = false;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void onboardingCompleted() {
		this.isOnboardingCompleted = true;
	}
}
