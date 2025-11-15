package com.skhu.oauthgoogleloginexample.controller;

import com.skhu.oauthgoogleloginexample.domain.RefreshToken;
import com.skhu.oauthgoogleloginexample.domain.User;
import com.skhu.oauthgoogleloginexample.dto.TokenDto;
import com.skhu.oauthgoogleloginexample.jwt.TokenProvider;
import com.skhu.oauthgoogleloginexample.repository.RefreshTokenRepository;
import com.skhu.oauthgoogleloginexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class RefreshController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @PostMapping("/refresh")
    public TokenDto refresh(@RequestParam String refreshToken) {

        RefreshToken stored = refreshTokenRepository.findById(
                Long.parseLong(
                        tokenProvider.getAuthentication(refreshToken).getName()
                )
        ).orElseThrow(() -> new RuntimeException("리프레시 토큰이 존재하지 않습니다."));

        if (!stored.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 일치하지 않습니다.");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        String newAccess = tokenProvider.createAccessToken(user);

        return TokenDto.builder()
                .accessToken(newAccess)
                .refreshToken(stored.getRefreshToken())
                .build();
    }
}
