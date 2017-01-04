ASM-NonClassloadingExtensions
=============================

Provides non-classloading alternatives to implementations within ObjectWeb ASM.

For the most part, ASM deals only with Java bytecode, and avoids using `java.lang.Class` and related classloading, in it's implementations. However, there are a small number of corner cases where ASM loads classes, in order to obtain more detailed information about the type.

Classloading can be problematic in some cases, such as static analysis, where it's necessary to avoid executing the code (including class initializer blocks) and where loading the classes of a large codebase can exhaust memory. Class loading can also be a problem in runtime class generation, if the class doesn't exist yet, and thus cannot be loaded, but it's still necessary to know some information about the type, such as it's superclass, or the interfaces it implements.
 
Examples of loading classes in ASM include:
 * `ClassWriter` which loads classes in order to find the common superclass between two types
 * `SimpleVerifier` which loads classes to know whether the type represents an interface, what it's superclass is, and if one type can be assigned to another
 
Both of these examples can be overridden, which is where ASM-NonClassloadingExtensions comes in: it provides two drop-in replacements that can achieve the same functionality without loading classes, by reading the class files instead.

Drop-in Replacements
====================

 * if you want a `ClassWriter` that doesn't load classes, use an instance of `org.mutabilitydetector.asm.NonClassloadingClassWriter` 
 * if you want a `SimpleVerifier` that doesn't load classes, use an instance of `org.mutabilitydetector.asm.tree.analysis.NonClassloadingSimpleVerifier`
 
In both cases, you should provide an implementation of `TypeHierarchyReader`. The default implementation:

 * expects to find the bytecode using `ClassLoader.getSystemResourceAsStream`. For static analysis or class generation, this is unlikely to be what you want, and you need to provide a way to retrieve the relevant bytecode to scan.
 * performs no caching of the classfiles it reads. Thus will likely be slower than the equivalent classloading version, since the JVM performs caching of `Class` instances for you implicitly.
 
This library makes no attempt to guess at how you want to retrieve class files, or which caching strategy suits you best, so instead provides the hooks to allow you to plug in those specific bits, and still use the library to perform the class reading and logic. Both subclassing and wrapping a `TypeHierarchyReader` are supported, and there's a couple of simple implementations available.

Querying type information 
=========================

In order to implement these non-classloading extensions, it was necessary to replace the job performed by `java.lang.Class` but without using a `ClassLoader` to retrieve instance of `Class`. The implementations of these were then found to be general enough that they might just be useful for querying type information, that you could normally only get via an instance of `Class`. In this codebase, `TypeHierarchy` provides an analogue to `Class`, and `TypeHierarchyReader` provides an analogue to `ClassLoader`.

So if you're doing static analysis of bytecode and you need:
 * to know if one type can be assigned to another
 * to be able to discover an entire type hierarchy all the way back to `java.lang.Object`
 
Then ASM-NonClassloadingExtensions may help you.

Disclaimer: if all you need to know is the immediate superclass or interfaces of a type, or if the class is an interface, that's already trivial with ASM, you probably don't need another library for that. However, the implementation of `TypeHierarchyReader#obtainHierarchyOf(type)` can show you how to do it.







 

 
 

 
 