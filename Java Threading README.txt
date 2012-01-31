Chris Chang
chrchang@seas.upenn.edu
CIS 554
Scala Book Indexing README

This is a concurrent book indexer that utilizes Java threading and synchronization 
but is written in Scala. Each chapter is indexed with a thread. 

USAGE: Run the Scala file. First select the input dictionary - that is, the
dictionary of words that you want to index on. Next, select the directory of the 
chapter files of the book to index (note that only text files can exist in this directory). 
The program will output an index TXT file that has each word with a list of float values
_._, where the value to the left of the decimal is the chapter, and the value to the right of the 
decimal is the line number of occurance. 
