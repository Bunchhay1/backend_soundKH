package com.soundkh.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
            86400000L
        );
    }

    @Test
    void generateAndExtractUsername() {
        String token = jwtUtil.generate("chhay", "CREATOR");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("chhay");
    }

    @Test
    void validToken() {
        String token = jwtUtil.generate("chhay", "CREATOR");
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void invalidToken() {
        assertThat(jwtUtil.isValid("not.a.token")).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtUtil shortLived = new JwtUtil(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
            -1L
        );
        String token = shortLived.generate("chhay", "LISTENER");
        assertThat(shortLived.isValid(token)).isFalse();
    }
}
