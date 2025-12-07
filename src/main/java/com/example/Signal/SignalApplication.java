package com.example.Signal;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("my-theme")
@Slf4j
public class SignalApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SignalApplication.class, args);
    }
}
