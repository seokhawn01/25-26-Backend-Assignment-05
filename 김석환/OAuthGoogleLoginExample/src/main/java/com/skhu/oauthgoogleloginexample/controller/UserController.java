package com.skhu.oauthgoogleloginexample.controller;

import com.skhu.oauthgoogleloginexample.domain.User;
import com.skhu.oauthgoogleloginexample.service.GoogleLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("gdg")
public class UserController {

    private final GoogleLoginService googleLoginService;

    @GetMapping("/test")
    public User getUser(Principal principal) {
        return googleLoginService.test(principal);
    }
}
