package com.zenith.udl.transformer;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import com.zenith.udl.Udl;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Field;
import java.util.Map;

public class UdlTransformer {
    private static boolean initialized = false;

    public enum Phase {
        ILaunchPluginServiceBefore,
        ITransformationService,
        PostMixin,
        ILaunchPluginService,
        ClassFileTransformer
    }

    public static void ensureLaunchPluginInstalled() {
        initialize();
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        try {
            ILaunchPluginService plugin = new UdlTransformerPlugin();
            Field field = Launcher.class.getDeclaredField("launchPlugins");
            field.setAccessible(true);
            LaunchPluginHandler pluginHandler = (LaunchPluginHandler) field.get(Launcher.INSTANCE);
            field = LaunchPluginHandler.class.getDeclaredField("plugins");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILaunchPluginService> map = (Map<String, ILaunchPluginService>) field.get(pluginHandler);
            map.put(plugin.name(), plugin);
            Udl.LOGGER.info("[UDLTransformer] registered {}", plugin.name());
        } catch (Exception e) {
            Udl.LOGGER.error("[UDLTransformer] failed to register launch plugin", e);
        }
        initialized = true;
    }

    public static int transform(Phase phase, ClassNode classNode) {
        if (!classNode.name.equals("net/minecraft/client/Minecraft")) {
            return ILaunchPluginService.ComputeFlags.NO_REWRITE;
        }

        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("updateTitle") || !method.desc.equals("()V")) {
                continue;
            }

            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode methodInsn) {
                    boolean isCreateTitle = methodInsn.owner.equals("net/minecraft/client/Minecraft")
                            && methodInsn.name.equals("createTitle")
                            && methodInsn.desc.equals("()Ljava/lang/String;");

                    if (isCreateTitle) {
                        // 1. 直前の命令が ALOAD 0 (this) であることを確認して削除する
                        AbstractInsnNode prev = methodInsn.getPrevious();
                        if (prev instanceof VarInsnNode varInsnNode) {
                            if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 0) {
                                method.instructions.remove(varInsnNode);
                            }
                        }

                        method.instructions.set(methodInsn, new LdcInsnNode("まいんくらふと♡ ふぉーじ 1.20.1 - ゆーでぃーえる とらんすふぉーまあー"));

                        modified = true;
                        break; // 1つのメソッド内で対象を見つけたら抜けてOK
                    }
                }
            }
        }

        return modified ? ILaunchPluginService.ComputeFlags.SIMPLE_REWRITE
                : ILaunchPluginService.ComputeFlags.NO_REWRITE;
    }
}