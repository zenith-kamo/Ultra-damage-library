package com.zenith.udl.transformer.rules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class UpdateTitleRule implements IMethodTransformerRule {
    @Override
    public boolean matches(ClassNode classNode, MethodNode methodNode) {
        return classNode.name.equals("net/minecraft/client/Minecraft")
                && methodNode.name.equals("updateTitle")
                && methodNode.desc.equals("()V");
    }

    @Override
    public void apply(ClassNode classNode, MethodNode methodNode) {
        for (AbstractInsnNode insn = methodNode.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode methodInsn) {
                boolean isCreateTitle = methodInsn.owner.equals("net/minecraft/client/Minecraft")
                        && methodInsn.name.equals("createTitle")
                        && methodInsn.desc.equals("()Ljava/lang/String;");

                if (isCreateTitle) {
                    // スタックバランスを整えるため、直前の ALOAD 0 (this) も削除
                    AbstractInsnNode prev = methodInsn.getPrevious();
                    if (prev instanceof VarInsnNode varInsnNode) {
                        if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 0) {
                            methodNode.instructions.remove(varInsnNode);
                        }
                    }
                    // createTitle() の呼び出しを "UDL!!" に置き換え
                    methodNode.instructions.set(methodInsn, new LdcInsnNode("まいんくらふと♡ ふぉーじ 1.20.1～"));
                    break;
                }
            }
        }
    }
}