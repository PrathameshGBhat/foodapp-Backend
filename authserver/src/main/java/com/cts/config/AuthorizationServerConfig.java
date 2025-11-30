package com.cts.config;


import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class AuthorizationServerConfig {
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		
		TokenSettings tokenSettings=TokenSettings.builder()
				.accessTokenTimeToLive(Duration.ofMinutes(10))
				.refreshTokenTimeToLive(Duration.ofHours(1))
				.reuseRefreshTokens(false)
				.build();
		
		RegisteredClient adminClient=RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("adminclient")
				.clientSecret(passwordEncoder.encode("admin"))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("https://oauth.pstmn.io/v1/callback")
				.scope("read")
				.scope("write")
				.tokenSettings(tokenSettings)
				.build();
		
		RegisteredClient vendorClient=RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("vendorclient")
				.clientSecret(passwordEncoder.encode("vendor"))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("https://oauth.pstmn.io/v1/callback")
				.scope("read")
				.scope("write")
				.tokenSettings(tokenSettings)
				.build();
		
		RegisteredClient customerClient=RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("customerclient")
				.clientSecret(passwordEncoder.encode("customer"))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("https://oauth.pstmn.io/v1/callback")
				.scope("read")
				.scope("write")
				.tokenSettings(tokenSettings)
				.build();
		
		RegisteredClientRepository registeredClientRepository=new InMemoryRegisteredClientRepository(adminClient,vendorClient,customerClient);
		return registeredClientRepository;
	}
	
	@Bean
	public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey){
		
		JWKSet jwkSet=new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
		
	}
	
	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwtEncoder(jwkSource);
	}
	
	@Bean
	public AuthorizationServerSettings providerSettings() {
		return AuthorizationServerSettings.builder()
				.issuer("http://localhost:9001")
				.build();
	}
	
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(){
		return context->{
			if(context.getPrincipal()!=null && context.getPrincipal().getAuthorities()!=null) {
				List<String> roles=context.getPrincipal().getAuthorities().stream()
						.map(auth->auth.getAuthority()).collect(Collectors.toList());
			context.getClaims().claim("roles", roles);
			}
		
	};
	
	
	

	
}
}
