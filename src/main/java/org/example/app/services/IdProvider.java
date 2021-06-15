package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class IdProvider implements InitializingBean, DisposableBean, BeanPostProcessor {
    public String provideId(Book book) {
        return this.hashCode() + "_" + book.hashCode();
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

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        logger.info("invoked postProcessBeforeInitialization for bean " + beanName);
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        logger.info("invoked postProcessAfterInitialization for bean " + beanName);
        return null;
    }
}