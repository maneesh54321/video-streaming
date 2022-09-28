package com.stream.video.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoMetadata {
    private Integer id;
    private String title;
    private String filePath;
    private String resolution;
}
