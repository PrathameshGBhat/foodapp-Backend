package com.cts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,JwtAuthenticationConverter jwtConverter) throws Exception{
		http.csrf(csrf->csrf.disable()).authorizeHttpRequests(auth->auth
				.requestMatchers("/admin/register","/admin/login").permitAll()
				.requestMatchers("/vendor/login","/vendor/register").permitAll()
				.requestMatchers("/customer/register","/customer/login").permitAll()
				
				.requestMatchers(HttpMethod.GET,"/vendor/**").hasAnyRole("ADMIN","VENDOR")
				
				.requestMatchers(HttpMethod.POST,"/vendor/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT,"/vendor/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.DELETE,"/vendor/**").hasRole("ADMIN")
				.anyRequest().authenticated())
		
		.oauth2ResourceServer(oauth->oauth.jwt(jwt->jwt.jwtAuthenticationConverter(jwtConverter)));
		return http.build();
	}
	
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter conv=new JwtGrantedAuthoritiesConverter();
		conv.setAuthoritiesClaimName("roles");
		conv.setAuthorityPrefix("");
		
		JwtAuthenticationConverter converter=new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(conv);
		return converter;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
