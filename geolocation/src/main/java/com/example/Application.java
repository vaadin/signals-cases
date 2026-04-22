package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
<<<<<<<< HEAD:signals/src/main/java/com/example/Application.java
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.lumo.Lumo;
========
import com.vaadin.flow.theme.aura.Aura;
>>>>>>>> a0c0ab4 (Geolocation use cases):geolocation/src/main/java/com/example/Application.java

@SpringBootApplication
@EnableAsync
@StyleSheet(Lumo.STYLESHEET) // Use Aura.STYLESHEET to use Aura instead
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css") // Your custom styles
@Push
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
