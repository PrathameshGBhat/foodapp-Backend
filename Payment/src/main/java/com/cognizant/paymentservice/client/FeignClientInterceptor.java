package com.cognizant.paymentservice.client;

import java.net.Authenticator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class FeignClientInterceptor implements RequestInterceptor{

	@Override
	public void apply(RequestTemplate template) {
		Authentication auth=SecurityContextHolder.getContext().getAuthentication();
		
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			String token=jwtAuth.getToken().getTokenValue();
			template.header("Authorization", "Bearer " + token);
		}
	}
}
