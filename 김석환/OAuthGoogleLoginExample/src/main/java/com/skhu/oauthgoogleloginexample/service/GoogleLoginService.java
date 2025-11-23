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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.Principal;

@Service
@RequiredArgsConstructor
public class GoogleLoginService {

    @Value("${jwt.oauth.google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${jwt.oauth.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    @Value("${jwt.oauth.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URI;

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    /**
     * 1) Authorization Code → Google AccessToken 교환
     */
    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // Google OAuth는 반드시 x-www-form-urlencoded 로 요청해야 함
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", GOOGLE_CLIENT_ID);
        params.add("client_secret", GOOGLE_CLIENT_SECRET);
        params.add("redirect_uri", GOOGLE_REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("구글 액세스 토큰 요청 실패: " + response.getBody());
        }

        TokenDto tokenDto = new Gson().fromJson(response.getBody(), TokenDto.class);
        return tokenDto.getAccessToken();
    }


    /**
     * 2) AccessToken → 구글 사용자 정보 요청
     */
    private UserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("구글 사용자 정보 요청 실패: " + response.getBody());
        }

        return new Gson().fromJson(response.getBody(), UserInfo.class);
    }

    /**
     * 로그인 / 회원가입 처리
     */
    public TokenDto loginOrSignUp(String googleAccessToken) {
        // 1. 구글에서 유저 정보 가져오기
        UserInfo userInfo = getUserInfo(googleAccessToken);

        // 2. 우리 서비스에 이미 가입된 유저인지 확인, 아니면 회원가입
        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .profileUrl(userInfo.getPictureUrl())
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });

        // 3. 우리 서비스용 AccessToken, RefreshToken 생성
        String accessToken = tokenProvider.createAccessToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        // 4. RefreshToken DB에 저장 (userId를 PK로 사용하는 엔티티)
        RefreshToken refreshTokenEntity = new RefreshToken(user.getId(), refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        // 5. 클라이언트에 내려줄 TokenDto 구성
        TokenDto tokenDto = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return tokenDto;
    }
    public User getUserByPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("로그인이 필요한 요청입니다.");
        }

        String email = principal.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
    }

}
