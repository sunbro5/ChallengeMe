package org.jan.game;

import org.jan.game.dto.GameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired private GameRepository gameRepository;

    @GetMapping
    public ResponseEntity<List<GameDto>> getAll() {
        return ResponseEntity.ok(
                gameRepository.findAll().stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<GameDto> getByKey(@PathVariable String key) {
        return gameRepository.findByKey(key)
                .map(g -> ResponseEntity.ok(toDto(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    private GameDto toDto(Game g) {
        return new GameDto(g.getId(), g.getKey(), g.getName(), g.getIcon(),
                g.getTagline(), g.getDescription(), g.getHowToWin(),
                g.getRules(), g.getTips());
    }
}
