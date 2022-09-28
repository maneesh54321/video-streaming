package com.stream.video.service;

import com.stream.video.model.VideoMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.MultimediaInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class VideoStreamingService {

    private static final float CHUNK_DURATION = 2;

    private final Map<Integer, List<VideoMetadata>> videoIdMetadataMap;

    private static final AtomicInteger VIDEO_ID_GENERATOR = new AtomicInteger();

    public VideoStreamingService() {
        this.videoIdMetadataMap = new HashMap<>();
    }

    public void uploadVideo(String location) throws IOException {

        try (Stream<Path> paths = Files.walk(Paths.get(location))) {

            paths.toList().parallelStream().filter(path -> !Files.isDirectory(path)).forEach(path -> {
                var video_id = VIDEO_ID_GENERATOR.incrementAndGet();
                String fileName = path.getFileName().toString();
                var title = fileName.substring(0, fileName.lastIndexOf("."));
                var resolution = "720p";

                var video = new MultimediaObject(path.toFile());
                MultimediaInfo info;
                try {
                    info = video.getInfo();

                    log.info("Video info: {}", info);

                    float totalDuration = video.getInfo().getDuration() / 1000;

                    Encoder encoder = new Encoder();
                    EncodingAttributes encodingAttributes = new EncodingAttributes();

                    VideoAttributes videoAttributes = new VideoAttributes();
                    videoAttributes.setCodec("h264");
                    videoAttributes.setX264Profile(X264_PROFILE.BASELINE);
                    videoAttributes.setBitRate(video.getInfo().getVideo().getBitRate());
                    videoAttributes.setFrameRate((int) video.getInfo().getVideo().getFrameRate());
                    videoAttributes.setSize(video.getInfo().getVideo().getSize());

                    AudioAttributes audioAttributes = new AudioAttributes();
                    audioAttributes.setCodec("aac");
                    audioAttributes.setBitRate(video.getInfo().getAudio().getBitRate());
                    audioAttributes.setChannels(video.getInfo().getAudio().getChannels());
                    audioAttributes.setSamplingRate(video.getInfo().getAudio().getSamplingRate());

                    encodingAttributes.setVideoAttributes(videoAttributes);
                    encodingAttributes.setAudioAttributes(audioAttributes);

                    float offset = 0;

                    int counter = 1;

                    while (offset < totalDuration) {
                        encodingAttributes.setOffset(offset);
                        encodingAttributes.setDuration(CHUNK_DURATION);
                        String targetFilePath = String.format(location + "\\%s\\%02d.mp4", title, counter);
                        File targetFile = new File(targetFilePath);
                        encoder.encode(video, targetFile, encodingAttributes);
                        VideoMetadata videoChunkMetadata = new VideoMetadata(video_id, title, targetFilePath, resolution);
                        this.videoIdMetadataMap.putIfAbsent(video_id, new ArrayList<>());
                        this.videoIdMetadataMap.get(video_id).add(videoChunkMetadata);
                        offset += CHUNK_DURATION;
                        counter++;
                    }
                } catch (EncoderException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

    public byte[] streamVideo(Integer id, int time) {
        List<VideoMetadata> videoChunks = videoIdMetadataMap.get(id);
        int chunkIndex = (int) (time / CHUNK_DURATION);
        if (chunkIndex < videoChunks.size()) {
            VideoMetadata videoMetadata = videoChunks.get(chunkIndex);
            log.info("VideoMetadata: {}", videoMetadata);
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(videoMetadata.getFilePath()));
                return bis.readAllBytes();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
}
