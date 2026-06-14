package com.soundkh.service;

import com.soundkh.entity.Playlist;
import com.soundkh.repository.PlaylistRepository;
import com.soundkh.repository.TrackRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

    public PlaylistService(PlaylistRepository playlistRepository, UserRepository userRepository,
                           TrackRepository trackRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
    }

    public Map<String, Object> create(String name, String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        var playlist = new Playlist();
        playlist.setUser(user);
        playlist.setName(name);
        var saved = playlistRepository.save(playlist);
        return toMap(saved);
    }

    @Transactional
    public List<Map<String, Object>> listMine(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return playlistRepository.findByUserId(user.getId()).stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> addTrack(Long playlistId, Long trackId, String username) {
        var playlist = getOwned(playlistId, username);
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        if (!playlist.getTracks().contains(track))
            playlist.getTracks().add(track);
        return toMap(playlistRepository.save(playlist));
    }

    @Transactional
    public Map<String, Object> removeTrack(Long playlistId, Long trackId, String username) {
        var playlist = getOwned(playlistId, username);
        playlist.getTracks().removeIf(t -> t.getId().equals(trackId));
        return toMap(playlistRepository.save(playlist));
    }

    public void delete(Long playlistId, String username) {
        playlistRepository.delete(getOwned(playlistId, username));
    }

    private Playlist getOwned(Long playlistId, String username) {
        var playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));
        if (!playlist.getUser().getUsername().equals(username))
            throw new AccessDeniedException("Not your playlist");
        return playlist;
    }

    private Map<String, Object> toMap(Playlist p) {
        return Map.of(
                "id", p.getId(),
                "name", p.getName(),
                "trackCount", p.getTracks().size(),
                "trackIds", p.getTracks().stream().map(t -> t.getId()).toList()
        );
    }
}
