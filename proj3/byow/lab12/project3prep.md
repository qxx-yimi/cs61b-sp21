# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer:
The difference is that their implementation is recursive, while I used loops directly. Their implementation has more granular separation of functions, whereas in my implementation, the subtasks were not divided in such detail. My lesson is that dividing problems into simple, clean subtasks can reduce the difficulty of the overall problem.

-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer:
The hallways and rooms in Project 3 are analogous to hexagons. Filling rooms and hallways in an appropriate manner is analogous to the tessellation process.

-----

**If you were to start working on world generation, what kind of method would you think of writing first?
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer:
I would first consider how to generate rooms and hallways.

-----

**What distinguishes a hallway from a room? How are they similar?**

Answer:
They both have walls. Rooms can be much larger, while hallway width can only be 1 or 2.
