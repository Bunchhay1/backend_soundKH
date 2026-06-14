package com.soundkh.controller;

import com.soundkh.service.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChannelControllerTest extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @MockBean S3StorageService s3StorageService;

    private String creatorToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register a CREATOR role user (we'll promote via direct DB or use LISTENER for now)
        var result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"channelcreator","email":"channelcreator@test.com","password":"pass123"}
                    """))
                .andReturn();
        // Extract token from response
        String body = result.getResponse().getContentAsString();
        creatorToken = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void createChannel_asListener_returns403() throws Exception {
        // Default role is LISTENER, which cannot create channels
        mockMvc.perform(post("/api/channels")
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"My Channel","description":"desc"}
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    void getChannel_notFound_returns400() throws Exception {
        mockMvc.perform(get("/api/channels/99999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listMine_authenticated_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/channels/mine")
                .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listMine_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/channels/mine"))
                .andExpect(status().isUnauthorized());
    }
}
