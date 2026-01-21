package com.raisedeveloper.server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.global.security.jwt.JwtAuthenticationFilter;
import com.raisedeveloper.server.global.security.jwt.JwtExceptionFilter;
import com.raisedeveloper.server.global.security.jwt.JwtTokenProvider;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtTokenProvider jwtTokenProvider,
		ObjectMapper objectMapper
	) throws Exception {

		JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(jwtTokenProvider);
		JwtExceptionFilter jwtExceptionFilter = new JwtExceptionFilter(objectMapper);

		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/auth/**",
					"/health",
					"/actuator/**"
				).permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
			)

			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)

			.addFilterBefore(jwtExceptionFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}

