package com.zenith.udl.transformer.rules;

import com.zenith.udl.Udl;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LivingEntityRule implements IMethodTransformerRule {

    private static final String TARGET_MANAGER = "com/zenith/udl/manager/TargetManager";

    private static final String[] GET_HEALTH_NAMES = {
            "getHealth",
            "m_21223_",
    };

    private static final String[] GET_MAX_HEALTH_NAMES = {
            "getMaxHealth",
            "m_21233_",
    };

    // 追加: isAlive のメソッド名候補 (Mojang Mapping 名 + 難読化名候補)
    private static final String[] IS_ALIVE_NAMES = {
            "isAlive",
            "m_6084_"
    };

    // 追加: isDeadOrDying のメソッド名候補 (Mojang Mapping 名 + 難読化名候補)
    private static final String[] IS_DEAD_OR_DYING_NAMES = {
            "isDeadOrDying",
            "m_21224_"
    };

    @Override
    public boolean matches(ClassNode classNode, MethodNode methodNode) {
        if (!classNode.name.equals("net/minecraft/world/entity/LivingEntity")) {
            return false;
        }

        boolean isGetHealth = matchesAny(methodNode.name, GET_HEALTH_NAMES) && methodNode.desc.equals("()F");
        boolean isGetMaxHealth = matchesAny(methodNode.name, GET_MAX_HEALTH_NAMES) && methodNode.desc.equals("()F");

        // 追加: isAlive と isDeadOrDying のマッチング判定 (戻り値は boolean: Z)
        boolean isIsAlive = matchesAny(methodNode.name, IS_ALIVE_NAMES) && methodNode.desc.equals("()Z");
        boolean isIsDeadOrDying = matchesAny(methodNode.name, IS_DEAD_OR_DYING_NAMES) && methodNode.desc.equals("()Z");

        if (methodNode.desc.equals("()F") || methodNode.desc.equals("()Z")) {
            Udl.LOGGER.info("[UDL] [HealthOverride] Checking method: {}{} (health:{}, maxHealth:{}, isAlive:{}, isDeadOrDying:{})",
                    methodNode.name, methodNode.desc, isGetHealth, isGetMaxHealth, isIsAlive, isIsDeadOrDying);
        }

        return isGetHealth || isGetMaxHealth || isIsAlive || isIsDeadOrDying;
    }

    private boolean matchesAny(String name, String[] candidates) {
        for (String c : candidates) {
            if (name.equals(c)) return true;
        }
        return false;
    }

    @Override
    public void apply(ClassNode classNode, MethodNode methodNode) {
        InsnList toInject = new InsnList();
        LabelNode originalCode = new LabelNode();

        // this (LivingEntity) をスタックに積む
        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));

        boolean isHealth = matchesAny(methodNode.name, GET_HEALTH_NAMES);
        boolean isMaxHealth = matchesAny(methodNode.name, GET_MAX_HEALTH_NAMES);
        boolean isAlive = matchesAny(methodNode.name, IS_ALIVE_NAMES);
        boolean isDeadOrDying = matchesAny(methodNode.name, IS_DEAD_OR_DYING_NAMES);

        // isKillTarget のチェックを挿入
        // ※ isHealthTarget の場合は getHealth のみで判定していた既存ロジックを維持しつつ、
        //   isAlive / isDeadOrDying の場合は isKillTarget で判定するように統一しています。
        if (isHealth) {
            toInject.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, TARGET_MANAGER, "isHealthTarget",
                    "(Lnet/minecraft/world/entity/LivingEntity;)Z", false
            ));
        } else {
            toInject.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, TARGET_MANAGER, "isKillTarget",
                    "(Lnet/minecraft/world/entity/Entity;)Z", false
            ));
        }

        // 条件が false (0) なら元のコードへジャンプ
        toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalCode));

        // 条件が true の場合の早期リターン値を設定
        if (isHealth || isMaxHealth) {
            // HP関連は 0.0f を返す
            toInject.add(new InsnNode(Opcodes.FCONST_0));
            toInject.add(new InsnNode(Opcodes.FRETURN));
        } else if (isAlive) {
            // isAlive の場合は false (0) を返す
            toInject.add(new InsnNode(Opcodes.ICONST_0));
            toInject.add(new InsnNode(Opcodes.IRETURN));
        } else if (isDeadOrDying) {
            // isDeadOrDying の場合は true (1) を返す
            toInject.add(new InsnNode(Opcodes.ICONST_1));
            toInject.add(new InsnNode(Opcodes.IRETURN));
        }

        // 元のコードのラベル
        toInject.add(originalCode);

        // メソッドの先頭に挿入
        methodNode.instructions.insert(toInject);
        Udl.LOGGER.info("[UDL] [HealthOverride] Injected condition to {} (isHealth={}, isMaxHealth={}, isAlive={}, isDeadOrDying={})",
                methodNode.name, isHealth, isMaxHealth, isAlive, isDeadOrDying);
    }
}