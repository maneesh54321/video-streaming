package com.stream.video.controller;

import com.stream.video.service.VideoStreamingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class VideoStreamController {

    @Autowired
    private VideoStreamingService videoStreamingService;

    @PostMapping("/video")
    public Integer uploadVideo(){
        return null;
    }

    @GetMapping("/video/all")
    public Integer getAllVideos(){
        return null;
    }

    @GetMapping(value = "/video/stream/{videoId}/{startingSecond}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] streamVideo(@PathVariable("videoId") Integer id, @PathVariable("startingSecond") Integer startingSecond){
        log.info("videoId: {}", id);
        return videoStreamingService.streamVideo(id, startingSecond);
    }
}
