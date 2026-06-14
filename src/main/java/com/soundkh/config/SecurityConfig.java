package com.soundkh.config;

import com.soundkh.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // channel/track writes — must come BEFORE generic GET permitAll
                        .requestMatchers(HttpMethod.POST, "/api/tracks/**").hasAnyRole("SUPER_STAR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/channels/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/channels/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/tracks/**").hasAnyRole("CREATOR", "SUPER_STAR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/channels/**").hasAnyRole("CREATOR", "SUPER_STAR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tracks/**").hasAnyRole("CREATOR", "SUPER_STAR", "ADMIN")
                        // authenticated-only endpoints
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/channels/mine").authenticated()
                        .requestMatchers("/api/access-requests/**").authenticated()
                        .requestMatchers("/api/channel-access-requests/**").authenticated()
                        .requestMatchers("/api/playlists/**").authenticated()
                        .requestMatchers("/api/subscriptions/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/comments/**").authenticated()
                        .requestMatchers("/api/tracks/*/like").authenticated()
                        .requestMatchers("/api/tracks/feed").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tracks/*/stream").authenticated()
                        // public GET reads
                        .requestMatchers(HttpMethod.GET, "/api/tracks/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/channels/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
