package com.gms.cheerlot.cheersong.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheerSong {
    private Long id;
    private String playerCode;
    private String title;
    private String lyrics;
    private String audioFileName;
}
