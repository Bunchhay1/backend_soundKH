# 📓 SoundKH — Project Notebook

> Cambodian local music platform — iOS app + Spring Boot backend

---

## 🧠 App Concept

**SoundKH** is a music streaming platform focused on **Cambodian local artists**.
- Artists (Creators / Super Stars) post songs to their channels
- Channels can be **public** (anyone can listen) or **private** (request access)
- Private channel access requires **approval by the channel owner** → generates an **8-char access code**
- Super Star tier = verified artists with a badge, featured on home screen

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2.5 / Java 17 |
| Database | PostgreSQL 16 + Flyway migrations |
| Storage | MinIO (S3-compatible) |
| Cache | Redis 7 |
| Auth | JWT (jjwt 0.12.5) |
| Rate Limiting | Bucket4j |
| Email | Spring Mail (optional) |
| Transcoding | TranscodingService (async) |
| Containerization | Docker + Docker Compose |

---

## 👤 User Roles

| Role | Permissions |
|---|---|
| `LISTENER` | Browse, play public tracks, request private channel access |
| `CREATOR` | + Create channels, upload tracks, approve/reject requests |
| `SUPER_STAR` | + Verified badge, featured on home, upload tracks |
| `ADMIN` | Full platform control (ban users, verify channels, change roles) |

---

## 🗄️ Database Schema (Flyway Migrations)

| Version | Description |
|---|---|
| V1 | Init: `users`, `channels`, `tracks`, `access_requests` |
| V2 | Indexes |
| V3 | `play_count`, `genre`, `waveform`, full-text search vector + trigger |
| V4 | `subscriptions` (paid plan), `playlists` |
| V5 | `channel_access_requests` (private channel access) |
| V6 | `channels.visibility` (PUBLIC/PRIVATE), `channel_access_requests.access_code` |
| V7 | `comments`, `likes`, `notifications`, `users.bio`, `users.avatar_url` |

---

## 📁 Project Structure

```
src/main/java/com/soundkh/
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ChannelController.java
│   ├── TrackController.java
│   ├── AccessRequestController.java
│   ├── ChannelAccessRequestController.java
│   ├── PlaylistController.java
│   ├── SubscriptionController.java
│   ├── CommentController.java
│   ├── LikeController.java
│   ├── NotificationController.java
│   └── AdminController.java
├── service/
│   ├── AuthService.java
│   ├── UserService (inline in controller)
│   ├── ChannelService.java
│   ├── TrackService.java
│   ├── AccessRequestService.java
│   ├── ChannelAccessRequestService.java
│   ├── PlaylistService.java
│   ├── SubscriptionService.java
│   ├── CommentService.java
│   ├── LikeService.java
│   ├── NotificationService.java
│   ├── S3StorageService.java
│   ├── TranscodingService.java
│   └── EmailService.java
├── entity/
│   ├── User.java          (roles: LISTENER, CREATOR, SUPER_STAR, ADMIN)
│   ├── Channel.java       (visibility: PUBLIC, PRIVATE; isVerified)
│   ├── Track.java         (visibility: PUBLIC, PRIVATE; playCount; waveform)
│   ├── AccessRequest.java (track-level access)
│   ├── ChannelAccessRequest.java (channel-level; accessCode on approval)
│   ├── Subscription.java  (paid plan: SUPER_STAR)
│   ├── Playlist.java
│   ├── Comment.java
│   ├── Like.java
│   └── Notification.java
├── repository/            (JPA repos for each entity)
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
├── config/
│   ├── SecurityConfig.java
│   ├── CacheConfig.java
│   ├── RateLimitFilter.java
│   └── S3Config.java
├── dto/
│   ├── AuthDto.java
│   ├── TrackDto.java
│   └── ChannelDto.java
└── exception/
    └── GlobalExceptionHandler.java
```

---

## 🌐 All 49 APIs

### Auth (2)
```
POST   /api/auth/register
POST   /api/auth/login
```

### Users (4)
```
GET    /api/users/me                    ← my profile (auth)
GET    /api/users/{username}            ← public profile
PUT    /api/users/me                    ← update bio/avatar
PUT    /api/users/me/password           ← change password
```

### Channels (7)
```
POST   /api/channels                    ← create (CREATOR+)
GET    /api/channels/featured           ← verified channels (public)
GET    /api/channels/search?q=          ← search by name (public)
GET    /api/channels/mine               ← my channels (auth)
GET    /api/channels/{id}               ← get channel (public)
PUT    /api/channels/{id}               ← update (owner)
DELETE /api/channels/{id}               ← delete (owner)
```

### Tracks (9)
```
POST   /api/tracks/channels/{id}        ← upload (SUPER_STAR+)
GET    /api/tracks/channels/{id}        ← list by channel (public)
GET    /api/tracks/search?q=            ← full-text search (public)
GET    /api/tracks/trending             ← top by play_count (public)
GET    /api/tracks/new-releases         ← latest public tracks
GET    /api/tracks/feed                 ← tracks from followed channels (auth)
GET    /api/tracks/{id}/stream          ← byte-range stream (auth)
GET    /api/tracks/{id}/presign         ← presigned URL (auth)
PUT    /api/tracks/{id}                 ← update title/genre/visibility (owner)
DELETE /api/tracks/{id}                 ← delete (owner)
```

### Channel Access Requests (3)
```
POST   /api/channel-access-requests/channels/{id}   ← request access
GET    /api/channel-access-requests/pending          ← list pending (owner)
PATCH  /api/channel-access-requests/{id}/status      ← approve/reject → returns accessCode
```

### Track Access Requests (3)
```
POST   /api/access-requests/tracks/{id}   ← request track access
GET    /api/access-requests/pending        ← list pending (owner)
PATCH  /api/access-requests/{id}/status   ← approve/reject
```

### Comments (4)
```
POST   /api/tracks/{id}/comments          ← post comment (auth)
GET    /api/tracks/{id}/comments          ← list comments (public)
PUT    /api/comments/{id}                 ← edit own comment
DELETE /api/comments/{id}                 ← delete own comment
```

### Likes (3)
```
POST   /api/tracks/{id}/like              ← like (auth)
DELETE /api/tracks/{id}/like              ← unlike (auth)
GET    /api/tracks/{id}/likes             ← like count (public)
```

### Notifications (3)
```
GET    /api/notifications                 ← list (auth)
PATCH  /api/notifications/{id}/read       ← mark read
DELETE /api/notifications                 ← clear all
```

### Playlists (4)
```
POST   /api/playlists
GET    /api/playlists
POST   /api/playlists/{id}/tracks/{trackId}
DELETE /api/playlists/{id}/tracks/{trackId}
```

### Subscriptions (2)
```
POST   /api/subscriptions/{channelId}
DELETE /api/subscriptions/{channelId}
```

### Admin (4)
```
GET    /api/admin/users                   ← list all users
PATCH  /api/admin/users/{id}/role         ← promote role
DELETE /api/admin/users/{id}              ← ban user
PATCH  /api/admin/channels/{id}/verify    ← verify channel (Super Star badge)
```

**Total: 49 APIs**

---

## 🐳 Docker Deployment

### Services
| Container | Image | Port |
|---|---|---|
| soundkh-app | (built from source) | **8081** |
| soundkh-postgres | postgres:16-alpine | 5433 |
| soundkh-redis | redis:7-alpine | 6380 |
| soundkh-minio | minio/minio | 9000 (API), 9001 (Console) |

### Commands
```bash
# Start everything
docker compose up -d

# Rebuild app + start
docker compose up --build -d

# Stop + wipe all data (fresh start)
docker compose down -v

# View app logs
docker logs soundkh-app -f

# Check status
docker compose ps
```

### Environment Variables (in docker-compose)
```
SPRING_DATASOURCE_URL       = jdbc:postgresql://postgres:5432/soundkh
SPRING_DATASOURCE_USERNAME  = soundkh
SPRING_DATASOURCE_PASSWORD  = soundkh_pass
SPRING_DATA_REDIS_HOST      = redis
SPRING_DATA_REDIS_PORT      = 6379
MINIO_ENDPOINT              = http://minio:9000
MINIO_ACCESS_KEY            = minioadmin
MINIO_SECRET_KEY            = minioadmin123
JWT_SECRET                  = (default in application.yml)
```

---

## 🔐 Private Channel Flow

```
1. Listener taps private channel
2. POST /api/channel-access-requests/channels/{id}  → status: PENDING
3. Owner sees it via GET /api/channel-access-requests/pending
4. Owner approves: PATCH /api/channel-access-requests/{id}/status?status=APPROVED
   → Response includes { "accessCode": "AB3K9XZ2" }
5. Email sent to listener (if mail configured)
6. Listener can now stream private tracks in that channel
```

---

## 📱 iOS App Plan (Next Phase)

**Stack:** SwiftUI + AVFoundation + async/await

**Key Screens:**
- Login / Register
- Home (featured Super Star channels + new releases)
- Channel detail (track list, public/private badge)
- Player (AVPlayer with waveform)
- Upload track (CREATOR/SUPER_STAR only)
- Access request flow (request → pending → enter code)
- Admin dashboard (approve requests, verify channels)
- Profile (bio, avatar, role badge)

---

## ⚠️ Known Notes

- **Email** is optional — app starts without mail server configured
- **Byte Buddy / Java 25** — local tests need `-Dnet.bytebuddy.experimental=true` (already in pom.xml)
- **MinIO** is used instead of AWS S3 for local dev (S3-compatible API)
- **Redis port** mapped to 6380 (6379 was taken by titan-project)
- **App port** mapped to 8081 (8080 was taken by titan-project)
- Titan-project containers: stop with `docker stop titan-spring-gateway titan-ai-service titan-core-banking titan-kafka titan-redis titan-db titan-zipkin`
