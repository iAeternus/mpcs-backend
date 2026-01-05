package com.ricky.common.security.jwt;

import com.ricky.common.json.JsonCodec;
import com.ricky.common.properties.JwtProperties;
import com.ricky.common.security.IpJwtCookieUpdater;
import com.ricky.common.security.MdcFilter;
import com.ricky.common.tracing.TracingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@RequiredArgsConstructor
public class JwtWebSecurityConfiguration {

    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final JwtService jwtService;
    private final JwtCookieFactory jwtCookieFactory;
    private final IpJwtCookieUpdater ipJwtCookieUpdater;
    private final JwtProperties jwtProperties;
    private final JsonCodec jsonCodec;
    private final TracingService tracingService;

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        ProviderManager authenticationManager = new ProviderManager(this.jwtAuthenticationProvider);
        http.authorizeHttpRequests(registry -> registry
                        .requestMatchers(POST, "/user/registration").permitAll()
                        .requestMatchers(POST, "/login").permitAll()
                        .requestMatchers(POST, "/verification-code-login").permitAll()
                        .requestMatchers(DELETE, "/logout").permitAll()
                        .requestMatchers(POST, "/verification-codes/for-register").permitAll()
                        .requestMatchers(POST, "/verification-codes/for-login").permitAll()
                        .requestMatchers(POST, "/verification-codes/for-find-back-password").permitAll()
                        .requestMatchers("/about",
                                "/favicon.ico",
                                "/error").permitAll()
                        .anyRequest().authenticated())
                .authenticationManager(authenticationManager)
                .exceptionHandling(it -> it.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint))
                .addFilterAfter(new JwtAuthenticationFilter(authenticationManager, jsonCodec, tracingService), BasicAuthenticationFilter.class)
                .addFilterAfter(new AutoRefreshJwtFilter(jwtService,
                                jwtCookieFactory,
                                ipJwtCookieUpdater,
                                jwtProperties.getAheadAutoRefresh()),
                        AuthorizationFilter.class)
                .addFilterBefore(new MdcFilter(), ExceptionTranslationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(Customizer.withDefaults())
                .cors(AbstractHttpConfigurer::disable)
                .anonymous(configurer -> configurer.authenticationFilter(new JwtAnonymousAuthenticationFilter()))
                .csrf(AbstractHttpConfigurer::disable)
                .servletApi(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
