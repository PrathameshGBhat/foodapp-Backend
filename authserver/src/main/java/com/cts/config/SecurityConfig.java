package com.cts.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	
	
	@Bean
	public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http)throws Exception{
		
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		
		http
		.csrf(csrf->csrf.ignoringRequestMatchers("/oauth2/**"))
		.sessionManagement(session->session.sessionFixation().migrateSession().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
		.securityContext(securityContext->securityContext.requireExplicitSave(false))
		.exceptionHandling(exceptions->exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
		.logout(logout->logout.logoutUrl("/logout")
				.invalidateHttpSession(true).deleteCookies("JSESSIONID"));
		return http.build();
	}
	@Bean
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception{
		http.addFilterBefore(new ClientSessionIsolationFilter(), UsernamePasswordAuthenticationFilter.class)
		.sessionManagement(session->session.sessionFixation().migrateSession()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
		.securityContext(securityContext->securityContext.requireExplicitSave(true))
		
		.authorizeHttpRequests(
				auth->auth.requestMatchers("/login","/error","/oauth2/**").permitAll().anyRequest().authenticated())
		.formLogin(withDefaults())
		.oauth2ResourceServer(oauth2->oauth2.jwt(withDefaults()));
		
		return http.build();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
		return configuration.getAuthenticationManager();
	}
	
				
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
