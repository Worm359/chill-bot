package ru.worm.discord.chill.ffmpeg;

import org.bytedeco.ffmpeg.ffmpeg;
import org.bytedeco.javacpp.Loader;

import java.io.IOException;

/**
 * Тестовый метод для преобразования mp3 файлов в opus формат, требуемый discord4j.voice.AudioProvider
 */
public class FFmpegConvertForTesting {
    public static void main(String[] args) throws IOException, InterruptedException {
        String ffmpeg = Loader.load(ffmpeg.class);
        //пример использования из документации
        //ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-i", "/path/to/input.mp4", "-vcodec", "h264", "/path/to/output.mp4");
        //пример для конвертации в формат opus
        //-i input.mp3 -c:a libopus output.opus
        ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-i", "pupushka.mp3", "-c:a", "libopus", "output.opus");
        pb.inheritIO().start().waitFor();
    }
}
