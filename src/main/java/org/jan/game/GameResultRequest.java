package org.jan.game;

import lombok.Data;

@Data
public class GameResultRequest {
    private String winnerUsername;
    private String resultNote;
}
