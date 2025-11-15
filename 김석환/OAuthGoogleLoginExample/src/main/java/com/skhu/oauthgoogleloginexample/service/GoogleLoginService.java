package com.skhu.oauthgoogleloginexample.service;

import com.google.gson.Gson;
import com.skhu.oauthgoogleloginexample.domain.RefreshToken;
import com.skhu.oauthgoogleloginexample.domain.Role;
import com.skhu.oauthgoogleloginexample.domain.User;
import com.skhu.oauthgoogleloginexample.dto.TokenDto;
import com.skhu.oauthgoogleloginexample.dto.UserInfo;
import com.skhu.oauthgoogleloginexample.jwt.TokenProvider;
import com.skhu.oauthgoogleloginexample.repository.RefreshTokenRepository;
import com.skhu.oauthgoogleloginexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleLoginService {

    @Value("${oauth.google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${oauth.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    @Value("${oauth.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URI;

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Authorization Code → Google AccessToken 교환
     */
    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = Map.of(
                "code", code,
                "client_id", GOOGLE_CLIENT_ID,
                "client_secret", GOOGLE_CLIENT_SECRET,
                "redirect_uri", GOOGLE_REDIRECT_URI,
                "grant_type", "authorization_code"
        );

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Gson gson = new Gson();
            return gson.fromJson(responseEntity.getBody(), TokenDto.class)
                    .getAccessToken();
        }

        throw new RuntimeException("구글 액세스 토큰을 가져오는데 실패했습니다.");
    }

    /**
     * AccessToken → Google User 정보 조회
     */
    private UserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        ResponseEntity<String> responseEntity = restTemplate.exchange(request, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Gson gson = new Gson();
            return gson.fromJson(responseEntity.getBody(), UserInfo.class);
        }

        throw new RuntimeException("유저 정보를 가져오는데 실패했습니다.");
    }

    /**
     * 로그인 / 회원가입 처리
     */
    public TokenDto loginOrSignUp(String googleAccessToken) {
        UserInfo userInfo = getUserInfo(googleAccessToken
