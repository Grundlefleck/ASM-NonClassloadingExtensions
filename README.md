ASM-NonClassloadingExtensions
=============================

Provides non-classloading alternatives to implementations within ObjectWeb ASM.

For the most part, ASM deals only with Java bytecode, and avoids using `java.lang.Class` and related classloading, in it's implementations. However, there are a small number of corner cases where ASM loads classes, in order to obtain more detailed information about the type.
 
Examples include:
 * `ClassWriter` loads classes in order to find the common superclass between two types
 * `SimpleVerifier` loads classes to know whether the type represents an interface, what it's superclass is, and if one type can be assigned to another
 
Both of these examples can be overridden, which is where ASM-NonClassloadingExtensions comes in.


 

 
 

 
 