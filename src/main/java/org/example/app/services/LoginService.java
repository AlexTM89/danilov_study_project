package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Service
public class LoginService {

    private Logger logger = Logger.getLogger(LoginService.class);
    private final LoginRepository loginRepository;

    @Autowired
    public LoginService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public boolean authenticate(LoginForm loginFrom) {
        logger.info("try auth with user-form: " + loginFrom);
        LoginForm form = loginRepository.retrieveAll()
                .stream()
                .filter(login -> login.getPassword().equals(loginFrom.getPassword()) && login.getUsername().equals(loginFrom.getUsername()))
                .findFirst()
                .orElse(null);
        if (form == null) {
            logger.error("cannot auth: no user with name " + loginFrom.getUsername() + " and password " + loginFrom.getPassword());
        }
        return form != null;
    }

    public List<LoginForm> index() {
        return loginRepository.retrieveAll();
    }

    public void registerUser(LoginForm user) {
        loginRepository.store(user);
    }

}
