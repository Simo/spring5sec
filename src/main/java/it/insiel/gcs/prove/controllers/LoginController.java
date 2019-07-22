package it.insiel.gcs.prove.controllers;

import it.insiel.nazca.security.loginfvg.AttributiChiamataLoginFvgDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
@RequestMapping({"/login"})
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(it.insiel.nazca.security.loginfvg.LoginController.class);
    public static final String URL_REQUEST = "URL_REQUEST";
    public static final String CODICE_APPLICAZIONE = "COD_APPLICAZIONE";
    public static final String CODICE_DOMINIO = "COD_DOMINIO";
    public static final String ServiceName = "ServiceName";
    public static final String ATTRIBUTES_REDIRECT_URL_KEY = "attributesRedirectUrl";
    public static final String APP_FINAL_CALLBACK_URL_KEY = "finalCallbackUrl";
    public static final String IDP_SERVER_URL_KEY = "idpServerUrl";
    public static final String APPLICATION_ID_KEY = "applicationId";
    public static final String DOMAIN_ID_KEY = "domainId";
    public static final String SERVICE_NAME_KEY = "serviceName";
    public static final String SERVICE_NAME_PARAM = "ServiceName";
    @SuppressWarnings("unused")
    private static final String EXT_ATTRIBUTES_D = "extAttributes/%d";
    private String loginView = "login";
    private String domainId;
    private String serviceName;
    private String hostPortControl;
    private String applicationId;
    private String idpServerUrl;
    private String attAuthorityUrl;
    private String serviceProviderUrl;
    private boolean checkForDeleghe;
    private String risorsaForDeleghe;
    private String domainIdForDeleghe;
    private String applicationIdForDeleghe;
    private String finalApplicationCallbackUrl = "/index.jsp";
    private String failedLoginUrl = "/";
    private String baseUrl;
    private List<AttributiChiamataLoginFvgDto> otherCalls;
    public LoginController() {
    }
    @RequestMapping(
            method = {RequestMethod.GET}
    )
    public ModelAndView getLoginView(HttpServletRequest request) {
        ModelAndView result = new ModelAndView(this.loginView);
        result.addObject("domainId", this.domainId);
        result.addObject("applicationId", this.applicationId);
        String srvName_p = StringUtils.defaultString(request.getParameter("ServiceName"));
        if(StringUtils.isNotBlank(this.serviceName)) {
            String srvName = StringUtils.isNotBlank(srvName_p)?srvName_p:this.serviceName;
            result.addObject("serviceName", srvName);
            request.getSession().setAttribute("ServiceName", srvName_p);
        }
        result.addObject("idpServerUrl", this.idpServerUrl);
        result.addObject("finalCallbackUrl", this.getFinalCallbackUrl(request));
        result.addObject("attributesRedirectUrl", this.getAttributesRedirectUrl(request));
        return result;
    }
    @RequestMapping(
            value = {"/attributes"},
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public ModelAndView getAttributeAuthorityView(@RequestParam(value = "SAMLRESPONSE",required = false) String saml, HttpServletRequest request) {
        ModelAndView result = new ModelAndView();
        if(saml != null && !saml.equals("null")) {
            this.logger.debug("Eseguo redirect verso attribute authority: {}", this.attAuthorityUrl);
            this.setRedirectUrl(result, this.attAuthorityUrl);
            result.addObject("COD_DOMINIO", this.domainId);
            result.addObject("COD_APPLICAZIONE", this.applicationId);
            if(this.existOtherCalls()) {
                result.addObject("URL_REQUEST", this.getGenericRedirectUrl(request, String.format("extAttributes/%d", new Object[]{Integer.valueOf(0)})));
            } else if(this.checkForDeleghe) {
                result.addObject("URL_REQUEST", this.getDelegheRedirectUrl(request));
            } else {
                result.addObject("URL_REQUEST", this.getFinalCallbackUrl(request));
            }
        } else {
            this.logger.debug("Identificazione utente fallita. Eseguo redirect verso pagina: {}", this.failedLoginUrl);
            this.setRedirectUrl(result, this.failedLoginUrl);
        }
        return result;
    }
    @RequestMapping(
            value = {"/deleghe"},
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public ModelAndView getServiceProviderDelegheView(@RequestParam(value = "SAMLRESPONSE",required = false) String saml, HttpServletRequest request) {
        ModelAndView result = new ModelAndView();
        if(saml != null && !saml.equals("null")) {
            this.logger.debug("Eseguo redirect verso service provider: {}", this.serviceProviderUrl);
            this.setRedirectUrl(result, this.serviceProviderUrl);
            result.addObject("COD_DOMINIO", this.domainIdForDeleghe != null?this.domainIdForDeleghe:this.domainId);
            result.addObject("COD_APPLICAZIONE", this.applicationIdForDeleghe != null?this.applicationIdForDeleghe:this.applicationId);
            result.addObject("risorsa", this.risorsaForDeleghe);
            result.addObject("URL_REQUEST", this.getFinalCallbackUrl(request));
        } else {
            this.logger.debug("Recupero attributi locali utente fallita. Eseguo redirect verso pagina: {}", this.failedLoginUrl);
            this.setRedirectUrl(result, this.failedLoginUrl);
        }
        return result;
    }
    @RequestMapping(
            value = {"/extAttributes/{call:\\d}"},
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public ModelAndView getOtherAttributesView(@PathVariable Integer call, @RequestParam(value = "SAMLRESPONSE",required = false) String saml, HttpServletRequest request) {
        ModelAndView result = new ModelAndView();
        if(saml != null && !saml.equals("null") && this.existOtherCalls() && call != null && call.intValue() >= 0 && call.intValue() < this.otherCalls.size()) {
            AttributiChiamataLoginFvgDto dtoAttributeAuthority = (AttributiChiamataLoginFvgDto)this.otherCalls.get(call.intValue());
            this.setRedirectUrl(result, dtoAttributeAuthority.getUrlRedirect());
            if(dtoAttributeAuthority.hasAttributes()) {
                result.addAllObjects(dtoAttributeAuthority.getAttributi());
            }
            if(this.otherCalls.size() > call.intValue() + 1) {
                result.addObject("URL_REQUEST", this.getGenericRedirectUrl(request, String.format("extAttributes/%d", new Object[]{Integer.valueOf(call.intValue() + 1)})));
            } else if(this.checkForDeleghe) {
                result.addObject("URL_REQUEST", this.getDelegheRedirectUrl(request));
            } else {
                result.addObject("URL_REQUEST", this.getFinalCallbackUrl(request));
            }
        } else {
            this.logger.debug("Recupero attributi esterni utente fallita. Eseguo redirect verso pagina: {}", this.failedLoginUrl);
            this.setRedirectUrl(result, this.failedLoginUrl);
        }
        return result;
    }
    private void setRedirectUrl(ModelAndView model, String url) {
        model.setViewName(String.format("redirect:%s", new Object[]{url}));
    }
    protected String getGenericRedirectUrl(HttpServletRequest request, String pathName) {
        StringBuilder requestUrl = this.getBaseAppUrl(request);
        return requestUrl.append(String.format("login/%s", new Object[]{pathName})).toString();
    }
    private boolean existOtherCalls() {
        return this.otherCalls != null && !this.otherCalls.isEmpty();
    }
    private StringBuilder getContextUrl(HttpServletRequest request) {
        if(StringUtils.isNotEmpty(this.baseUrl)) {
            return new StringBuilder(this.baseUrl);
        } else {
            StringBuilder result = new StringBuilder();
            result.append(request.getScheme());
            result.append("://");
            String hostName = request.getHeader("Host");
            if(!StringUtils.defaultString(this.hostPortControl).equals("false") && !hostName.startsWith("localhost") && StringUtils.isAlpha(hostName.substring(0, 1)) && hostName.contains(":")) {
                hostName = hostName.substring(0, hostName.indexOf(58));
            }
            this.logger.debug("HostName: {}", hostName);
            String context = request.getContextPath();
            result.append(hostName);
            result.append(context);
            this.logger.debug("Context App URL: {}", result.toString());
            return result;
        }
    }
    private StringBuilder getBaseAppUrl(HttpServletRequest request) {
        StringBuilder result = this.getContextUrl(request);
        StringBuffer requestUrl = request.getRequestURL();
        String context = request.getContextPath();
        requestUrl = requestUrl.delete(0, requestUrl.lastIndexOf(context) + context.length());
        requestUrl = requestUrl.delete(requestUrl.lastIndexOf("login"), requestUrl.length());
        result.append(requestUrl);
        this.logger.debug("Base App URL: {}", result.toString());
        return result;
    }
    private String getAttributesRedirectUrl(HttpServletRequest request) {
        StringBuilder result = this.getBaseAppUrl(request);
        result.append("login/attributes");
        return result.toString();
    }
    private String getDelegheRedirectUrl(HttpServletRequest request) {
        StringBuilder result = this.getBaseAppUrl(request);
        result.append("login/deleghe");
        return result.toString();
    }
    protected String getFinalCallbackUrl(HttpServletRequest request) {
        return this.getContextUrl(request).append(this.finalApplicationCallbackUrl).toString();
    }
    public void setLoginView(String loginView) {
        this.loginView = loginView;
    }
    public void setFinalApplicationCallbackUrl(String finalApplicationCallbackUrl) {
        this.finalApplicationCallbackUrl = finalApplicationCallbackUrl;
    }
    public void setFailedLoginUrl(String failedLoginUrl) {
        this.failedLoginUrl = failedLoginUrl;
    }
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public void setHostPortControl(String hostPortControl) {
        this.hostPortControl = hostPortControl;
    }
    public void setIdpServerUrl(String idpServerUrl) {
        this.idpServerUrl = idpServerUrl;
    }
    public void setAttAuthorityUrl(String attAuthorityUrl) {
        this.attAuthorityUrl = attAuthorityUrl;
    }
    public void setServiceProviderUrl(String serviceProviderUrl) {
        this.serviceProviderUrl = serviceProviderUrl;
    }
    public void setCheckForDeleghe(boolean checkForDeleghe) {
        this.checkForDeleghe = checkForDeleghe;
    }
    public void setRisorsaForDeleghe(String risorsaForDeleghe) {
        this.risorsaForDeleghe = risorsaForDeleghe;
    }
    public void setDomainIdForDeleghe(String domainIdForDeleghe) {
        this.domainIdForDeleghe = domainIdForDeleghe;
    }
    public void setApplicationIdForDeleghe(String applicationIdForDeleghe) {
        this.applicationIdForDeleghe = applicationIdForDeleghe;
    }
    public void setOtherCalls(List<AttributiChiamataLoginFvgDto> otherCalls) {
        this.otherCalls = otherCalls;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}