package it.insiel.gcs.prove;

import it.insiel.nazca.security.commons.StandardLoginResultCodeDecoder;
import it.insiel.nazca.security.loginfvg.LoginFvgAuthenticationDetailsSource;
import it.insiel.nazca.security.loginfvg.LoginFvgAuthenticationProvider;
import it.insiel.nazca.security.loginfvg.LoginFvgPreAuthenticatedProcessingFilter;
import it.insiel.nazca.security.loginfvg.StandardLoginFvgUserDetailsDtoExtractor;
import it.insiel.nazca.security.ws3.client.AxisWsExtLoginSAMLClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /*
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .formLogin().and()
            .httpBasic();
    }
    */

    @Value("${spid.endpoint}")
    private String endpoint;
    @Value("${loginfvg.domain}")
    private String domain;
    @Value("${loginfvg.app}")
    private String app;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(loginFvgAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .addFilter(loginFvgPreAuthenticatedProcessingFilter())
                .authorizeRequests().antMatchers("/").permitAll()
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/**").hasAnyRole("NAZCA_USER")
                .and()
                .formLogin()
                .loginPage("/login").permitAll();
    }

    @Bean
    public LoginFvgAuthenticationProvider loginFvgAuthenticationProvider() {
        return new LoginFvgAuthenticationProvider();
    }

    @Bean
    public LoginUrlAuthenticationEntryPoint webappAuthenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/login");
    }

    @Bean
    public AxisWsExtLoginSAMLClient wsExtLoginSAMLClient() {
        AxisWsExtLoginSAMLClient client = new AxisWsExtLoginSAMLClient();
        client.setEndpoint("https://fvgaccountws.regione.fvg.it/wsfvg/services/WsExtLoginSAML");
        return client;
    }

    @Bean
    public StandardLoginResultCodeDecoder resultCodeDecoder() {
        return new StandardLoginResultCodeDecoder();
    }

    @Bean
    public StandardLoginFvgUserDetailsDtoExtractor userDetailsDtoExtractor() {
        StandardLoginFvgUserDetailsDtoExtractor sle = new StandardLoginFvgUserDetailsDtoExtractor();
        sle.setApplicationId("103681");
        sle.setDomainId("541");
        sle.setResponseHandler(resultCodeDecoder());
        sle.setWs3Client(wsExtLoginSAMLClient());
        return sle;
    }

    @Bean
    public LoginFvgAuthenticationDetailsSource authenticationDetailsSource() {
        LoginFvgAuthenticationDetailsSource ds = new LoginFvgAuthenticationDetailsSource();
        ds.setUserDetailsExtractor(userDetailsDtoExtractor());
        String[] attributes = new String[] { "controllore", "dichiarante", "amministratore" };
        ds.setAttributesToConsider(attributes);
        return ds;
    }

    @Bean
    public LoginFvgPreAuthenticatedProcessingFilter loginFvgPreAuthenticatedProcessingFilter() throws Exception {
        LoginFvgPreAuthenticatedProcessingFilter filter = new LoginFvgPreAuthenticatedProcessingFilter();
        filter.setAuthenticationDetailsSource(authenticationDetailsSource());
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

}