package com.soundkh.controller;

import com.soundkh.service.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrackControllerTest extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @MockBean S3StorageService s3StorageService;

    private String listenerToken;

    @BeforeEach
    void setUp() throws Exception {
        var result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"tracklistener","email":"tracklistener@test.com","password":"pass123"}
                    """))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        listenerToken = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void listTracks_byChannel_returnsArray() throws Exception {
        mockMvc.perform(get("/api/tracks/channels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void uploadTrack_asListener_returns403() throws Exception {
        var file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/tracks/channels/1")
                .file(file)
                .param("title", "Test Song")
                .param("visibility", "PUBLIC")
                .header("Authorization", "Bearer " + listenerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void streamTrack_notFound_returns400() throws Exception {
        mockMvc.perform(get("/api/tracks/99999/stream")
                .header("Authorization", "Bearer " + listenerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrack_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/tracks/1"))
                .andExpect(status().isUnauthorized());
    }
}
