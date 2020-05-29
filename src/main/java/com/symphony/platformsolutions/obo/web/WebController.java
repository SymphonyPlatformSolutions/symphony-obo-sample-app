package com.symphony.platformsolutions.obo.web;

import authentication.SymExtensionAppRSAAuth;
import com.symphony.platformsolutions.obo.OboBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WebController {
    private static final Logger LOG = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/")
    public String home(){
        return "Hello World";
    }

    @GetMapping("/appToken")
    public Map<String, String> getAppToken(){
        SymExtensionAppRSAAuth appAuth = new SymExtensionAppRSAAuth(OboBot.getConfig());
        Map<String, String> map = new HashMap<>();
        map.put("token", appAuth.appAuthenticate().getAppToken());
        return map;
    }

}
