package com.crs.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collection;

import java.util.List;

import java.util.Map;

import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.core.convert.converter.Converter;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration

@EnableWebFluxSecurity

public class ApiGatewaySecurityConfig {

	@Bean

	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

		http.csrf(ServerHttpSecurity.CsrfSpec::disable);

		http.authorizeExchange(ex -> ex.pathMatchers("/admin/register",

				"/admin/login").permitAll()

				.pathMatchers("/customer/register",

						"/customer/login")
				.permitAll()

				.pathMatchers("/vendor/register",

						"/vendor/login")
				.permitAll()

				.pathMatchers("/vendor/**").permitAll()

				.pathMatchers("/admin/**").hasRole("ADMIN")

				.pathMatchers("/vendor/**").hasAnyRole("VENDOR", "ADMIN")

				.pathMatchers("/customer/**").hasRole("CUSTOMER")

//				.pathMatchers(HttpMethod.POST,"/restaurants/api/restaurants/**").hasRole("ADMIN")

//				.pathMatchers(HttpMethod.PUT,"/restaurants/api/restaurants/**").hasRole("ADMIN")

//				.pathMatchers(HttpMethod.DELETE,"/restaurants/api/restaurants/**").hasRole("ADMIN")

//				.pathMatchers(HttpMethod.GET,"/restaurants/api/restaurants/**").hasAnyRole("ADMIN","VENDOR","CUSTOMER")

				.pathMatchers(HttpMethod.POST, "/api/menu/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.PUT, "/api/menu/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.DELETE, "/api/menu/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.GET, "/api/menu/**").hasAnyRole("ADMIN", "VENDOR", "CUSTOMER")

				.pathMatchers(HttpMethod.POST, "/menu-service/api/category/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.PUT, "/menu-service/api/category/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.DELETE, "/menu-service/api/category/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.GET, "/menu-service/api/category/**").hasAnyRole("ADMIN", "VENDOR", "CUSTOMER")
				
				.pathMatchers(HttpMethod.POST, "/api/inventory/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("VENDOR")

				.pathMatchers(HttpMethod.GET, "/api/inventory/**").hasRole("VENDOR")
				
				.pathMatchers(HttpMethod.POST, "/api/cart/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.PUT, "/api/cart/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.DELETE, "/api/cart/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.GET, "/api/cart/**").hasRole("CUSTOMER")
				
				.pathMatchers(HttpMethod.POST, "/api/orders/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.PUT, "/api/orders/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("VENDOR","CUSTOMER")
				
				.pathMatchers(HttpMethod.POST, "/payment/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.PUT, "/payment/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.DELETE, "/payment/**").hasRole("CUSTOMER")

				.pathMatchers(HttpMethod.GET, "/payment/**").hasAnyRole("VENDOR","CUSTOMER","ADMIN")
				
				.pathMatchers(HttpMethod.POST, "/api/report/**").hasRole("ADMIN")

				.pathMatchers(HttpMethod.DELETE, "/api/report/**").hasRole("ADMIN")

				.pathMatchers(HttpMethod.GET, "/api/report/**").hasRole("ADMIN")

				.pathMatchers(HttpMethod.GET, "/feedback/**").hasAnyRole("VENDOR","ADMIN","CUSTOMER")
				
				.pathMatchers(HttpMethod.POST, "/feedback/**").hasRole("CUSTOMER")

				.anyExchange()

				.authenticated()

		);

		http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(

				new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter())

		))

		);

		return http.build();

	}

	@Bean

	public JwtAuthenticationConverter jwtAuthenticationConverter() {

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesExtractor());

		return converter;

	}

	private Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesExtractor() {

		return jwt -> {

			List<GrantedAuthority> authorities = new ArrayList<>();

			Object rolesClaim = jwt.getClaim("roles");

			if (rolesClaim instanceof Collection<?>) {

				authorities.addAll(((Collection<?>) rolesClaim).stream()

						.map(Object::toString)

						.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)

						.map(SimpleGrantedAuthority::new)

						.collect(Collectors.toList()));

			} else {

				Object realmAccess = jwt.getClaim("realm_access");

				if (realmAccess instanceof Map<?, ?>) {

					Object rroles = ((Map<?, ?>) realmAccess).get("roles");

					if (rroles instanceof Collection<?>) {

						Collection<?> roles = (Collection<?>) rroles;

						authorities.addAll(

								roles.stream()

										.map(Object::toString)

										.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)

										.map(SimpleGrantedAuthority::new)

										.collect(Collectors.toList()));

					}

				}

			}

			Object scopeClaim = jwt.getClaim("scope");

			if (scopeClaim instanceof String scopeStr) {

				authorities.addAll(Arrays.stream(scopeStr.split(" "))

						.map(s -> "SCOPE_" + s)

						.map(SimpleGrantedAuthority::new)

						.collect(Collectors.toList()));

			} else if (scopeClaim instanceof Collection<?> scopes) {

				authorities.addAll(scopes.stream()

						.map(Object::toString)

						.map(s -> "SCOPE_" + s)

						.map(SimpleGrantedAuthority::new)

						.collect(Collectors.toList()));

			}

			return authorities;

		};

	}

}
