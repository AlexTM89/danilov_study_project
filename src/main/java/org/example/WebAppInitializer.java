package org.example;

import org.apache.log4j.Logger;
import org.example.app.config.RootApplicationContext;
import org.example.web.config.WebContextConfig;
import org.h2.server.web.WebServlet;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

@Configuration
public class WebAppInitializer implements WebApplicationInitializer {

    Logger logger = Logger.getLogger(WebApplicationInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(RootApplicationContext.class);
        servletContext.addListener(new ContextLoaderListener(appContext));

        logger.info("registered app context");

        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(WebContextConfig.class);

        logger.info("registered web context");

        DispatcherServlet dispatcherServlet = new DispatcherServlet(webContext);
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        logger.info("dispatcher ready");

        ServletRegistration.Dynamic databaseServlet = servletContext.addServlet("h2-console", new WebServlet());
        databaseServlet.setLoadOnStartup(2);
        databaseServlet.addMapping("/console/*");

        logger.info("database h2-console config ready");
    }
}
