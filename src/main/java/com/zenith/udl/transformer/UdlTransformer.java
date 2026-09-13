package com.zenith.udl.transformer;

import com.zenith.udl.transformer.rules.LivingEntityRule;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import com.zenith.udl.Udl;
import com.zenith.udl.transformer.rules.IMethodTransformerRule;
import com.zenith.udl.transformer.rules.UpdateTitleRule;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UdlTransformer {
    private static boolean initialized = false;

    private static final List<IMethodTransformerRule> RULES = new ArrayList<>();
    private static final Set<String> DUMPED_CLASSES = ConcurrentHashMap.newKeySet();

    static {
        RULES.add(new UpdateTitleRule());
        RULES.add(new LivingEntityRule());
        Udl.LOGGER.info("[UDL] Registered {} transformer rules.", RULES.size());
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
            Udl.LOGGER.info("[UDL] LaunchPlugin registered successfully: {}", plugin.name());
        } catch (Exception e) {
            Udl.LOGGER.error("[UDL] Failed to register launch plugin: {}", e.getMessage(), e);
        }
        initialized = true;
    }

    public static int transform(ClassNode classNode) {
        boolean modified = false;

        if (DUMPED_CLASSES.add(classNode.name)) {
            if (classNode.name.contains("Minecraft") ||
                    classNode.name.contains("LivingEntity") ||
                    classNode.name.equals("net/minecraft/client/Minecraft") ||
                    classNode.name.equals("net/minecraft/world/entity/LivingEntity")) {

                Udl.LOGGER.info("[UDL] ========== Class Dump: {} ==========", classNode.name);
                for (MethodNode m : classNode.methods) {
                    Udl.LOGGER.info("[UDL]   Method: {}{}", m.name, m.desc);
                }
                Udl.LOGGER.info("[UDL] ==========================================");
            }
        }

        for (IMethodTransformerRule rule : RULES) {
            for (MethodNode method : classNode.methods) {
                if (rule.matches(classNode, method)) {
                    try {
                        rule.apply(classNode, method);
                        modified = true;
                        Udl.LOGGER.info("[UDL] ✓ Applied rule [{}] to {}.{}{}",
                                rule.getClass().getSimpleName(),
                                classNode.name, method.name, method.desc);
                    } catch (Exception e) {
                        Udl.LOGGER.error("[UDL] ✗ Rule [{}] failed on {}.{}{}: {}",
                                rule.getClass().getSimpleName(),
                                classNode.name, method.name, method.desc, e.getMessage(), e);
                    }
                }
            }
        }

        return modified ? ILaunchPluginService.ComputeFlags.SIMPLE_REWRITE
                : ILaunchPluginService.ComputeFlags.NO_REWRITE;
    }
}