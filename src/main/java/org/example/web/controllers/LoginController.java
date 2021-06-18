package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.exceptions.BookShelfLoginException;
import org.example.app.services.LoginService;
import org.example.web.dto.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String authenticate(LoginForm loginFrom) throws BookShelfLoginException {
        if (loginService.authenticate(loginFrom)) {
            logger.info("login OK redirect to book shelf");
            return "redirect:/books/shelf";
        } else {
            logger.error("login FAIL - goto 404 page");
            throw new BookShelfLoginException("login failed - invalid username or password entered");
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

    @ExceptionHandler({BookShelfLoginException.class})
    public String handleLoginException(Model model, BookShelfLoginException e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "errors/404";
    }
}
