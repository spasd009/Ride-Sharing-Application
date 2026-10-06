package com.project;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwords(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService accounts(JdbcTemplate db){return name -> db.query("SELECT * FROM users WHERE username=?",(rs,n)->User.withUsername(rs.getString("username")).password(rs.getString("password")).roles("USER").build(),name).stream().findFirst().orElseThrow(()->new UsernameNotFoundException("Account not found"));}
 @Bean SecurityFilterChain security(HttpSecurity http,@Value("${app.security.require-https:false}") boolean https) throws Exception {
  if(https)http.requiresChannel(c->c.anyRequest().requiresSecure());
  return http.authorizeHttpRequests(a->a.requestMatchers("/","/rides","/login","/register","/style.css","/error").permitAll().anyRequest().authenticated())
   .headers(h->h
    .contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; script-src 'none'; style-src 'self'; img-src 'self'; media-src 'self'; connect-src 'self'; font-src 'self'; frame-ancestors 'none'; form-action 'self'; base-uri 'none'; object-src 'none'"))
    .referrerPolicy(r->r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
    .frameOptions(f->f.deny())
    .permissionsPolicy(p->p.policy("camera=(), microphone=(), geolocation=()")))
   .formLogin(f->f.loginPage("/login").defaultSuccessUrl("/account",true).permitAll()).logout(l->l.logoutSuccessUrl("/")).build();
 }
}
