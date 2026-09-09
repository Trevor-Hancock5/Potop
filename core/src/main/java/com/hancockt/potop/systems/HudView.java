package com.hancockt.potop.systems;

import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

public record HudView(RunData runData, PersistentData persistentData, PlayerData playerData, float time) {
}
