package id.ac.ui.cs.advprog.bidmart.backend.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ServiceTokenFilter serviceTokenFilter;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, ServiceTokenFilter serviceTokenFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.serviceTokenFilter = serviceTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers(HttpMethod.POST,
                        "/auth/register",
                        "/api/auth/register",
                        "/auth/login",
                        "/api/auth/login",
                        "/auth/refresh",
                        "/api/auth/refresh",
                        "/auth/refresh-v2",
                        "/api/auth/refresh-v2",
                        "/auth/verify-email",
                        "/api/auth/verify-email",
                        "/auth/2fa/verify",
                        "/api/auth/2fa/verify",
                        "/auth/forgot-password",
                        "/api/auth/forgot-password",
                        "/auth/reset-password",
                        "/api/auth/reset-password"
                ).permitAll()

                .requestMatchers(HttpMethod.GET,
                        "/auth/verify",
                        "/api/auth/verify",
                        "/auth/reset-password/validate",
                        "/api/auth/reset-password/validate"
                ).permitAll()
                .requestMatchers("/health").permitAll()

                .requestMatchers("/internal/users/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/me").authenticated()
                .requestMatchers("/users/me/**").authenticated()
                .requestMatchers("/auth/2fa/**").authenticated()
                .requestMatchers("/api/auth/2fa/**").authenticated()
                .requestMatchers("/api/auth/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
        )
                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
