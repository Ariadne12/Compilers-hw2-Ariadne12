# Compilers-hw2-Ariadne12

Αριάδνη Κουκουδάκη
ΑΜ: 1115202300083

How to run:
make
(for some reason, i havent had time to investigate, it puts import org.w3c.dom.Node; on the top of TypeCheckingVisitor. I think i've fixed it but if it causes any problems remove it.)

We got MiniJava grammar in JavaCC form and we start by using the JTB tool to convert it into a grammar that produces class hierarchies.
Now we have the syntax tree (AST) and the visitor directories.

For the semantic analysis on this project we will use 2 visitors: one for storing all the important information of the code in symbol tables and one for then analyzing them and type checking them to see if they are correct. This could be done with only one but i believe the 2 visitors is a cleaner approach.

Then for the names and offsets printing, a 3rd visitor could be used but by storing enough information in the first one we can recreate them accurately.

More technically, all the information for each class will be stored in a helper class called ClassData and respectively all the information for each method will be in class MethData.
We obviously make the fields all public since the deafult is private so we can access them from all the other files.

We will repeatedly need to access classes, fields and methods by name.
We also need to keep the insertion order (for the future printing)

Since we dont care about the offset priting for the parameters maybe we keep them as list instead of a more complicated structure.

For the insertion order maybe we could keep the line we found it in as an

We are going to implement custom logic for the nodes we care about, for the others the default implementation from GJDepthFirst is going to be used automatically. So no expressions, statements, if/while or arithmetic.

While the symbol table gets filled is really practical to do some basic checking at that stage. For example we can easily check if a variable, a method or a class get reinitialized. We can also differentiate if there is any overloading or overriding happening.

At first i was thinking of combining that check happening in STV and the lookup of anything from the symbol table that would be needed in TPV since they had a lot of similar structure/code, but it became kinda complicated so i decided on simplicity and divided the 2 operations.

Important note/difference from original Java, where if the parameters are either sub or supertyps of each other they don't count as valid overloading.

For better delegation of duties, the addMethod in ClassData is totally for storing. the logic and actual checks happen in SymbolTable.

Finally since the STV really was going through everything, it was a convenient time to also collect the offsets. So by his finishing we had all the information we needed to print them.

Now onto the typechecking, to see if everything is correct. Going through the syntax tree a second time this visitor, finds all the semantic problems, overriding most of the class nodes of the AST.

It passes all the tests from the minijava-examples folder.

Didn't bother trying to catch array bounds violation for example since that is a runtime concern, not a static type-checking concern.