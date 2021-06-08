package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.services.LoginService;
import org.example.web.dto.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/login")
public class LoginController {

    private final Logger logger = Logger.getLogger(LoginController.class);
    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping
    public String login(Model model) {
        logger.info("GET /login returns login_page.html");
        model.addAttribute("loginForm", new LoginForm());
        return "login_page";
    }

    @PostMapping("/auth")
    public String authenticate(LoginForm loginFrom) {
        if (loginService.authenticate(loginFrom)) {
            logger.info("login OK redirect to book shelf");
            return "redirect:/books/shelf";
        } else {
            logger.info("login FAIL redirect back to login");
            return "redirect:/login";
        }
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        logger.info("GET /users returns user_page.html");
        model.addAttribute("userList", loginService.index());
        model.addAttribute("newUser", new LoginForm());
        logger.info("retrieved " + loginService.index().size() + " items");
        return "user_page";
    }

    @PostMapping
    public String registerUser(@ModelAttribute("newUser") LoginForm form) {
        logger.info("register " + form);
        loginService.registerUser(form);
        return "redirect:/login/users";
    }
}
