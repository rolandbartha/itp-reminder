package net.rolibrt.itp_reminder.config;

import jakarta.servlet.http.Cookie;
import net.rolibrt.itp_reminder.components.AppVariables;
import net.rolibrt.itp_reminder.auth.CustomAuthFailureHandler;
import net.rolibrt.itp_reminder.components.TrafficFilter;
import net.rolibrt.itp_reminder.models.Role;
import net.rolibrt.itp_reminder.services.TrustedDeviceService;
import net.rolibrt.itp_reminder.services.WebUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private WebUserDetailsService userDetailsService;
    @Autowired
    private TrafficFilter trafficFilter;
    @Autowired
    private CustomAuthFailureHandler authFailureHandler;
    @Autowired
    private TrustedDeviceService trustedDeviceService;
    @Autowired
    private AppVariables appVariables;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(trafficFilter, RememberMeAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/google*.html", "/css/**", "/js/**", "/images/**",
                                "/login", "/error/**", "/api/**")
                        .permitAll()
                        .requestMatchers("/", "/2fa/**", "/profile/**").authenticated()
                        .requestMatchers("/home", "/entries/**")
                        .hasAnyRole(Role.ADMIN.toString(), Role.MODERATOR.toString())
                        .requestMatchers("/users/**", "/admin/**")
                        .hasRole(Role.ADMIN.toString())
                        .anyRequest().fullyAuthenticated()
                )
                /*.rememberMe(remember -> remember
                        .key(appVariables.getRememberMeKey())
                        .tokenValiditySeconds(60 * 60 * 24 * 30) // 30 days
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                )
                 */
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .failureHandler(authFailureHandler)
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .addLogoutHandler((request, response, authentication) -> {
                            // Delete trusted_device cookie and remove from DB
                            if (request.getCookies() != null) {
                                for (Cookie cookie : request.getCookies()) {
                                    if (cookie.getName().equals("trusted_device")) {
                                        // Remove from DB
                                        trustedDeviceService.removeToken(cookie.getValue());

                                        // Expire cookie
                                        response.addHeader(HttpHeaders.SET_COOKIE,
                                                ResponseCookie.from("trusted_device", "")
                                                        .httpOnly(true)
                                                        .secure(appVariables.isProduction())
                                                        .path("/")
                                                        .maxAge(0)
                                                        .build().toString());
                                    }
                                }
                            }
                        })
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}