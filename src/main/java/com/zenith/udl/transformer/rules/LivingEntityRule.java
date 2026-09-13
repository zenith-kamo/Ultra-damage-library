package com.zenith.udl.transformer.rules;

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

    @Override
    public boolean matches(ClassNode classNode, MethodNode methodNode) {
        if (!classNode.name.equals("net/minecraft/world/entity/LivingEntity")) {
            return false;
        }

        // getHealth() と getMaxHealth() の両方を対象にする
        return (methodNode.name.equals("getHealth") && methodNode.desc.equals("()F")) ||
                (methodNode.name.equals("getMaxHealth") && methodNode.desc.equals("()F"));
    }

    @Override
    public void apply(ClassNode classNode, MethodNode methodNode) {
        InsnList toInject = new InsnList();
        LabelNode originalCode = new LabelNode(); // 元の処理の先頭ラベル

        // 1. this (LivingEntity) をスタックに積む
        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));

        // 2. 対象メソッドに応じて、呼び出すTargetManagerのメソッドを切り替える
        if (methodNode.name.equals("getHealth")) {
            toInject.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    TARGET_MANAGER,
                    "isHealthTarget",
                    "(Lnet/minecraft/world/entity/LivingEntity;)Z",
                    false
            ));
        } else { // getMaxHealth
            toInject.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    TARGET_MANAGER,
                    "isKillTarget",
                    // LivingEntityはEntityを継承しているため、Entity型で受け取れる
                    "(Lnet/minecraft/world/entity/Entity;)Z",
                    false
            ));
        }

        // 3. 戻り値が false (0) なら、元の処理 (originalCode) にジャンプ
        toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalCode));

        // 4. true (1) の場合の処理: 0.0F を返してメソッドを抜ける
        toInject.add(new InsnNode(Opcodes.FCONST_0));
        toInject.add(new InsnNode(Opcodes.FRETURN));

        // 5. false の場合のジャンプ先（元の処理の先頭）を配置
        toInject.add(originalCode);

        // 6. 生成した命令リストをメソッドの先頭に挿入
        methodNode.instructions.insert(toInject);
    }
}