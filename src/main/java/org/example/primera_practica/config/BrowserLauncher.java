package org.example.primera_practica.config;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class BrowserLauncher {

    private static final Logger logger = LoggerFactory.getLogger(BrowserLauncher.class);

    private final Environment environment;

    public BrowserLauncher(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        boolean shouldOpen = environment.getProperty("app.open-browser-on-start", Boolean.class, true);
        if (!shouldOpen) {
            logger.info("Browser auto-launch disabled via app.open-browser-on-start.");
            return;
        }

        String address = environment.getProperty("server.address");
        String host = (address == null || address.isBlank()) ? "localhost" : address;
        String port = environment.getProperty("server.port", "8080");
        String baseUrl = String.format("http://%s:%s", host, port);

        try {
            if (!Desktop.isDesktopSupported()) {
                logger.info("Desktop integration is not supported; skipping browser launch.");
                return;
            }

            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                logger.info("Desktop browse action is not supported; skipping browser launch.");
                return;
            }

            desktop.browse(URI.create(baseUrl));
            logger.info("Opening browser at {}", baseUrl);
        } catch (Exception ex) {
            logger.warn("Unable to open browser at {} (headless/CI or restricted environment).", baseUrl, ex);
        }
    }
}
