package com.zenith.udl.client.tooltip;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * このインターフェースを {@link net.minecraft.world.item.Item} の実装クラスに付けると、
 * そのアイテムのツールチップが UDL 独自の「発光グラデーションツールチップ」で描画されます。
 * <p>
 * 各メソッドはデフォルト実装を持っているので、必要な部分だけオーバーライドすれば
 * 見た目や挙動を自由に調整できます。
 */
public interface IUDLGlowTooltip {

    /** ツールチップの縁・グロー・区切り線・テキストの発光に使う基本色（0xRRGGBB）。 */
    default int udl$getGlowColor() {
        return 0xB030FF; // 紫系ネオン
    }

    /** 背景グラデーションの上端色（ARGB, 0xAARRGGBB）。 */
    default int udl$getGradientTop() {
        return 0xF0080008; // ほぼ黒
    }

    /** 背景グラデーションの下端色（ARGB, 0xAARRGGBB）。 */
    default int udl$getGradientBottom() {
        return 0xF03A0A55; // 深い紫
    }

    /** 中央に3Dアイテムモデルを大きく表示するか。 */
    default boolean udl$showItemModel() {
        return true;
    }

    /** 3Dアイテムモデルの自転速度（度/秒）。 */
    default float udl$rotationSpeed() {
        return 40f;
    }

    /** 3Dアイテムモデルの傾き（X軸, 度）。0で真正面から。 */
    default float udl$tiltDegrees() {
        return 14f;
    }

    /** 背景に浮遊する発光パーティクル（環境アニメーション）を表示するか。 */
    default boolean udl$showSparkles() {
        return true;
    }

    /**
     * 区切り線を挿入するテキスト行インデックスのリスト（0始まり）。
     * 例: {@code List.of(0)} を返すと、0行目（通常はアイテム名）の直後に区切り線が入る。
     */
    default List<Integer> udl$separatorAfterLines(ItemStack stack) {
        return List.of(0);
    }
}