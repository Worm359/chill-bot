# Chill-Bot

A lightweight Discord music bot written in **Java17** and **Kotlin**. It uses the Java Discord API (JDA) for gateway interaction, **Lavaplayer** for audio streaming, and **yt-dlp** for on-demand media retrieval. Integration with the **YouTube Data API** enables playlist support and track metadata lookup. Command parsing follows the Unix‐style convention provided by **Apache-Commons-CLI**.

---

## Key Features

* Works with any Discord server where the bot has voice permissions.
* Supports YouTube playlist import and per-track duration limits.
* Queue navigation commands: `next`/`prev`/`playNext`/`playNow`/`skipTo`/`add`.
* Download-then-play workflow powered by **yt-dlp** to minimise buffering issues.
* Command line style syntax familiar to users of Unix utilities (implemented with **Apache-Commons-CLI**).
* Designed for self-hosting; no external stateful services are required.

## Requirements

| Tool                 | Version       | Purpose                        |
| -------------------- | ------------- | ------------------------------ |
| JDK                  | 17 or later   | Runtime                        |
| Kotlin               | 1.9 or later  | Source code                    |
| maven                | 3.9.4 or later| Runtime                        |
| FFmpeg               | latest        | Audio transcoding              |
| YouTube Data API key | -         	   | Playlist and metadata requests |

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-user/chill-bot.git
   cd chill-bot
   ```
2. **Build**

   ```bash
   mvn clean install
   ```
3. Setup settings:
	* Copy `chill-bot/src/main/resources/application.example.properties` to `chill-bot/devrun` folder
	* Edit the settings according to your needs. The most important are:
		- `chill-bot.discord.token`
		- `chill-bot.youtube.api-key`
		- `chill-bot.youtube.maximum-video-length-minutes`
		- `chill-bot.youtube.ytp-dlp-bin`
4. **Run** (slash at the end for --spring.config.location folder is mandatory)

   ```bash
   java -jar chill-bot/devrun/chill-bot.jar --spring.config.location=chill-bot/devrun/
   ```

You can move the application files from `devrun` to wherever you like.  
Default command prefix is `!`, you can change this with `chill-bot.discord.prefix` setting.

## Basic Commands

`!help` output:

```
======= ADDING TRACKS =======

!add         - add youtube video to the end of track
               queue

!playlist    - add youtube playlist to the end of track
               queue

!play_next   - play specified track next (by track ID
               or youtube URL). Use ID
               for referencing existing
               track in the
               queue/history.

!play_now    - skip current, play specified track now
               (by track ID or youtube
               URL). Use ID for
               referencing existing
               track in the
               queue/history.

======= QUEUE CONTROLS =======

!next        - play next track from queue

!prev        - play the previous track

!rmv         - deletes the track from queue

!skip_to     - skip the queue to specific ID

!now         - print currently playing song

!list        - print all tracks from history & track
               queue

======= BOT CONTROL =======

!join        - invite bot to the voice channel you
               currently in

!player      - play/pause

!clean       - remove recent bot related messages from
               text channel

!status      - show bot status/version

!ping        - ping the bot

!lock        - lock bot to current discord server (for dev purposes)

!dev         - to notify the bots that 'dev'
               environment is used. add
               ' disable' to return to
               normal

=====================
for more info type '!command -h'
```
