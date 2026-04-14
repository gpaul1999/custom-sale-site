package com.example.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Configuration
public class AppConfig {
    @Value("${app.brand-name}")
    private String brandName;
    @Value("${app.phone}")
    private String phone;
    @Value("${app.email}")
    private String email;
    @Value("${app.business-phone}")
    private String businessPhone;
    @Value("${app.business-email}")
    private String businessEmail;

    public String getBrandName() { return brandName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getBusinessPhone() { return businessPhone; }
    public String getBusinessEmail() { return businessEmail; }
}

@ControllerAdvice
class GlobalAppConfigAdvice {
    private final AppConfig appConfig;
    public GlobalAppConfigAdvice(AppConfig appConfig) { this.appConfig = appConfig; }
    @ModelAttribute
    public void addAppConfig(Model model) {
        model.addAttribute("brandName", appConfig.getBrandName());
        model.addAttribute("appPhone", appConfig.getPhone());
        model.addAttribute("appEmail", appConfig.getEmail());
        model.addAttribute("appBusinessPhone", appConfig.getBusinessPhone());
        model.addAttribute("appBusinessEmail", appConfig.getBusinessEmail());
    }
}
