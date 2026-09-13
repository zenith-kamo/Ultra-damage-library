package com.zenith.udl.transformer.rules;

import com.zenith.udl.Udl;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class UpdateTitleRule implements IMethodTransformerRule {

    private static final String[] POSSIBLE_METHOD_NAMES = {
            "updateTitle",
            "m_91341_",
    };

    @Override
    public boolean matches(ClassNode classNode, MethodNode methodNode) {
        boolean isTargetClass = classNode.name.equals("net/minecraft/client/Minecraft");
        boolean isTargetMethod = false;

        for (String name : POSSIBLE_METHOD_NAMES) {
            if (methodNode.name.equals(name) && methodNode.desc.equals("()V")) {
                isTargetMethod = true;
                break;
            }
        }

        if (isTargetClass && methodNode.desc.equals("()V")) {
            Udl.LOGGER.info("[UDL] [UpdateTitleRule] Checking method: {}{} (matched: {})",
                    methodNode.name, methodNode.desc, isTargetMethod);
        }

        return isTargetClass && isTargetMethod;
    }

    @Override
    public void apply(ClassNode classNode, MethodNode methodNode) {
        for (AbstractInsnNode insn = methodNode.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode methodInsn) {
                boolean isCreateTitle = methodInsn.owner.equals("net/minecraft/client/Minecraft")
                        && (methodInsn.name.equals("createTitle") || methodInsn.name.startsWith("m_"))
                        && methodInsn.desc.equals("()Ljava/lang/String;");

                if (isCreateTitle) {
                    AbstractInsnNode prev = methodInsn.getPrevious();
                    if (prev instanceof VarInsnNode varInsnNode) {
                        if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 0) {
                            methodNode.instructions.remove(varInsnNode);
                        }
                    }
                    methodNode.instructions.set(methodInsn, new LdcInsnNode("UDL!!"));
                    Udl.LOGGER.info("[UDL] [UpdateTitleRule] Replaced createTitle with 'UDL!!'");
                    break;
                }
            }
        }
    }
}