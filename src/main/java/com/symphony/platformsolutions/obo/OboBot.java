package com.symphony.platformsolutions.obo;

import authentication.ISymAuth;
import authentication.SymBotRSAAuth;
import authentication.SymOBORSAAuth;
import clients.SymBotClient;
import com.symphony.platformsolutions.obo.bot.IMListenerImpl;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import listeners.IMListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import services.DatafeedEventsService;

import java.net.URL;

@SpringBootApplication
public class OboBot {
    private static Logger LOGGER = LoggerFactory.getLogger(OboBot.class);
    private static SymConfig config;
    private static SymOBORSAAuth oboAuth;
    private static SymBotClient botClient;


    public static void main(String [] args) {
        SpringApplication.run(OboBot.class, args);
    }

    public OboBot() throws RuntimeException {

        URL url = getClass().getClassLoader().getResource("config.json");
        if (url == null)
            throw new RuntimeException("Bad URL");
        config = SymConfigLoader.loadFromFile(url.getPath());
        ISymAuth botAuth = new SymBotRSAAuth(config);
        try {
            botAuth.authenticate();
        }
        catch (Exception e) {
            LOGGER.debug(e.toString());
        }

        SymBotClient botClient = SymBotClient.initBot(config, botAuth);
        DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
        IMListener imListener = new IMListenerImpl(botClient);
        datafeedEventsService.addIMListener(imListener);

        oboAuth = new SymOBORSAAuth(config);
        oboAuth.authenticate();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/bundle.json").allowedOrigins("*");
            }
        };
    }


    public static SymOBORSAAuth getOboAuth() {
        return oboAuth;
    }

    public static SymConfig getConfig() {
        return config;
    }

    public static SymBotClient getBotClient(){
        return botClient;
    }
}