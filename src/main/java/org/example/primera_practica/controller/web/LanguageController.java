package org.example.primera_practica.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
public class LanguageController {

    private static final String FALLBACK_REDIRECT = "/mocks";

    private final LocaleResolver localeResolver;

    public LanguageController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @PostMapping("/i18n/change")
    public String changeLanguage(@RequestParam("lang") String language,
                                 @RequestParam(value = "redirect", required = false) String redirect,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        localeResolver.setLocale(request, response, Locale.forLanguageTag(language));
        return "redirect:" + sanitizeRedirect(redirect);
    }

    private String sanitizeRedirect(String redirect) {
        if (!StringUtils.hasText(redirect)) {
            return FALLBACK_REDIRECT;
        }

        String candidate = redirect.trim();
        if (!candidate.startsWith("/")) {
            return FALLBACK_REDIRECT;
        }

        if (candidate.startsWith("//") || candidate.contains("://")) {
            return FALLBACK_REDIRECT;
        }

        return candidate;
    }
}
