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
        if (classNode.name.startsWith("com/zenith/udl/transformer")) {
            return ComputeFlags.NO_REWRITE;
        }
        if (!ITransformerActivity.CLASSLOADING_REASON.equals(reason)) {
            return ComputeFlags.NO_REWRITE;
        }
        return UdlTransformer.transform(phase == Phase.AFTER
                ? UdlTransformer.Phase.ILaunchPluginService
                : UdlTransformer.Phase.ILaunchPluginServiceBefore,
                classNode);
    }

    @Override
    public EnumSet<Phase> handlesClass(Type type, boolean isEmpty) {
        if (type.getClassName().startsWith("com.zenith.udl.transformer")) {
            return EnumSet.noneOf(Phase.class);
        }
        return EnumSet.of(Phase.AFTER, Phase.BEFORE);
    }
}
