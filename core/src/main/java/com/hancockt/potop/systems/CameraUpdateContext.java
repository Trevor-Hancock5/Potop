package com.hancockt.potop.systems;

import com.hancockt.potop.state.GameState;
import com.hancockt.potop.state.PlayerData;

public record CameraUpdateContext(float delta, PlayerData playerData, GameState gameState, float waterLevel,
                                  float maxFallSpeed) {
}
