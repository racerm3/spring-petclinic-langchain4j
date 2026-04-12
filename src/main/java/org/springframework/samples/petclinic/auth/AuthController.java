package org.springframework.samples.petclinic.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/login")
	public String showLoginForm() {
		return "auth/login";
	}

	@PostMapping("/login")
	public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session,
			Model model) {
		User user = userRepository.findByUsername(username);
		if (user != null && user.getPassword().equals(password)) {
			session.setAttribute("user", user);
			if (Boolean.TRUE.equals(user.getForcePasswordChange())) {
				return "redirect:/change-password";
			}
			return "redirect:/";
		}
		model.addAttribute("error", "Invalid username or password");
		return "auth/login";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}

	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "auth/register";
	}

	@PostMapping("/register")
	public String processRegister(User user, BindingResult result, Model model) {
		if (userRepository.findByUsername(user.getUsername()) != null) {
			model.addAttribute("error", "Username is already taken");
			return "auth/register";
		}
		if (user.getPassword() == null || user.getPassword().length() < 8) {
			model.addAttribute("error", "Password must be at least 8 characters");
			return "auth/register";
		}
		user.setForcePasswordChange(false); // Newly registered user set their own
											// password
		userRepository.save(user);
		return "redirect:/login?registered=true";
	}

	@GetMapping("/forgot-password")
	public String showForgotPasswordForm() {
		return "auth/forgot-password";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String username, @RequestParam String newPassword, Model model) {
		User user = userRepository.findByUsername(username);
		if (user == null) {
			model.addAttribute("error", "Username not found");
			return "auth/forgot-password";
		}
		if (newPassword == null || newPassword.length() < 8) {
			model.addAttribute("error", "Password must be at least 8 characters");
			return "auth/forgot-password";
		}
		user.setPassword(newPassword);
		user.setForcePasswordChange(false);
		userRepository.save(user);
		return "redirect:/login?passwordChanged=true";
	}

	@GetMapping("/change-password")
	public String showChangePasswordForm(HttpSession session) {
		if (session.getAttribute("user") == null) {
			return "redirect:/login";
		}
		return "auth/change-password";
	}

	@PostMapping("/change-password")
	public String processChangePassword(@RequestParam String newPassword, HttpSession session, Model model) {
		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null) {
			return "redirect:/login";
		}
		if (newPassword == null || newPassword.length() < 8) {
			model.addAttribute("error", "Password must be at least 8 characters");
			return "auth/change-password";
		}
		User dbUser = userRepository.findByUsername(sessionUser.getUsername());
		dbUser.setPassword(newPassword);
		dbUser.setForcePasswordChange(false);
		userRepository.save(dbUser);

		session.setAttribute("user", dbUser);
		return "redirect:/";
	}

}
