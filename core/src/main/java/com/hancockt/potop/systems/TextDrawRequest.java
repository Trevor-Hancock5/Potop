package com.hancockt.potop.systems;

import java.util.List;

public record TextDrawRequest(List<String> text, List<Float> xPos, List<Float> yPos, List<Float> scale) {
}
