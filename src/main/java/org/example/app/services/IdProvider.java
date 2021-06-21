package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class IdProvider implements InitializingBean, DisposableBean {
    public Integer provideId(Book book) {
        return book.getId();
    }

    private final Logger logger = Logger.getLogger(IdProvider.class);

    private void initMethod() {
        logger.info("invoked IdProvider specific init-method");
    }

    private void destroyMethod() {
        logger.info("invoked IdProvider specific destroy-method");
    }

    private void defaultInit() {
        logger.info("invoked default init method");
    }

    private void defaultDestroy() {
        logger.info("invoked default destroy method");
    }

    @Override
    public void destroy() throws Exception {
        logger.info("invoked destroy method from DisposableBean interface");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("run afterPropertiesSet method from InitializingBean interface");
    }

    @PostConstruct
    private void idProviderPostConstruct() {
        logger.info("invoked post construct method annotaded @PostConstruct");
    }

    @PreDestroy
    private void idProviderPreDestroy() {
        logger.info("invoked pre destroy method annotated @PreDestroy");
    }
}
