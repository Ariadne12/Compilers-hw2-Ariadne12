# Compilers-hw2-Ariadne12

ΑΜ: 1115202300083

We got MiniJava grammar in JavaCC form and we start by using the JTB tool to convert it into a grammar that produces class hierarchies.
Now we have the syntax tree (AST) and the visitor directories.

For the semantic analysis on this project we will use 2 visitors: one for storing all the iimportant information of the code in symbol tables and one for then analyzing them and type checking them to see if they are correct. This could be done with only one but i believe the 2 visitors is a cleaner approach.

Then for the names and offsets printing, a 3rd visitor could be used but by storing enough information in the first one we can recreate them accurately.

More technically, all the information for each class will be stored in a helper class called ClassData and respectively all the information for each method will be in class MethData.
We obviously make the fields all public since the deafult is private so we can access them from all the other files.
For easier access reasons we use ArrayList for any connected items.

We will repeatedly need to access classes, fields and methods by name.
We also need to keep the insertion order (for the future printing)

For the insertion order maybe we could keep the line we found it in as an

We are going to implement custom logic for the nodes we care about, for the others the default implementation from GJDepthFirst is going to be used automatically. So no expressions, statements, if/while, arithmetic
