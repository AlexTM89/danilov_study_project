package org.example.app.config;

import org.apache.log4j.Logger;
import org.example.app.services.UsersAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class AppSecurityContext extends WebSecurityConfigurerAdapter {

    final Logger logger = Logger.getLogger(AppSecurityContext.class);

    private final UsersAuthenticationProvider authenticationProvider;

    public AppSecurityContext(UsersAuthenticationProvider usersAuthenticationProvider) {
        this.authenticationProvider = usersAuthenticationProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        logger.info("populate inmemory authentication");
        auth
                .inMemoryAuthentication()
                .withUser("root")
                .password(passwordEncoder().encode("123"))
                .roles("USER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login*").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/login/auth")
                    .defaultSuccessUrl("/books/shelf", true)
                    .failureForwardUrl("/login");
        http.authenticationProvider(authenticationProvider);
    }

    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring()
                .antMatchers("/images/**")
                .antMatchers("/login/users")
                .antMatchers("/errors/*");
    }
}
