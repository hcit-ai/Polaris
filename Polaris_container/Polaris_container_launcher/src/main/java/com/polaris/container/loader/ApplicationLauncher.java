package com.polaris.container.loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.ClassReader;

public class ApplicationLauncher {
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationLauncher.class);
	private static final String DOT_CLASS = ".class";
	private static final String MANIFEST_FILE = "META-INF/MANIFEST.MF";
	private static final String JRE_LIB = "/jre/lib/";
	
	public static void main(String[] args) throws IOException {
		ApplicationLauncher.scanMainClass(args);
	}
	
    private static void scanMainClass(String[] args) throws IOException {  
        Enumeration<URL> urls = ApplicationLauncher.class.getClassLoader().getResources(MANIFEST_FILE);
        while(urls.hasMoreElements()){
            URL url = urls.nextElement();
            String path = java.net.URLDecoder.decode(url.getPath(),Charset.defaultCharset().name());
            if (path.contains(JRE_LIB)) {
                continue;
            }
            path = path.substring(5).split("!")[0];
            File file = new File(path);
            try(JarFile jarFile = new JarFile(file)) {
                for(Enumeration<JarEntry> enumeration =  jarFile.entries(); enumeration.hasMoreElements(); ) {
                    JarEntry jarEntry = enumeration.nextElement();
                    if (jarEntry.getName().endsWith(DOT_CLASS)) {
                        try (InputStream inputStream = new BufferedInputStream(jarFile.getInputStream(jarEntry))) {
                            ClassDescriptor classDescriptor = createClassDescriptor(inputStream);
                            if (classDescriptor != null && 
                                classDescriptor.isMainMethodFound() && 
                                classDescriptor.isTargetAnnotationFound()) {
                                String className = convertToClassName(jarEntry.getName());
                                logger.info("startup class:{}",className);
                                Class<?> startClass =Class.forName(className);
                                Method method = startClass.getMethod("main", String[].class);
                                method.invoke(null, (Object)args);
                                return;
                            }
                        } catch (Exception e) {
                            logger.error("ERROR:{}",e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("ERROR:{}",e);
            }
        }
        logger.error("startup class is not found"); 
	}
    
    private static ClassDescriptor createClassDescriptor(InputStream inputStream) {
		try {
			ClassReader classReader = new ClassReader(inputStream);
			ClassDescriptor classDescriptor = new ClassDescriptor();
			classReader.accept(classDescriptor, ClassReader.SKIP_CODE);
			return classDescriptor;
		} catch (IOException ex) {
			logger.error("ERROR:",ex);
			return null;
		}
	}
    
    private static String convertToClassName(String name) {
		name = name.replace('/', '.');
		name = name.replace('\\', '.');
		name = name.substring(0, name.length() - DOT_CLASS.length());
		return name;
	}
}
