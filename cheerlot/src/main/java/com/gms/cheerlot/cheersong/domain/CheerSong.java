package com.gms.cheerlot.cheersong.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CheerSong {
    private Long id;
    private String playerCode;
    private String title;
    private String lyrics;
    private String audioFileName;
}
