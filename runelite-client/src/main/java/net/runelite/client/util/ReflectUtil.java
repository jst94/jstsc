/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2018, Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.util;

import com.google.common.io.ByteStreams;
import com.google.inject.Injector;
import com.google.inject.Key;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtil {

	private static final String CLASS_FILE_PATH = "/" + PrivateLookupHelper.class.getName().replace('.', '/') + ".class";
	private static Set<Class<?>> annotationClasses = Collections.newSetFromMap(new WeakHashMap<>());

	public static MethodHandles.Lookup getPrivateLookup(Class<?> clazz) {
		try {
			MethodHandles.Lookup callerLookup = (clazz.getClassLoader() instanceof PrivateLookupableClassLoader)
					? ((PrivateLookupableClassLoader) clazz.getClassLoader()).getLookup()
					: MethodHandles.lookup();
			return MethodHandles.privateLookupIn(clazz, callerLookup);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void installLookupHelper(PrivateLookupableClassLoader classLoader) {
		try (InputStream in = ReflectUtil.class.getResourceAsStream(CLASS_FILE_PATH)) {
			byte[] classData = ByteStreams.toByteArray(in);
			Class<?> clazz = classLoader.defineClass0(PrivateLookupHelper.class.getName(), classData, 0, classData.length);
			clazz.getDeclaredConstructor().newInstance();
			clazz.getConstructor().newInstance();
		} catch (IOException | ReflectiveOperationException e) {
			throw new RuntimeException("Unable to install lookup helper", e);
		}
	}

	public static class PrivateLookupHelper {
		static {
			ClassLoader classLoader = PrivateLookupHelper.class.getClassLoader();
			if (classLoader instanceof PrivateLookupableClassLoader) {
				PrivateLookupableClassLoader pcl = (PrivateLookupableClassLoader) classLoader;
				pcl.setLookup(MethodHandles.lookup());
			}
		}
	}

	public interface PrivateLookupableClassLoader {
		void setLookup(MethodHandles.Lookup lookup);

		Class<?> defineClass0(String name, byte[] classData, int i, int length);

		MethodHandles.Lookup getLookup();
	}

	public static synchronized void queueInjectorAnnotationCacheInvalidation(Injector injector) {
		if (annotationClasses == null) return;

		for (Key<?> key : injector.getAllBindings().keySet()) {
			for (Class<?> clazz = key.getTypeLiteral().getRawType(); clazz != null; clazz = clazz.getSuperclass()) {
				annotationClasses.add(clazz);
			}
		}
	}

	public static synchronized void invalidateAnnotationCaches() {
		try {
			for (Class<?> clazz : annotationClasses) {
				invalidateMethodAnnotations(clazz.getDeclaredMethods());
				invalidateFieldAnnotations(clazz.getDeclaredFields());
				invalidateConstructorAnnotations(clazz.getDeclaredConstructors());
			}
		} catch (Exception ex) {
			log.debug(null, ex);
		} finally {
			annotationClasses.clear();
			annotationClasses = null;
		}
	}

	private static void invalidateMethodAnnotations(Method[] methods) throws Exception {
		for (Method method : methods) {
			uncacheAnnotations(method, Executable.class);
		}
	}

	private static void invalidateFieldAnnotations(Field[] fields) throws Exception {
		for (Field field : fields) {
			uncacheAnnotations(field, Field.class);
		}
	}

	private static void invalidateConstructorAnnotations(Constructor<?>[] constructors) throws Exception {
		for (Constructor<?> constructor : constructors) {
			uncacheAnnotations(constructor, Executable.class);
		}
	}

	private static void uncacheAnnotations(final Object object, Class<?> declaredAnnotationsClazz) throws Exception {
		if (object == null) return;

		Field declaredAnnotations = declaredAnnotationsClazz.getDeclaredField("declaredAnnotations");
		declaredAnnotations.setAccessible(true);

		synchronized (object) {
			Map<Class<? extends Annotation>, Annotation> cache = (Map) declaredAnnotations.get(object);
			if (cache != null && cache != Collections.<Class<? extends Annotation>, Annotation>emptyMap()) {
				declaredAnnotations.set(object, null);
			}
		}

		Field rootField = object.getClass().getDeclaredField("root");
		rootField.setAccessible(true);
		final Object root = rootField.get(object);
		uncacheAnnotations(root, declaredAnnotationsClazz);
	}
}
