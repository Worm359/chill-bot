package ru.worm.discord.chill.ffmpeg;

import discord4j.voice.AudioProvider;
import discord4j.voice.Opus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.worm.discord.chill.util.ExceptionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//@Component
public class FfmpegAudioProvider extends AudioProvider {
    Logger log = LoggerFactory.getLogger(getClass());
    private final static byte[] tempOpusBytes; //OPUS file, which is read from disk

    public FfmpegAudioProvider() {
        //various tries to pick up the right ByteBuffer size...
        //super();
        //super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        //super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        //super(ByteBuffer.allocate(240));
        super(ByteBuffer.allocate(Opus.FRAME_SIZE));
    }

    private int position = 0; //position of read byte from OPUS file
    @Override
    public boolean provide() {
        ByteBuffer buffer = getBuffer();
        //разница между размером буффера и его заполненностью (пишем до полного буфера)
        log.debug("buffer limit {} position {}", buffer.limit(), buffer.position());
        int providedAudioBytes = buffer.limit() - buffer.position();
        //из считанной Mp3 фозможно осталось меньше байтов
        providedAudioBytes = Math.min(providedAudioBytes, tempOpusBytes.length - position);
        //передавать нечего - либо буфер не почищен, либо все уже отдали
        if (providedAudioBytes <= 0) {
            log.debug("providing {} bytes to buffer, limit {}, position {}",
                    providedAudioBytes, buffer.limit(), buffer.position());
            return false;
        }
        log.debug("providing {} bytes to buffer", providedAudioBytes);
        buffer.put(tempOpusBytes, position, providedAudioBytes);
        position += providedAudioBytes;
        buffer.flip();
        return true;
    }

    static {
        Path opusFile = Paths.get("").toAbsolutePath().getParent().resolve("off_minimal.opus");
        byte[] temp;
        try {
            temp = Files.readAllBytes(opusFile);
        } catch (IOException e) {
            System.out.println("ERROR: couldn't extract file " + opusFile.toAbsolutePath());
            System.out.println(ExceptionUtils.getStackTrace(e));
            temp = new byte[0];
        }
        tempOpusBytes = temp;
    }
}
