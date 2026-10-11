
package com.zenith.udl.item;

public interface IColoredTooltip {

    default int getTooltipBackgroundColor() {
        return 0xF0101010;
    }

    default int getTooltipBorderTopColor() {
        return 0xFF5050FF;
    }

    default int getTooltipBorderBottomColor() {
        return 0xFF202080;
    }
}
