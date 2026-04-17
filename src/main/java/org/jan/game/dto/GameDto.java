package org.jan.game.dto;

import lombok.Value;

import java.util.List;

@Value
public class GameDto {
    Long id;
    String key;
    String icon;
    String nameCs;     String nameEn;
    String taglineCs;  String taglineEn;
    String descriptionCs; String descriptionEn;
    String howToWinCs;    String howToWinEn;
    List<String> rulesCs; List<String> rulesEn;
    List<String> tipsCs;  List<String> tipsEn;
}
