package com.hancockt.potop.systems;

import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

public record RenderWorldView(RunData runData, PlayerData playerData, float waterLevel, float faucetInteractionTime) {
}
