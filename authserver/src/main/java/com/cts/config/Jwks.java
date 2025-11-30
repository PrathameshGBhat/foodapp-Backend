package com.cts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

@Configuration
public class Jwks {
	
	@Bean
	public com.nimbusds.jose.jwk.RSAKey rsaKey() throws Exception{
		return new RSAKeyGenerator(2048)
				.keyID("auth-server-key")
				.generate();
	}
	
}
			
		
	


