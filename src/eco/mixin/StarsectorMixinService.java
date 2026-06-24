package eco.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinServiceAbstract;
import org.spongepowered.tools.agent.MixinAgent;

public class StarsectorMixinService extends MixinServiceAbstract {

    private static volatile IMixinTransformer cachedTransformer;

    public static IMixinTransformer getCachedTransformer() {
        return cachedTransformer;
    }

    @Override
    public String getName() {
        return "StarsectorMixinService";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return MixinEnvironment.Phase.DEFAULT;
    }

    @Override
    public IClassProvider getClassProvider() {
        return new IClassProvider() {
            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                return Class.forName(name, false, getClassLoader());
            }

            @Override
            public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
                return Class.forName(name, initialize, getClassLoader());
            }

            @Override
            public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
                return findClass(name, initialize);
            }

            @Override
            public URL[] getClassPath() {
                return new URL[0];
            }
        };
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return new IClassBytecodeProvider() {
            @Override
            public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
                return getClassNode(name, true, ClassReader.EXPAND_FRAMES);
            }

            @Override
            public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
                return getClassNode(name, runTransformers, ClassReader.EXPAND_FRAMES);
            }

            @Override
            public ClassNode getClassNode(String name, boolean runTransformers, int flags) throws ClassNotFoundException, IOException {
                byte[] bytes = getClassBytes(name, name);
                if (bytes == null) {
                    return null;
                }
                ClassReader reader = new ClassReader(bytes);
                ClassNode node = new ClassNode();
                reader.accept(node, flags);
                return node;
            }

            private byte[] getClassBytes(String name, String transformedName) throws IOException {
                String resourceName = transformedName.replace('.', '/') + ".class";
                InputStream is = getClassLoader().getResourceAsStream(resourceName);
                if (is == null) {
                    return null;
                }
                byte[] bytes = is.readAllBytes();
                is.close();
                return bytes;
            }
        };
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        ContainerHandleVirtual container = new ContainerHandleVirtual("CoreCracking");
        container.setAttribute("MixinConfigs", "mixin/corecracking.json");
        return container;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public void init() {
        IMixinTransformerFactory factory = getInternal(IMixinTransformerFactory.class);
        if (factory != null) {
            cachedTransformer = factory.createTransformer();
            new MixinAgent(cachedTransformer);
        }
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        Set<ClassLoader> tried = new HashSet<>();
        ClassLoader[] loaders = {
            Thread.currentThread().getContextClassLoader(),
            getClass().getClassLoader(),
            ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader cl : loaders) {
            while (cl != null && tried.add(cl)) {
                InputStream is = cl.getResourceAsStream(name);
                if (is != null) {
                    return is;
                }
                cl = cl.getParent();
            }
        }
        return null;
    }

    private ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            return cl;
        }
        cl = getClass().getClassLoader();
        if (cl != null) {
            return cl;
        }
        return ClassLoader.getSystemClassLoader();
    }
}
