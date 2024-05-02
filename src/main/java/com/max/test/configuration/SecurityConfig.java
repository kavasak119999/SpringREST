package com.max.test.configuration;

import com.max.test.filter.JwtAccessTokenFilter;
import com.max.test.service.JwtProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtProviderService jwtProviderService;

    @Autowired
    public SecurityConfig(JwtProviderService jwtProviderService) {
        this.jwtProviderService = jwtProviderService;
    }

    public JwtAccessTokenFilter jwtAccessTokenFilter() {
        return new JwtAccessTokenFilter(jwtProviderService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests(auth -> auth
                        .antMatchers(HttpMethod.POST, "/api/auth/**", "/api/users").permitAll()
                        .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                        .and()
                        .addFilterBefore(jwtAccessTokenFilter(), UsernamePasswordAuthenticationFilter.class))
                .build();
    }
}