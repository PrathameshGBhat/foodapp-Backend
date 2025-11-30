package com.cts.config;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import reactor.core.publisher.Mono;

public class KeycloakJwtConverter implements Converter<Jwt,Mono<AbstractAuthenticationToken>> {

	private final JwtGrantedAuthoritiesConverter authoritiesConverter;
	
	public KeycloakJwtConverter() {
		authoritiesConverter=new JwtGrantedAuthoritiesConverter();
		authoritiesConverter.setAuthoritiesClaimName("roles");
		authoritiesConverter.setAuthorityPrefix("ROLE_");
	}
	
	@Override
	public Mono<AbstractAuthenticationToken> convert(Jwt source) {
		Collection<GrantedAuthority> authorities=authoritiesConverter.convert(source);
		return Mono.just(new JwtAuthenticationToken(source,authorities));
	}

}
