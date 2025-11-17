package com.relieflink.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //implements the WevMvcConfigurer to manage the MVC design

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //created a Interceptor registry and added the AuthInterceptor class object to the registry.
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**");//dont know this line
    }
}
