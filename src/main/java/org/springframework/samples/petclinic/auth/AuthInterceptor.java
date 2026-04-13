package org.springframework.samples.petclinic.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler) throws Exception {
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("user") != null) {
			User user = (User) session.getAttribute("user");
			if (Boolean.TRUE.equals(user.getForcePasswordChange())
					&& !request.getRequestURI().equals("/change-password")
					&& !request.getRequestURI().equals("/logout")) {
				response.sendRedirect("/change-password");
				return false;
			}
			return true;
		}
		response.sendRedirect("/login");
		return false;
	}

}
