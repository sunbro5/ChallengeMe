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

    @Column(nullable = false)
    private String name;

    /** Emoji or short icon string shown next to the game name. */
    private String icon;

    /** One-liner shown in dropdowns and cards. */
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "how_to_win", columnDefinition = "TEXT")
    private String howToWin;

    /** Newline-delimited rules stored in a single TEXT column. */
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> rules = new ArrayList<>();

    /** Newline-delimited tips stored in a single TEXT column. */
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> tips = new ArrayList<>();

    public Game(String key, String name, String icon, String tagline,
                String description, String howToWin,
                List<String> rules, List<String> tips) {
        this.key         = key;
        this.name        = name;
        this.icon        = icon;
        this.tagline     = tagline;
        this.description = description;
        this.howToWin    = howToWin;
        this.rules       = new ArrayList<>(rules);
        this.tips        = new ArrayList<>(tips);
    }
}
