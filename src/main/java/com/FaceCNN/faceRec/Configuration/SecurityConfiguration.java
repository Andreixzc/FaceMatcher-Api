package com.FaceCNN.faceRec.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
        // @Bean
        // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //         return http.csrf(csrf -> csrf.disable())
        //                         .sessionManagement(session -> session
        //                                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        //                         .authorizeHttpRequests((request) -> request
        //                                         .requestMatchers(HttpMethod.POST, "/user/**").permitAll()
        //                                         .requestMatchers("/h2-console/**").permitAll() 
        //                                         .requestMatchers("/s3/**").permitAll()                                         
        //                                         .anyRequest().authenticated())
        //                         .headers(headers -> headers.frameOptions((frameOptions) -> frameOptions.disable())) // Disable
        //                                                                                                             // X-Frame-Options
        //                                                                                                             // for
        //                                                                                                             // h2-console
        //                         .build();
        // }
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .cors().and() // Enable CORS globally
                .csrf().disable() // Disable CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/user/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/folder/**").permitAll()
                        .requestMatchers("/s3/**").permitAll()

                    .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(options -> options.disable())); // Disable X-Frame-Options for h2-console
    
            return http.build();
        }
}       
