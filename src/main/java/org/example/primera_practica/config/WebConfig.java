package org.example.primera_practica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Object locale = session.getAttribute(LOCALE_SESSION_ATTRIBUTE_NAME);
                    if (locale instanceof Locale) {
                        return (Locale) locale;
                    }
                }

                Locale headerLocale = request.getLocale();
                if (headerLocale != null) {
                    return headerLocale;
                }

                Locale defaultLocale = getDefaultLocale();
                return defaultLocale != null ? defaultLocale : Locale.ENGLISH;
            }
        };
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
