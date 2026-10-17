# /discord/bot/canute
![Knut_1000](https://github.com/user-attachments/assets/79d457b5-9be8-403c-888a-ca1eb8c0c062)

Discord bot written in 100% Kotlin
Requires JDK 21. Use the included Gradle wrapper (8.14.3).

`./gradlew clean build`
`./gradlew run`

## Spotify playlist playback

Join a normal voice channel, then run:

```text
/play playlist:https://open.spotify.com/playlist/PLAYLIST_ID
/skip
/stop
```

`/play` appends the loaded playlist in order to this server's queue and starts
playback if idle. `/skip` advances one song; `/stop` clears the queue and leaves
voice. Users must be in the bot's voice channel to control it. Each server has
an independent queue, limited to 1000 tracks. Queues are held in memory and lost
on restart. The bot stays connected when a queue finishes; use `/stop` to leave.

Full Spotify playlist links (including `?si=...`, locale paths and Discord angle
brackets) and `spotify:playlist:ID` URIs are supported. Short `spotify.link`
links, individual tracks, albums, and arbitrary URLs are rejected.

### Setup

1. Invite the Discord bot with `bot` and `applications.commands` scopes. Give
   it View Channel, Connect and Speak permissions in the voice channel.
2. Download [Lavalink 4.2.2](https://github.com/lavalink-devs/Lavalink/releases/tag/4.2.2)
   and place `Lavalink.jar` in `lavalink/`. Use JDK 21 for both processes.
   The checked-in `lavalink/application.yml` installs youtube-source 1.18.2. Lavalink handles Discord's DAVE voice encryption.
3. Create an application in the [Spotify developer dashboard](https://developer.spotify.com/dashboard).
   Add `http://127.0.0.1:8888/callback` as an allowed redirect URI. Ensure the
   account is allowed to use the application (development-mode apps require
   the app owner to have Premium). On your own computer, authorize once:

   ```sh
   export SPOTIFY_CLIENT_ID='your-client-id'
   export SPOTIFY_CLIENT_SECRET='your-client-secret'
   python3 scripts/spotify_authorize.py
   ```

   The helper opens Spotify and prints a refresh token after consent. Keep it
   secret. It requests playlist-read-private and playlist-read-collaborative,
   binds only to loopback, checks OAuth state, and times out after five minutes.
   Set these variables in the **Lavalink process** environment:

   ```sh
   export LAVALINK_PASSWORD='your-own-long-password'
   cd lavalink
   java -jar Lavalink.jar
   ```

4. In a second terminal, from the repository root, set the **bot process** environment:

   ```sh
   export DISCORD_TOKEN='your-discord-bot-token'
   export SPOTIFY_CLIENT_ID='your-client-id'
   export SPOTIFY_CLIENT_SECRET='your-client-secret'
   export SPOTIFY_REFRESH_TOKEN='the-token-from-the-helper'
   export LAVALINK_PASSWORD='the-same-password-as-above'
   export LAVALINK_URL='ws://localhost:2333' # optional; this is the default
   bash gradlew run
   ```

   No `.env` loader is included: export the variables or configure your service
   manager. Never commit credentials. If you previously created an ignored
   `Token.kt` defining `botToken`, remove it before building; `Main.kt` now reads
   `DISCORD_TOKEN`. Without `LAVALINK_PASSWORD`, existing utility commands still
   work and music commands explain the missing configuration.

The node listens on loopback by default. If running it on another host, configure
its bind address and a protected `wss://` endpoint and update `LAVALINK_URL`.
Commands are upserted on startup without deleting existing commands; global
slash-command updates can take time to appear in Discord.

### What Spotify support means

The bot reads playlist metadata through Spotify's official `/items` API and
lazily searches YouTube by song title and artist as each song reaches the front
of the queue. It does **not** stream or download audio from Spotify. Matches can
differ from the Spotify recording. Missing matches are skipped. The queued
count does not guarantee that every track will play. Local files, episodes,
removed items and explicitly unplayable tracks are excluded.

All commands use the **one Spotify account authorized by the bot owner**; Discord
users do not connect their own Spotify accounts. Only deploy this configuration
in servers where that account's playlist access may be shared.
[Spotify's 2026 development-mode restrictions](https://developer.spotify.com/documentation/web-api/tutorials/february-2026-migration-guide)
limit playlist contents to playlists that account owns or collaborates on.
Arbitrary public playlists and Spotify-generated playlists may be inaccessible.
The command explains denied access, missing playlists and rate limits.

Playlist pages are fetched in order, with a hard limit of 1000 entries (larger
playlists are rejected rather than silently truncated). Access tokens refresh
automatically. If Spotify revokes authorization, or rotates a refresh token that
is then lost on restart, run the authorization helper again. Refresh tokens are
kept in memory and are never logged by the bot.

Audio needs an available YouTube match and working source configuration on the
hosting network; see the [youtube-source setup guide](https://github.com/lavalink-devs/youtube-source).
A batch of failed searches is bounded to 30 seconds; a search/node timeout clears
the affected queue, so use `/play` again after fixing the node.

### Verification

Run `bash gradlew test build`. Tests cover accepted/rejected playlist inputs,
queue order, duplicates, capacity, clearing and isolation between servers.
For a live check, queue an accessible playlist of at least three songs, append
another while it plays, then try `/skip` and `/stop`. Confirm automatic advance,
that another server has a separate queue, and that someone in a different voice
channel cannot interrupt playback. Live checks need your own Discord/Spotify
credentials and a running Lavalink node.
