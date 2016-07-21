Tabular data extraction in PDFs
-------------------------------
### Example

For an example, see src/test/resources/images/10.1.1.106.5870-Table-4.png.
This is what this code will output: 

-----------------
data cells
-----------------

0.62 GB rowpath: List(UDP)colpath: List(business user, self-hosting)

4.23 GB rowpath: List(UDP)colpath: List(business user, third-party)

16.79 GB rowpath: List(TCP)colpath: List(business user, self-hosting)

22.74 GB rowpath: List(UDP)colpath: List(home user, self-hosting)

15.92 GB rowpath: List(TCP)colpath: List(business user, third-party)

6.24 GB rowpath: List(FC)colpath: List(business user, self-hosting)

35.15 GB rowpath: List(UDP)colpath: List(home user, third-party)

15.27 GB rowpath: List(TCP)colpath: List(home user, self-hosting)

3.75 GB rowpath: List(FC)colpath: List(business user, third-party)

49.91 GB rowpath: List(TCP)colpath: List(home user, third-party)

68.72 GB rowpath: List(FC)colpath: List(home user, self-hosting)

55.00 GB rowpath: List(FC)colpath: List(home user, third-party)

There are three types of cells in a "data" table: row header, column header and data. In a well formed table(WFT) each data cell is "indexable" by a row-header path and a column header path. That's how humans understand tables. Our example shows how each "data cell" in the table is converted into a triple of the form "row-header-path", "col-heder-path". The benifit is now you can pull up very specific data cells in response to a query. Also, you can convert a PDF table to a database table.

### Problem Description:

1. Identify the bounding box of the table and the words in the table on a PDF page. This code uses the tables
extracted by a software called pdffigures (https://github.com/allenai/pdffigures) that does really good on 
scholarly papers. This can be swapped later with Roman's code. 

2. Identify the table substructure: Given the outputs from the first step, create table "cells" with start rows and start columns. This is equivalent to converting a PDF table into an Excel table. Current code approaches the problem in two steps: a. Merge words inside a table to form cells, and b. Use a variant of single source longest path to identify the row and column numbers for the cells. The merging, if perfect, will always produce correct row and column numbers. This heuriistics should be replaced with linear chain CRF, working on that.

3. Classify the cells in row-header/column-header/data. For a WFT, this can be done in a complete unsupervised fashion. We first find a critical cell (the top-left corner of the data cells) and the startrow and startcol for that cell is the row index point and column index point. Any cell above the critical cell is a row-header and anything left to it is a col-header.  

4. Identify the row-paths and col-paths to the data cell.

Evidently, the "hardest" problem is problem 2. 

#### Ongoing and Future Work

1. Replacing the merging heuristic with a linear chain CRF (step 2 in problem description).

2. Enriching the RDFs with context (what is business user and self hosting?)

