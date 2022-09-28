package com.stream.video;

import com.stream.video.service.VideoStreamingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryDataInitializer implements CommandLineRunner {

    @Autowired
    private VideoStreamingService videoStreamingService;

    @Override
    public void run(String... args) throws Exception {
        String fileLocation = "C:\\Users\\manee\\Downloads\\work\\test-data\\video-streamer";

        log.info("Uploading all videos from location {}", fileLocation);

        videoStreamingService.uploadVideo(fileLocation);
    }
}
