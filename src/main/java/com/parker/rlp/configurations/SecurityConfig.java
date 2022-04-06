package com.parker.rlp.configurations;

import com.parker.rlp.services.SecurityUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    SecurityUserService securityUserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()

                .mvcMatchers( "/update", "/checkout-book/{id}",
                        "/return-book/{id}", "/dashboard", "/books/checkout/{bookId}",
                        "/books/return/{bookId}/{userId}").authenticated()

                .mvcMatchers("/delete{id}", "/user-list", "/user-list", "/books/new", "/books/save",
                        "/books/update/{id}", "/books/delete/{id}", "/books/edit/{id}", "/delete/{id}", "/user-list",
                        "/user-books/{id}", "/user/rental-history/{id}", "/books/rental-history/{id}",
                        "/bookcase/new", "/bookcase/save", "/subject").hasRole("ADMIN")

                .mvcMatchers(HttpMethod.POST, "/update/{id}").hasRole("ADMIN")

                .mvcMatchers(HttpMethod.GET, "/admin/loadBookCases").hasRole("ADMIN")

                .mvcMatchers(HttpMethod.GET, "/", "/webjars/**", "/css/**",
                        "/login/**", "/images/**", "/register", "/books", "/books/available",
                        "/books/cover-image/{bookId}", "books/new-arrivals", "/books/search").permitAll()

                .mvcMatchers(HttpMethod.POST, "/register", "/register-form", "/sign-in",
                        "/login-error").permitAll()

                .and()

                .formLogin()
                .loginPage("/login").permitAll()
                .failureUrl("/login-error").permitAll()

                .and()

                .logout()
                .logoutSuccessUrl("/");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(securityUserService)
                .passwordEncoder(passwordEncoder());
    }
}
