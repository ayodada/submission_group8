Prototype Web Search Engine in Java
===================================

This project builds an index from the provided WT web collection, searches the 20 topics in `topics.txt`, and writes the output in TREC format.

Files in this folder
--------------------
- `data\` source collection in WTxx\Bxx.GZ format
- `topics.txt` topic file
- `src\searchengine\` Java source code
- `build.bat` compile command
- `run-index.bat` build the index
- `run-search.bat` create the run file
- `run-web.bat` start the simple web interface

What the program does
---------------------
1. Decompresses each `.GZ` file.
2. Reads every `<DOC> ... </DOC>` block.
3. Extracts the `DOCNO`.
4. Removes the header and HTML tags.
5. Tokenizes the text, removes common stop words, and applies light stemming.
6. Builds an inverted index.
7. Uses BM25 to rank documents for each topic.
8. Writes the final results in TREC format.

Compile
-------
Install JDK 8 or newer and run:

build.bat

Compiled classes will be placed in:

out\classes

Build the index
---------------
run-index.bat data index

This will create an `index` folder with the saved index files.

Generate the results file
-------------------------
run-search.bat index topics.txt results.txt g1 1000

Change `g1` to your own group id if needed.

Start the web interface
-----------------------
run-web.bat index 8080 20

Then open:

http://localhost:8080

Manual commands
---------------
javac -encoding UTF-8 -d out\classes src\searchengine\*.java
java -cp out\classes searchengine.SearchEngineApp index data index
java -cp out\classes searchengine.SearchEngineApp run index topics.txt results.txt g1 1000
java -cp out\classes searchengine.SearchEngineApp serve index 8080 20

Example output
--------------
401 Q0 WT24-B28-147 1 6.714665567764736 g1
401 Q0 WT24-B20-169 2 6.710866977409565 g1

Notes
-----
- The current query parser uses the title and description fields.
- The ranking method is BM25.
- No external libraries are required.
- The web page is only for local searching and demo purposes.

