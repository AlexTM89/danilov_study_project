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
    public LoginController(LoginService loginService, BookShelfController bookShelfController) {
        this.loginService = loginService;
    }

    @GetMapping
    public String login(Model model) {
        logger.info("GET /login returns login_page.html");
        model.addAttribute("loginForm", new LoginForm());
        return "login_page";
    }

//    @PostMapping("/auth")
//    данный обработчик не работает из-за использования custom authenticate provider
//    public String authenticate(LoginForm loginFrom, Model model) throws BookShelfLoginException {
//        if (loginService.authenticate(loginFrom)) {
//            return "redirect:/books/shelf";
//        } else {
//            return "redirect:/errors/404";
//        }
//        logger.info("invoke authenticate method POST for /login/auth");
//        if (loginService.authenticate(loginFrom)) {
//            logger.info("login OK redirect to book shelf");
//            return "redirect:/books/shelf";
//        } else {
//            logger.error("login FAIL - goto 404 page");
//            throw new BookShelfLoginException("login failed - invalid username or password entered");
//        }
//    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        logger.info("GET /users returns user_page.html");
        model.addAttribute("userList", loginService.index());
        model.addAttribute("newUser", new LoginForm());
        logger.info("retrieved " + loginService.index().size() + " items");
        return "user_page";
    }

    @PostMapping("/users")
    public String registerUser(@ModelAttribute("newUser") LoginForm form) {
        logger.info("register " + form);
        loginService.registerUser(form);
        return "redirect:/login/users";
    }

    @ExceptionHandler(Exception.class)
    public String handleLoginException(Model model, Exception e) {
        logger.info("handle error " + e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "errors/404";
    }
}
