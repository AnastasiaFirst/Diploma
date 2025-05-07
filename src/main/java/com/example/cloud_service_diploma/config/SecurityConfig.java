package com.example.cloud_service_diploma.config;

import com.example.cloud_service_diploma.security.JwtRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests

                                .requestMatchers("/cloud/login").permitAll()
                                .requestMatchers("/cloud/file").permitAll()
                                .requestMatchers("/cloud/list").permitAll()

                                .requestMatchers("/cloud/file", "/cloud/list").authenticated()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated())

                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                        httpSecurityExceptionHandlingConfigurer
                                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))


                .logout(httpSecurityLogoutConfigurer ->
                        httpSecurityLogoutConfigurer
                                .logoutUrl("/cloud/logout")
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<OpenEntityManagerInViewFilter> openEntityManagerInViewFilter() {
        FilterRegistrationBean<OpenEntityManagerInViewFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new OpenEntityManagerInViewFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}