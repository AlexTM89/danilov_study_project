package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class UsersAuthenticationProvider implements AuthenticationProvider {

    private LoginService loginService;
    private final Logger logger = Logger.getLogger(UsersAuthenticationProvider.class);

    @Autowired
    public UsersAuthenticationProvider(LoginService service) {
        this.loginService = service;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final UsernamePasswordAuthenticationToken upAuth = (UsernamePasswordAuthenticationToken) authentication;
        final String name = (String) authentication.getPrincipal();

        final String password = (String) upAuth.getCredentials();

        logger.info("gathered name and password for authentication: " + name + ", " + password);

        if (loginService.authenticate(new LoginForm(name, password))) {
            logger.info("passed check from loginService");
            Object principal = authentication.getPrincipal();
            UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                    principal,
                    authentication.getCredentials(),
                    Collections.emptyList());
            result.setDetails(authentication.getDetails());

            return result;
        } else {
            throw new BadCredentialsException("illegal username or password given");
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return true;
    }

}