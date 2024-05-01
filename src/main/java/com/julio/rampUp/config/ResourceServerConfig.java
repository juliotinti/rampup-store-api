package com.julio.rampUp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    private static final String admin = "Admin";

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests() //
                .antMatchers("/users/**", "/customers/**", "/orders/**", "/productOfferings/**").permitAll() //
                .antMatchers("/roles/**", "/tickets/**", "/addresses/**").hasAuthority(admin).anyRequest().denyAll(); //
    }

}
