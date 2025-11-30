package com.cts.config;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class ClientSessionIsolationFilter extends OncePerRequestFilter{

	private static final String SESSION_CLIENT_ATTR="OAUTH_CLIENT_ID";
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		
		
		String path=request.getRequestURI();
		String clientId=request.getParameter("client_id");
		
		if((path.startsWith("oauth2/authorize") || path.startsWith("/login")) && clientId!=null) {
			HttpSession session=request.getSession(false);
				
			if(session==null) {
				HttpSession newSession=request.getSession(true);
				newSession.setAttribute(SESSION_CLIENT_ATTR,clientId);
			}else {
					Object existing=session.getAttribute(SESSION_CLIENT_ATTR);
					
					if(existing==null) {
						session.setAttribute(SESSION_CLIENT_ATTR, clientId);
					}else if(!clientId.equals(existing)) {
						session.invalidate();
						SecurityContextHolder.clearContext();
						
						HttpSession newSession=request.getSession(true);
						newSession.setAttribute(SESSION_CLIENT_ATTR, clientId);
			
				}
			}
		}
				
		filterChain.doFilter(request, response);
		
	}

}
