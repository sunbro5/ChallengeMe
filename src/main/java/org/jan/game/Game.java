package org.jan.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Matches the GameType enum name (e.g. "TIC_TAC_TOE"). */
    @Column(name = "game_key", unique = true, nullable = false)
    private String key;

    /** Emoji or short icon string shown next to the game name. */
    private String icon;

    private String nameCs;
    private String nameEn;

    private String taglineCs;
    private String taglineEn;

    @Column(columnDefinition = "TEXT") private String descriptionCs;
    @Column(columnDefinition = "TEXT") private String descriptionEn;

    @Column(name = "how_to_win_cs", columnDefinition = "TEXT") private String howToWinCs;
    @Column(name = "how_to_win_en", columnDefinition = "TEXT") private String howToWinEn;

    @Convert(converter = StringListConverter.class)
    @Column(name = "rules_cs", columnDefinition = "TEXT")
    private List<String> rulesCs = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "rules_en", columnDefinition = "TEXT")
    private List<String> rulesEn = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "tips_cs", columnDefinition = "TEXT")
    private List<String> tipsCs = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "tips_en", columnDefinition = "TEXT")
    private List<String> tipsEn = new ArrayList<>();

    public Game(String key, String icon,
                String nameCs, String nameEn,
                String taglineCs, String taglineEn,
                String descriptionCs, String descriptionEn,
                String howToWinCs, String howToWinEn,
                List<String> rulesCs, List<String> rulesEn,
                List<String> tipsCs, List<String> tipsEn) {
        this.key = key;
        this.icon = icon;
        this.nameCs = nameCs;
        this.nameEn = nameEn;
        this.taglineCs = taglineCs;
        this.taglineEn = taglineEn;
        this.descriptionCs = descriptionCs;
        this.descriptionEn = descriptionEn;
        this.howToWinCs = howToWinCs;
        this.howToWinEn = howToWinEn;
        this.rulesCs = new ArrayList<>(rulesCs);
        this.rulesEn = new ArrayList<>(rulesEn);
        this.tipsCs = new ArrayList<>(tipsCs);
        this.tipsEn = new ArrayList<>(tipsEn);
    }
}
