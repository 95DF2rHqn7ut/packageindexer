## Design reasoning

Since this is likely indexable, I will try to not put too many keywords for others.

Here is the main goal: Avoid the global lock for the data structure, which is some form of a graph. The ideal data structure for this project would be some form of ACID-compliant fast asynchrounous graph system that supports a DAG. Some libraries and databases supply such functionality, but that would defeat the point of the exercise.

So here is how to implement it without relying on one:

1. Make a hashtable that allows fast lookup of nodes by name.
2. Each node points to all the nodes that depend on it.
3. Each node has a counter that counts how many nodes depend on it.
4. Make lock namespace: A hashtable-backed data structure that enables a thread to lock using a named "lock" within that namespace, i.e. set of names.
5. Use that to lock the set of node names each operation would work on

### READ

This is a simple dirty read, no locking. Works fine.

### DELETE

Lock the namespace, decrement all counters of other nodes the node points to, remove the node that is to be removed - but make sure its own counter is at zero

### ADD

Lock the namespace, distinguish between adding a new node or updating an existing node. In the latter case, the namespace changes, and has to be updated


## Docker

mvn clean package

docker build -t pm .

docker run --name pm -d -p 8080:8080 pm