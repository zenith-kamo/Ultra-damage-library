package com.zenith.udl.transformer;

import com.zenith.udl.transformer.rules.LivingEntityRule;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import com.zenith.udl.Udl;
import com.zenith.udl.transformer.rules.IMethodTransformerRule;
import com.zenith.udl.transformer.rules.UpdateTitleRule;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UdlTransformer {
    private static boolean initialized = false;

    // 拡張性を考慮し、変換ルールをリストで管理する
    private static final List<IMethodTransformerRule> RULES = new ArrayList<>();

    static {
        // 今後新しい機能を追加する際は、ここに new YourNewRule() を追加するだけ
        RULES.add(new UpdateTitleRule());
        RULES.add(new LivingEntityRule());
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

    /**
     * ClassNode に対して登録されているすべてのルールを適用します。
     */
    public static int transform(ClassNode classNode) {
        boolean modified = false;

        for (IMethodTransformerRule rule : RULES) {
            for (var method : classNode.methods) {
                if (rule.matches(classNode, method)) {
                    rule.apply(classNode, method);
                    modified = true;
                    Udl.LOGGER.info("[UDLTransformer] Applied rule [{}] to {}.{}",
                            rule.getClass().getSimpleName(), classNode.name, method.name);
                }
            }
        }

        return modified ? ILaunchPluginService.ComputeFlags.SIMPLE_REWRITE
                : ILaunchPluginService.ComputeFlags.NO_REWRITE;
    }
}