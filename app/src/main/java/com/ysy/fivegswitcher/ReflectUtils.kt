package com.ysy.fivegswitcher

import java.lang.reflect.*
import java.util.*

class ReflectUtils private constructor(private val type: Class<*>,
                                       private val obj: Any? = type) {

    companion object {
        /**
         * Reflect the class.
         *
         * @param className The name of class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(className: String): ReflectUtils = reflect(forName(className))

        /**
         * Reflect the class.
         *
         * @param className   The name of class.
         * @param classLoader The loader of class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(className: String, classLoader: ClassLoader): ReflectUtils =
            reflect(forName(className, classLoader))

        /**
         * Reflect the class.
         *
         * @param clazz The class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(clazz: Class<*>): ReflectUtils = ReflectUtils(clazz)

        /**
         * Reflect the class.
         *
         * @param obj The object.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(obj: Any?): ReflectUtils =
            ReflectUtils(obj?.javaClass ?: Any::class.java, obj)

        private fun forName(className: String): Class<*> =
            try {
                Class.forName(className)
            } catch (e: ClassNotFoundException) {
                throw ReflectException(e)
            }

        private fun forName(name: String, classLoader: ClassLoader): Class<*> =
            try {
                Class.forName(name, true, classLoader)
            } catch (e: ClassNotFoundException) {
                throw ReflectException(e)
            }

        /**
         * Get the POJO property name of an getter/setter
         */
        private fun property(string: String): String = when (string.length) {
            0 -> ""
            1 -> string.toLowerCase(Locale.getDefault())
            else -> string.substring(0, 1)
                .toLowerCase(Locale.getDefault()) + string.substring(1)
        }
    }

    /**
     * Create and initialize a new instance.
     *
     * @param args The args.
     * @return the single [ReflectUtils] instance
     */
    fun newInstance(vararg args: Any?): ReflectUtils {
        val types = getArgsType(args)
        return try {
            val constructor = type().getDeclaredConstructor(*types)
            newInstance(constructor, args)
        } catch (e: NoSuchMethodException) {
            val list: MutableList<Constructor<*>> = ArrayList()
            for (constructor in type().declaredConstructors) {
                if (match(constructor.parameterTypes, types)) {
                    list.add(constructor)
                }
            }
            if (list.isEmpty()) {
                throw ReflectException(e)
            } else {
                sortConstructors(list)
                newInstance(list[0], args)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getArgsType(vararg args: Any?): Array<Class<*>> {
        if (args.isEmpty()) return emptyArray()
        val result: Array<Class<*>?> = arrayOfNulls(args.size)
        for (i in args.indices) {
            val value = args[i]
            result[i] = value?.javaClass ?: NULL::class.java
        }
        return result as Array<Class<*>>
    }

    private fun sortConstructors(list: List<Constructor<*>>) {
        Collections.sort(list, Comparator { o1, o2 ->
            val types1 = o1.parameterTypes
            val types2 = o2.parameterTypes
            for (i in types1.indices) {
                if (types1[i] != types2[i]) {
                    return@Comparator if (wrapper(types1[i]).isAssignableFrom(wrapper(types2[i]))) {
                        1
                    } else {
                        -1
                    }
                }
            }
            0
        })
    }

    private fun newInstance(constructor: Constructor<*>, vararg args: Any): ReflectUtils =
        try {
            ReflectUtils(constructor.declaringClass,
                accessible(constructor).newInstance(args))
        } catch (e: Exception) {
            throw ReflectException(e)
        }

    /**
     * Get the field.
     *
     * @param name The name of field.
     * @return the single [ReflectUtils] instance
     */
    fun field(name: String): ReflectUtils =
        try {
            val field = getField(name)
            ReflectUtils(field.type, field.get(obj))
        } catch (e: IllegalAccessException) {
            throw ReflectException(e)
        }

    /**
     * Set the field.
     *
     * @param name  The name of field.
     * @param value The value.
     * @return the single [ReflectUtils] instance
     */
    fun field(name: String, value: Any): ReflectUtils =
        try {
            val field = getField(name)
            field.set(obj, unwrap(value))
            this
        } catch (e: Exception) {
            throw ReflectException(e)
        }

    @Throws(IllegalAccessException::class)
    private fun getField(name: String): Field {
        val field = getAccessibleField(name)
        if (field.modifiers and Modifier.FINAL == Modifier.FINAL) {
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            } catch (ignore: NoSuchFieldException) {
                // runs in android will happen
                field.isAccessible = true
            }
        }
        return field
    }

    private fun getAccessibleField(name: String): Field {
        var type: Class<*>? = type()
        return try {
            accessible(type!!.getField(name))
        } catch (e: NoSuchFieldException) {
            do {
                try {
                    return accessible(type!!.getDeclaredField(name))
                } catch (ignore: NoSuchFieldException) {
                }
                type = type!!.superclass
            } while (type != null)
            throw ReflectException(e)
        }
    }

    private fun unwrap(obj: Any): Any = if (obj is ReflectUtils) obj.get() else obj

    /**
     * Invoke the method.
     *
     * @param name The name of method.
     * @param args The args.
     * @return the single [ReflectUtils] instance
     * @throws ReflectException if reflect unsuccessfully
     */
    @Throws(ReflectException::class)
    fun method(name: String, vararg args: Any?): ReflectUtils {
        val types = getArgsType(args)
        return try {
            val mtd = exactMethod(name, types)
            method(mtd, obj, args)
        } catch (e: NoSuchMethodException) {
            try {
                val mtd = similarMethod(name, types)
                method(mtd, obj, args)
            } catch (e1: NoSuchMethodException) {
                throw ReflectException(e1)
            }
        }
    }

    private fun method(method: Method, obj: Any?, vararg args: Any): ReflectUtils =
        try {
            accessible(method)
            if (method.returnType == Void.TYPE) {
                method.invoke(obj, *args)
                reflect(obj)
            } else {
                reflect(method.invoke(obj, args))
            }
        } catch (e: Exception) {
            throw ReflectException(e)
        }

    @Throws(NoSuchMethodException::class)
    private fun exactMethod(name: String, types: Array<Class<*>>): Method {
        var type: Class<*>? = type()
        return try {
            type!!.getMethod(name, *types)
        } catch (e: NoSuchMethodException) {
            do {
                try {
                    return type!!.getDeclaredMethod(name, *types)
                } catch (ignore: NoSuchMethodException) {
                }
                type = type!!.superclass
            } while (type != null)
            throw NoSuchMethodException()
        }
    }

    @Throws(NoSuchMethodException::class)
    private fun similarMethod(name: String, types: Array<Class<*>>): Method {
        var type: Class<*>? = type()
        val methods: MutableList<Method> = ArrayList()
        for (method in type!!.methods) {
            if (isSimilarSignature(method, name, types)) {
                methods.add(method)
            }
        }
        if (methods.isNotEmpty()) {
            sortMethods(methods)
            return methods[0]
        }
        do {
            for (method in type!!.declaredMethods) {
                if (isSimilarSignature(method, name, types)) {
                    methods.add(method)
                }
            }
            if (methods.isNotEmpty()) {
                sortMethods(methods)
                return methods[0]
            }
            type = type.superclass
        } while (type != null)
        throw NoSuchMethodException(
            "No similar method " + name + " with params "
                + types.contentToString() + " could be found on type " + type() + "."
        )
    }

    private fun sortMethods(methods: List<Method>) {
        Collections.sort(methods, Comparator { o1, o2 ->
            val types1 = o1.parameterTypes
            val types2 = o2.parameterTypes
            for (i in types1.indices) {
                if (types1[i] != types2[i]) {
                    return@Comparator if (wrapper(types1[i]).isAssignableFrom(wrapper(types2[i]))) {
                        1
                    } else {
                        -1
                    }
                }
            }
            0
        })
    }

    private fun isSimilarSignature(possiblyMatchingMethod: Method, desiredMethodName: String,
                                   desiredParamTypes: Array<Class<*>>): Boolean =
        possiblyMatchingMethod.name == desiredMethodName
            && match(possiblyMatchingMethod.parameterTypes, desiredParamTypes)

    private fun match(declaredTypes: Array<Class<*>>, actualTypes: Array<Class<*>>): Boolean {
        if (declaredTypes.size == actualTypes.size) {
            for (i in actualTypes.indices) {
                if (actualTypes[i] == NULL::class.java
                    || wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) {
                    continue
                }
                return false
            }
            return true
        } else return false
    }

    private fun <T : AccessibleObject> accessible(accessible: T): T =
        if (accessible is Member && Modifier.isPublic(accessible.modifiers)
            && Modifier.isPublic(accessible.declaringClass.modifiers)
        ) accessible else accessible.apply { isAccessible = true }

    /**
     * Create a proxy for the wrapped object allowing to typesafely invoke
     * methods on it using a custom interface.
     *
     * @param proxyType The interface type that is implemented by the proxy.
     * @return a proxy for the wrapped object
     */
    @Suppress("UNCHECKED_CAST")
    fun <P> proxy(proxyType: Class<P>): P {
        val isMap = obj is Map<*, *>
        val handler = InvocationHandler { _, method, args ->
            val name = method.name
            try {
                return@InvocationHandler reflect(obj)
            } catch (e: ReflectException) {
                if (isMap) {
                    val map = obj as MutableMap<String, Any>
                    val length = args?.size ?: 0
                    if (length == 0 && name.startsWith("get")) {
                        return@InvocationHandler map[property(name.substring(3))]
                    } else if (length == 0 && name.startsWith("is")) {
                        return@InvocationHandler map[property(name.substring(2))]
                    } else if (length == 1 && name.startsWith("set")) {
                        map[property(name.substring(3))] = args[0]
                        return@InvocationHandler null
                    }
                }
                throw e
            }
        }
        return Proxy.newProxyInstance(proxyType.classLoader,
            arrayOf<Class<*>>(proxyType), handler) as P
    }

    private fun type(): Class<*> = type

    private fun wrapper(type: Class<*>?): Class<*> = when {
        type == null -> NULL::class.java
        type.isPrimitive -> when {
            Boolean::class.javaPrimitiveType == type -> Boolean::class.java
            Int::class.javaPrimitiveType == type -> Int::class.java
            Long::class.javaPrimitiveType == type -> Long::class.java
            Short::class.javaPrimitiveType == type -> Short::class.java
            Byte::class.javaPrimitiveType == type -> Byte::class.java
            Double::class.javaPrimitiveType == type -> Double::class.java
            Float::class.javaPrimitiveType == type -> Float::class.java
            Char::class.javaPrimitiveType == type -> Char::class.java
            Void.TYPE == type -> Void::class.java
            else -> type
        }
        else -> type
    }

    /**
     * Get the result.
     *
     * @param <T> The value type.
     * @return the result
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(): T = obj as T

    override fun hashCode(): Int = obj.hashCode()

    override fun equals(other: Any?): Boolean = other is ReflectUtils && obj == other.get<Any>()

    override fun toString(): String = obj.toString()

    private class NULL

    class ReflectException : RuntimeException {
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
        constructor(cause: Throwable?) : super(cause)

        companion object {
            private const val serialVersionUID = 858774075258496016L
        }
    }
}
