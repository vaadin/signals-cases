package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.aura.Aura;

@SpringBootApplication
@EnableScheduling
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
@Push
// Required so Vaadin generates a service worker for Web Push (UC3).
@PWA(name = "Page Visibility API Use Cases", shortName = "PageVis")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
