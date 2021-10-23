package com.ysy.switcherfiveg

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.Collections

internal class ReflectUtils(private val type: Class<*>, private val any: Any) {

    constructor(type: Class<*>) : this(type, type)

    companion object {

        ///////////////////////////////////////////////////////////////////////////
        // reflect
        ///////////////////////////////////////////////////////////////////////////

        @Throws(ReflectException::class)
        fun reflect(className: String) = reflect(forName(className))

        @Throws(ReflectException::class)
        fun reflect(className: String, classLoader: ClassLoader) = reflect(
            forName(
                className,
                classLoader
            )
        )

        @Throws(ReflectException::class)
        fun reflect(clazz: Class<*>) = ReflectUtils(clazz)

        @Throws(ReflectException::class)
        fun reflect(any: Any) = ReflectUtils(any.javaClass, any)

        private fun forName(className: String) = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw ReflectException(e)
        }

        private fun forName(name: String, classLoader: ClassLoader): Class<*> = try {
            Class.forName(name, true, classLoader)
        } catch (e: ClassNotFoundException) {
            throw ReflectException(e)
        }

        ///////////////////////////////////////////////////////////////////////////
        // proxy
        ///////////////////////////////////////////////////////////////////////////

        private fun property(string: String): String {
            return when (string.length) {
                0 -> ""
                1 -> string.toLowerCase()
                else -> string.substring(0, 1).toLowerCase() + string.substring(1)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // newInstance
    ///////////////////////////////////////////////////////////////////////////

    fun newInstance(): ReflectUtils = newInstance(*emptyArray())

    fun newInstance(vararg args: Any): ReflectUtils {
        val types = getArgsType(*args)
        return try {
            val constructor = type().getDeclaredConstructor(*types)
            newInstance(constructor, *args)
        } catch (e: NoSuchMethodException) {
            val list = arrayListOf<Constructor<*>>()
            type().declaredConstructors.forEach { constructor ->
                if (match(constructor.parameterTypes, types)) {
                    list.add(constructor)
                }
            }
            if (list.isEmpty()) {
                throw ReflectException(e)
            } else {
                sortConstructors(list)
                newInstance(list[0], *args)
            }
        }
    }

    private fun getArgsType(vararg args: Any): Array<Class<*>> {
        val result = Array<Class<*>>(args.size) { Any::class.java }
        for (i in args.indices) {
            val value = args[i]
            result[i] = value.javaClass
        }
        return result
    }

    private fun sortConstructors(list: List<Constructor<*>>) {
        Collections.sort(list, Comparator { o1, o2 ->
            val types1 = o1.parameterTypes
            val types2 = o2.parameterTypes
            for (i in types1.indices) {
                if (!types1[i].equals(types2[i])) {
                    if (wrapper(types1[i]).isAssignableFrom(wrapper(types2[i]))) {
                        return@Comparator 1
                    } else {
                        return@Comparator -1
                    }
                }
            }
            0
        })
    }

    private fun newInstance(constructor: Constructor<*>, vararg args: Any): ReflectUtils {
        return try {
            ReflectUtils(
                constructor.declaringClass,
                accessible(constructor).newInstance(*args)
            )
        } catch (e: Exception) {
            throw ReflectException(e)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // field
    ///////////////////////////////////////////////////////////////////////////

    fun field(name: String) = try {
        val field = getField(name)
        ReflectUtils(field.type, field.get(any)!!)
    } catch (e: Exception) {
        throw ReflectException(e)
    }

    fun field(name: String, value: Any) = try {
        val field = getField(name)
        field.set(any, unwrap(value))
        this
    } catch (e: Exception) {
        throw ReflectException(e)
    }

    @Throws(IllegalAccessException::class)
    private fun getField(name: String): Field {
        val field = getAccessibleField(name)
        if ((field.modifiers and Modifier.FINAL) == Modifier.FINAL) {
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
        try {
            return accessible(type!!.getField(name))
        } catch (e: NoSuchFileException) {
            do {
                try {
                    return accessible(type!!.getDeclaredField(name))
                } catch (ignore: NoSuchFileException) {
                }
                type = type!!.superclass
            } while (type != null)
            throw ReflectException(e)
        }
    }

    private fun unwrap(any: Any) = if (any is ReflectUtils) any.get() else any

    ///////////////////////////////////////////////////////////////////////////
    // method
    ///////////////////////////////////////////////////////////////////////////

    @Throws(ReflectException::class)
    fun method(name: String) = method(name, *emptyArray())

    @Throws(ReflectException::class)
    fun method(name: String, vararg args: Any): ReflectUtils {
        val types = getArgsType(*args)
        return try {
            method(exactMethod(name, types), any, *args)
        } catch (e: NoSuchMethodException) {
            try {
                method(similarMethod(name, types), any, *args)
            } catch (e1: NoSuchMethodException) {
                throw ReflectException(e1)
            }
        }
    }

    private fun method(method: Method, obj: Any, vararg args: Any) = try {
        accessible(method)
        if (method.returnType == Void.TYPE) {
            method.invoke(obj, *args)
            reflect(obj)
        } else {
            reflect(method.invoke(obj, *args)!!)
        }
    } catch (e: Exception) {
        throw ReflectException(e)
    }

    @Throws(NoSuchMethodException::class)
    private fun exactMethod(name: String, types: Array<Class<*>>): Method {
        var type: Class<*>? = type()
        try {
            return type!!.getMethod(name, *types)
        } catch (e: NoSuchFileException) {
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
        val methods = arrayListOf<Method>()
        type!!.methods.forEach { method ->
            if (isSimilarSignature(method, name, types)) {
                methods.add(method)
            }
        }
        if (methods.isNotEmpty()) {
            sortMethods(methods)
            return methods[0]
        }
        do {
            type!!.declaredMethods.forEach { method ->
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
            "No similar method $name with params " +
                    "${types.contentToString()} could be found on type ${type()}."
        )
    }

    private fun sortMethods(methods: List<Method>) {
        Collections.sort(methods, Comparator { o1, o2 ->
            val types1 = o1.parameterTypes
            val types2 = o2.parameterTypes
            for (i in types1.indices) {
                if (!types1[i].equals(types2[i])) {
                    if (wrapper(types1[i]).isAssignableFrom(wrapper(types2[i]))) {
                        return@Comparator 1
                    } else {
                        return@Comparator -1
                    }
                }
            }
            0
        })
    }

    private fun isSimilarSignature(
        possiblyMatchingMethod: Method,
        desiredMethodName: String,
        desiredParamTypes: Array<Class<*>>
    ) = (possiblyMatchingMethod.name.equals(desiredMethodName)
            && match(possiblyMatchingMethod.parameterTypes, desiredParamTypes))

    private fun match(declaredTypes: Array<Class<*>>, actualTypes: Array<Class<*>>): Boolean {
        if (declaredTypes.size == actualTypes.size) {
            for (i in actualTypes.indices) {
                if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) {
                    continue
                }
                return false
            }
            return true
        } else {
            return false
        }
    }

    private fun <T : AccessibleObject> accessible(accessible: T): T {
        if (accessible is Member) {
            val member: Member = accessible
            if (Modifier.isPublic(member.modifiers)
                && Modifier.isPublic(member.declaringClass.modifiers)
            ) {
                return accessible
            }
        }
        if (!accessible.isAccessible) accessible.isAccessible = true
        return accessible
    }

    ///////////////////////////////////////////////////////////////////////////
    // proxy
    ///////////////////////////////////////////////////////////////////////////

    @Suppress("UNCHECKED_CAST")
    fun <P> proxy(proxyType: Class<P>): P {
        val isMap = any is Map<*, *>
        val handler = InvocationHandler { _, method, args ->
            val name = method.name
            try {
                return@InvocationHandler reflect(any).method(name, args).get()
            } catch (e: ReflectException) {
                if (isMap) {
                    val map = any as MutableMap<String, Any>
                    val length = args?.size ?: 0
                    if (length == 0 && name.startsWith("get")) {
                        return@InvocationHandler map[property(name.substring(3))]
                    } else if (length == 0 && name.startsWith("is")) {
                        return@InvocationHandler map[property(name.substring(2))]
                    } else if (length == 1 && name.startsWith("set")) {
                        map[property(name.substring(3))] = args!![0]
                        return@InvocationHandler null
                    }
                }
                throw e
            }
        }
        return Proxy.newProxyInstance(
            proxyType.classLoader, arrayOf<Class<*>>(proxyType),
            handler
        ) as P
    }

    private fun type() = type

    private fun wrapper(type: Class<*>): Class<*> {
        if (type.isPrimitive) {
            when {
                Boolean::class.javaPrimitiveType == type -> {
                    return Boolean::class.javaObjectType
                }
                Int::class.javaPrimitiveType == type -> {
                    return Int::class.javaObjectType
                }
                Long::class.javaPrimitiveType == type -> {
                    return Long::class.javaObjectType
                }
                Short::class.javaPrimitiveType == type -> {
                    return Short::class.javaObjectType
                }
                Byte::class.javaPrimitiveType == type -> {
                    return Byte::class.javaObjectType
                }
                Double::class.javaPrimitiveType == type -> {
                    return Double::class.javaObjectType
                }
                Float::class.javaPrimitiveType == type -> {
                    return Float::class.javaObjectType
                }
                Char::class.javaPrimitiveType == type -> {
                    return Char::class.javaObjectType
                }
                Void.TYPE == type -> {
                    return Void::class.javaObjectType
                }
            }
        }
        return type
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(): T {
        return any as T
    }

    override fun hashCode() = any.hashCode()

    override fun equals(other: Any?): Boolean {
        return other is ReflectUtils && any == other.get()
    }

    override fun toString() = any.toString()

    class ReflectException : RuntimeException {
        constructor(message: String) : super(message)
        constructor(message: String, cause: Throwable) : super(message, cause)
        constructor(cause: Throwable) : super(cause)

        companion object {
            private const val serialVersionUID = 858774075258496016L
        }
    }
}
