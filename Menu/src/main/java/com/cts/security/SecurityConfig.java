package com.cts.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health", "/actuator/info").permitAll()
            		.requestMatchers("/api/menu/**").hasAnyRole("VENDOR","CUSTOMER").requestMatchers("/api/category/**").hasAnyRole("VENDOR","ADMIN").anyRequest().authenticated())
           
            .oauth2ResourceServer(oauth2->oauth2.jwt(jwt->jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
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
}
 

