package kr.easw.lesson01.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DownloadLinkDto {
    @Getter
    private final String link;
}