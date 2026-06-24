package eco.mixin;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.service.MixinService;

public class AgentEntry {

    public static void premain(String arg, Instrumentation inst) {
        try {
            Field f = Class.forName("org.spongepowered.tools.agent.MixinAgent")
                    .getDeclaredField("instrumentation");
            f.setAccessible(true);
            f.set(null, inst);

            MixinBootstrap.init();
            Mixins.addConfiguration("mixin/corecracking.json");

            StarsectorMixinService svc = (StarsectorMixinService) MixinService.getService();
            svc.init();

            IMixinTransformer transformer = StarsectorMixinService.getCachedTransformer();
            if (transformer != null) {
                inst.addTransformer(new ClassFileTransformer() {
                    public byte[] transform(ClassLoader loader, String name,
                                            Class<?> classBeingRedefined,
                                            ProtectionDomain protectionDomain,
                                            byte[] classfileBuffer) {
                        String dotted = name.replace('/', '.');
                        return transformer.transformClassBytes(dotted, dotted, classfileBuffer);
                    }
                }, false);
            }

            System.out.println("[CoreCracking] Mixin initialized");
        } catch (Throwable t) {
            System.err.println("[CoreCracking] Mixin failed: " + t);
        }
    }
}
