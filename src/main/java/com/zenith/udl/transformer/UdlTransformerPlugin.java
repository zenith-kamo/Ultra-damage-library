package com.zenith.udl.transformer;

import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import com.zenith.udl.Udl;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UdlTransformerPlugin implements ILaunchPluginService {

    private static final Set<String> LOGGED_CLASSES = ConcurrentHashMap.newKeySet();

    @Override
    public String name() {
        return "udl_transformer_plugin";
    }

    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        String className = classType.getClassName();

        if (!ITransformerActivity.CLASSLOADING_REASON.equals(reason)) {
            return ComputeFlags.NO_REWRITE;
        }

        int result = UdlTransformer.transform(classNode);

        if (result != ComputeFlags.NO_REWRITE) {
            Udl.LOGGER.info("[UDL] Successfully transformed class: {} (phase: {})", className, phase);
        }

        return result;
    }

    @Override
    public EnumSet<Phase> handlesClass(Type type, boolean isEmpty) {
        String className = type.getClassName();

        if (className.startsWith("com.zenith.udl.transformer")) {
            return EnumSet.noneOf(Phase.class);
        }

        if (className.contains("Minecraft") ||
                className.contains("LivingEntity") ||
                className.contains("Entity") ||
                className.equals("net/minecraft/client/Minecraft") ||
                className.equals("net/minecraft/world/entity/LivingEntity")) {

            if (LOGGED_CLASSES.add(className)) {
                Udl.LOGGER.info("[UDL] handlesClass hit - className: {}, internalName: {}",
                        className, type.getInternalName());
            }
        }

        if (className.equals("net.minecraft.client.Minecraft") ||
                className.equals("net.minecraft.world.entity.LivingEntity")) {
            return EnumSet.of(Phase.AFTER);
        }

        return EnumSet.noneOf(Phase.class);
    }
}