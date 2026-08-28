package com.example.peerfolio.global.security;

import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtProvider jwtProvider;
	private final TokenBlacklistService tokenBlacklistService;
	private final UserRepository userRepository;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return CorsUtils.isPreFlightRequest(request);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String token = resolveToken(request);

		if (token != null
				&& jwtProvider.validateToken(token)
				&& !tokenBlacklistService.isBlacklisted(token)) {

			Long userId = jwtProvider.getUserId(token);
			User user = userRepository.findById(userId).orElse(null);

			if (user != null) {
				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(
								user,
								null,
								Collections.emptyList()
						);

				authentication.setDetails(
						new WebAuthenticationDetailsSource()
								.buildDetails(request)
				);

				SecurityContextHolder.getContext()
						.setAuthentication(authentication);
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String authorization =
				request.getHeader(AUTHORIZATION_HEADER);

		if (authorization == null
				|| !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}

		return authorization.substring(BEARER_PREFIX.length());
	}
}
