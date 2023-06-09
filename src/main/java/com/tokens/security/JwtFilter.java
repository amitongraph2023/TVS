package com.tokens.security;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tokens.utils.JwtCookieUtil;
import com.tokens.utils.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CustomUserDetailsService service;

	@Autowired
	private JwtCookieUtil jwtCookieUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException {

		boolean isException = false;
		String userName = null;
		Optional<Cookie> tokenCookie = jwtCookieUtil.getTokenCookieByName(httpServletRequest,
				JwtCookieUtil.ID_TOKEN_COOKIE_NAME);

		String token = null;

		if (httpServletRequest.getHeader("responsetype") == null) {

			if (tokenCookie.isPresent()) {
				httpServletResponse.setHeader("Authorization", "Bearer " + tokenCookie.get().getValue());
				try {
					userName = jwtUtil.extractUsername(tokenCookie.get().getValue());
					token = tokenCookie.get().getValue();
				} catch (IllegalArgumentException e) {
					logger.error("Exception caught while checking token : " + e.getMessage());
				} catch (ExpiredJwtException e) {
					logger.error("Token Expired when cookie present: " + e.getMessage());
				} catch (JwtException e) {
					logger.error("Exception caught while checking token : " + e.getMessage());
				}
			}
		} else {
			String authorizationHeader = httpServletRequest.getHeader("Authorization");
			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				token = authorizationHeader.substring(7);
				try {
					userName = jwtUtil.extractUsername(token);
				} catch (IllegalArgumentException e) {
					isException = true;
					logger.error("Exception caught while checking token : " + e.getMessage());
				} catch (ExpiredJwtException e) {
					isException = true;
					logger.error("Token Expired : " + e.getMessage());
				} catch (JwtException e) {
					isException = true;
					logger.error("Exception caught while checking token : " + e.getMessage());
				}
			}
		}

		if (isException) {
			httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpServletResponse.getWriter().write("Authentication failed: token expired or invalid token");
			return;
		}

		
		if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			UserDetails userDetails = service.loadUserByUsername(userName);

			if (jwtUtil.validateToken(token, userDetails)) {

				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}

		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}
}
