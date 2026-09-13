package com.zenith.udl.transformer;

import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

public class UdlTransformerPlugin implements ILaunchPluginService {
    @Override
    public String name() {
        return "udl_transformer_plugin";
    }

    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (!ITransformerActivity.CLASSLOADING_REASON.equals(reason)) {
            return ComputeFlags.NO_REWRITE;
        }

        // BEFORE と AFTER のどちらで呼ばれても、変換ロジックは同じなので統一して処理
        return UdlTransformer.transform(classNode);
    }

    @Override
    public EnumSet<Phase> handlesClass(Type type, boolean isEmpty) {
        String className = type.getClassName();

        // 自パッケージは変換対象外
        if (className.startsWith("com.zenith.udl.transformer")) {
            return EnumSet.noneOf(Phase.class);
        }

        // 変換対象のクラスを明示的に指定（パフォーマンス向上）
        // 今後新しいルールを追加する際は、ここに対象クラスを追加する
        if (className.equals("net.minecraft.client.Minecraft") ||
                className.equals("net.minecraft.world.entity.LivingEntity")) {
            return EnumSet.of(Phase.AFTER); // AFTER のみで十分
        }

        return EnumSet.noneOf(Phase.class);
    }
}