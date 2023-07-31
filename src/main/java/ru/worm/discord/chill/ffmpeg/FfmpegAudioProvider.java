package ru.worm.discord.chill.ffmpeg;

import discord4j.voice.AudioProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.worm.discord.chill.util.ExceptionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FfmpegAudioProvider extends AudioProvider {
    Logger log = LoggerFactory.getLogger(getClass());
    private final static byte[] tempOpusBytes;
    static {
        Path opusFile = Paths.get("").toAbsolutePath().getParent().resolve("output.opus");
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

    public FfmpegAudioProvider() {
        super();
    }

    private int position = 0;

    @Override
    public boolean provide() {
        ByteBuffer buffer = getBuffer();
        //разница между размером буффера и его заполненностью (пишем до полного буфера)
        int providedAudioBytes = buffer.limit() - buffer.position();
        //из считанной Mp3 фозможно осталось меньше байтов
        providedAudioBytes = Math.min(providedAudioBytes, tempOpusBytes.length - position);
        //передавать нечего - либо буфер не почищен, либо все уже отдали
        if (providedAudioBytes <= 0) {
            log.debug("remainedSpaceInBuffer is {}, limit {}, position {}",
                    providedAudioBytes, buffer.limit(), buffer.position());
            return false;
        }
        log.debug("remaindedSpaceInBuffer is {}", providedAudioBytes);
        buffer.put(tempOpusBytes, position, providedAudioBytes);
        position += providedAudioBytes;
        return true;
    }
}
