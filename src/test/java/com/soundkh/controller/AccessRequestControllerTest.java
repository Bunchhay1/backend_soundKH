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

class AccessRequestControllerTest extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @MockBean S3StorageService s3StorageService;

    private String listenerToken;

    @BeforeEach
    void setUp() throws Exception {
        var result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"arlistener","email":"arlistener@test.com","password":"pass123"}
                    """))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        listenerToken = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void requestAccess_trackNotFound_returns400() throws Exception {
        mockMvc.perform(post("/api/access-requests/tracks/99999")
                .header("Authorization", "Bearer " + listenerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestAccess_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/access-requests/tracks/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listPending_authenticated_returnsArray() throws Exception {
        mockMvc.perform(get("/api/access-requests/pending")
                .header("Authorization", "Bearer " + listenerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateStatus_notFound_returns400() throws Exception {
        mockMvc.perform(patch("/api/access-requests/99999/status")
                .param("status", "APPROVED")
                .header("Authorization", "Bearer " + listenerToken))
                .andExpect(status().isBadRequest());
    }
}
