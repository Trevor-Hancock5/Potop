package com.hancockt.potop.main;

import com.hancockt.potop.state.PersistentData;
import com.hancockt.potop.state.PlayerData;
import com.hancockt.potop.state.RunData;

public record FrameRuntimeContext(float delta, RunData runData, PersistentData persistentData, PlayerData playerData) {
}
