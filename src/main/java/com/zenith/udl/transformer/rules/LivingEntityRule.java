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

    @Override
    public boolean matches(ClassNode classNode, MethodNode methodNode) {
        if (!classNode.name.equals("net/minecraft/world/entity/LivingEntity")) {
            return false;
        }

        boolean isGetHealth = matchesAny(methodNode.name, GET_HEALTH_NAMES) && methodNode.desc.equals("()F");
        boolean isGetMaxHealth = matchesAny(methodNode.name, GET_MAX_HEALTH_NAMES) && methodNode.desc.equals("()F");

        if (methodNode.desc.equals("()F")) {
            Udl.LOGGER.info("[UDL] [HealthOverride] Checking method: {}{} (health:{}, maxHealth:{})",
                    methodNode.name, methodNode.desc, isGetHealth, isGetMaxHealth);
        }

        return isGetHealth || isGetMaxHealth;
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

        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));

        boolean isHealth = matchesAny(methodNode.name, GET_HEALTH_NAMES);

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

        toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalCode));
        toInject.add(new InsnNode(Opcodes.FCONST_0));
        toInject.add(new InsnNode(Opcodes.FRETURN));
        toInject.add(originalCode);

        methodNode.instructions.insert(toInject);
        Udl.LOGGER.info("[UDL] [HealthOverride] Injected condition to {} (isHealth={})", methodNode.name, isHealth);
    }
}