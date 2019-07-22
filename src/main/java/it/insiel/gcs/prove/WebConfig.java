package it.insiel.gcs.prove;

import it.insiel.gcs.prove.controllers.LoginController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "it.insiel.gcs.prove.controllers")
public class WebConfig {

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("/WEB-INF/classes/");
        vr.setSuffix(".jsp");
        return vr;
    }

    @Bean
    public LoginController loginController(){
        LoginController lg = new LoginController();
        lg.setDomainId("541");
        lg.setApplicationId("103681");
        lg.setIdpServerUrl("https://fvgaccountws.regione.fvg.it/wsfvg/administrator/estensione/loginAccount/IdPServer.jsp");
        lg.setAttAuthorityUrl("https://fvgaccountws.regione.fvg.it/wsfvg/administrator/estensione/loginAccount/ControlUsernamePasswordAttrIdP.jsp");
        lg.setFinalApplicationCallbackUrl("/");
        lg.setFailedLoginUrl("/login");
        return lg;
    }
}
