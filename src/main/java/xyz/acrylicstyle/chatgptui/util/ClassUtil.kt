package xyz.acrylicstyle.chatgptui.util

object ClassUtil {
    fun getSuperclassesAndInterfaces(clazz: Class<*>): Array<Class<*>> =
        clazz.interfaces +
                clazz.interfaces.flatMap { getSuperclassesAndInterfaces(it).toList() } +
                clazz.superclass?.let { getSuperclassesAndInterfaces(it) + it }.orEmpty()
}
