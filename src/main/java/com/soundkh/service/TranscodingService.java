package com.soundkh.service;

import com.soundkh.entity.Track;
import com.soundkh.repository.TrackRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class TranscodingService {

    private final S3StorageService s3;
    private final TrackRepository trackRepository;

    public TranscodingService(S3StorageService s3, TrackRepository trackRepository) {
        this.s3 = s3;
        this.trackRepository = trackRepository;
    }

    /**
     * Phase 16: Transcode original audio to 128kbps MP3 via FFmpeg.
     * Runs async so upload endpoint returns immediately.
     */
    @Async
    public void transcodeAsync(Long trackId, String rawKey) {
        File tmpIn = null;
        File tmpOut = null;
        try {
            tmpIn = File.createTempFile("soundkh-in-", ".audio");
            tmpOut = File.createTempFile("soundkh-out-", ".mp3");

            // Download raw file from MinIO
            try (var in = s3.downloadAudio(rawKey)) {
                Files.copy(in, tmpIn.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // FFmpeg: transcode to 128kbps MP3
            var proc = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", tmpIn.getAbsolutePath(),
                    "-vn", "-ar", "44100", "-ac", "2", "-b:a", "128k",
                    tmpOut.getAbsolutePath()
            ).redirectErrorStream(true).start();
            int exit = proc.waitFor();

            if (exit == 0) {
                String transcodedKey = rawKey.replace("audio/", "audio/transcoded/") + ".mp3";
                try (var out = Files.newInputStream(tmpOut.toPath())) {
                    s3.uploadAudio(transcodedKey, out, tmpOut.length());
                }
                // Update track with transcoded key and duration
                final File finalTmpOut = tmpOut;
                trackRepository.findById(trackId).ifPresent(track -> {
                    track.setS3ObjectKey(transcodedKey);
                    track.setDuration(probeDuration(finalTmpOut));
                    trackRepository.save(track);
                });
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (tmpIn != null) tmpIn.delete();
            if (tmpOut != null) tmpOut.delete();
        }
    }

    /**
     * Phase 17: Generate waveform data (100 amplitude samples) via FFmpeg.
     * Returns JSON array string stored in tracks.waveform column.
     */
    @Async
    public void generateWaveformAsync(Long trackId, String audioKey) {
        File tmpIn = null;
        File tmpWav = null;
        try {
            tmpIn = File.createTempFile("soundkh-wf-", ".audio");
            tmpWav = File.createTempFile("soundkh-wf-", ".wav");

            try (var in = s3.downloadAudio(audioKey)) {
                Files.copy(in, tmpIn.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Convert to mono 8kHz WAV for analysis
            new ProcessBuilder("ffmpeg", "-y", "-i", tmpIn.getAbsolutePath(),
                    "-ac", "1", "-ar", "8000", tmpWav.getAbsolutePath())
                    .redirectErrorStream(true).start().waitFor();

            // Read raw PCM bytes and downsample to 100 points
            byte[] pcm = Files.readAllBytes(tmpWav.toPath());
            int samples = 100;
            int step = Math.max(1, pcm.length / samples);
            List<Integer> waveform = new ArrayList<>(samples);
            for (int i = 0; i < samples; i++) {
                int idx = i * step;
                waveform.add(idx < pcm.length ? Math.abs(pcm[idx] & 0xFF) : 0);
            }

            String json = waveform.toString(); // simple JSON array
            trackRepository.findById(trackId).ifPresent(track -> {
                track.setWaveform(json);
                trackRepository.save(track);
            });
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (tmpIn != null) tmpIn.delete();
            if (tmpWav != null) tmpWav.delete();
        }
    }

    private int probeDuration(File file) {
        try {
            var proc = new ProcessBuilder(
                    "ffprobe", "-v", "error", "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1", file.getAbsolutePath()
            ).redirectErrorStream(true).start();
            String out = new String(proc.getInputStream().readAllBytes()).trim();
            return (int) Double.parseDouble(out);
        } catch (Exception e) {
            return 0;
        }
    }
}
