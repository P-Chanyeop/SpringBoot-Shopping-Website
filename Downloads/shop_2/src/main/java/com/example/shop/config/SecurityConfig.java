package com.example.shop.config;

import com.example.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    MemberService memberService;

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.formLogin()
                .loginPage("/members/login")    // 로그인 페이지 URL 설정
                .defaultSuccessUrl("/")     // 로그인 성공시 루트 URL로 이동
                .usernameParameter("email")     // 기본 유저 아이디 식별자를 이메일로 설정
                .failureUrl("/members/login/error")     // 로그인 실패시 URL 설정
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(("/members/logout")))   // 로그아웃 시 이동 URL 설정
                .logoutSuccessUrl("/");     // 로그아웃 성공시 루트 URL로 이동

        http.authorizeRequests()
                // 기본적으로 메인페이지, 로그인페이지, 상품 페이지, 이미지의 URL 요청은 기본유저의 접속 허용
                .mvcMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                // /admin으로 시작하는 URL은 해당 로그인 유저가 ADMIN의 역할일 경우에만 접속 허용
                .mvcMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated();

        http.exceptionHandling()
                // 인증된 사용자가 아닐경우 exceptionHandling 을 통해 Unauthorized 에러를 발생시키도록 설정
                .authenticationEntryPoint(
                        new CustomAuthenticationEntryPoint()
                );

    }

    @Override
    public void configure(WebSecurity web) throws Exception{
        // css, js, 이미지, h2-console(데이터베이스)의 인증은 불필요하기 때문에 무시
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**", "/h2-console/**");
    }

    @Bean
    // 패스워드 인코딩
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    // 사용자 인증을 위해 사용자 정보를 가져오는 서비스와 사용자 비밀번호의 암호화 방법을 설정하고,
    // Spring Security가 이 정보를 사용하여 사용자의 로그인을 처리한다.
    // 이렇게 설정된 사용자 정보와 암호화 방법에 따라 Spring Security는 사용자 인증을 수행한다.
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(memberService)
                .passwordEncoder(passwordEncoder());
    }


}
