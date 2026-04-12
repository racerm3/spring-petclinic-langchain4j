package org.springframework.samples.petclinic.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.auth.AuthInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private AuthInterceptor authInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns("/login", "/register", "/forgot-password", "/logout", "/resources/**", "/webjars/**",
					"/css/**", "/images/**", "/js/**", "/error", "/chat/**");
	}

}
