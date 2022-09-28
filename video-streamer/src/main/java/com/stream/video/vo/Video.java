package com.stream.video.vo;

import lombok.Data;

@Data
public class Video {
    private Integer id;
    private String title;
    private byte[] content;
}
