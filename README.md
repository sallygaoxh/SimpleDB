# SimpleDB
Coursework of eecs 339-Intro to Database

TupleDesc class: 
The TupleDesc class has an attributes: TDItemList.
TDItemList is an arraylist of TDItem, which stores the type and the name of each field.

Tuple class:
Each tuple object obeys a description, which is determined by its attribute td.
All the field of each tuple object are stored in its attribute fieldList.
Each tuple object can be identified uniquely with an attribute name recordId, contains the information of the pageId it belongs to and the tupleNo in this page.

Catalog class:
Each catalog object stores all the tables in the dababase. One database has one and only one catalog object. The tables in catalog are formed as DbFile, the names and primary keys for each table are also stored.

BufferPool class:
BufferPool is used to store pages for database processing. It protect the table from being read and written at the same time and resulting in incorrect data. 
The pages in BufferPool can belong to different tables. Thus, I have define an attribute named bufferList to store all the pages existing in the BufferPool.
In lab1, I finished the readPage() function of BufferPool class. First, compare the PageId of required page with all the pages in bufferList, if the page is already in bufferList, return it. If the page is not in the BufferPool, I will load it from the corresponding HeapFile, and store it in the BufferPool

HeapFile class:
HeapFile is a class implementing the DbFile interface. It links the table with the data on the disk. Each table in the database corresponds to one and only one HeapFile. HeapFile is a set of HeapPages.

HeapPage class:
A HeapPage implements the Page interface. It is the element of a HeapFile. 

HeapPageId class:
This class implements the PageId interface. It contains two int attributes: tableId and pageNo. The tableId identifies the table it belongs to. Using HeapPage.tableId, we can retrieve the information about the table. The pageNo identifies the order of this page in the whole table (HeapFile). It can be used as the offset when reading and writing pages to or from HeapFiles.
All the HeapPages in one HeapFile share the same tableId. The '-1' pageNo is a placeholder for when we create a table that does not have any pages yet.

RecordID class:
It is used to identify the specific tuple in one HeapFile, consisting of pageId and tupleno.

SeqScan class:
It is designed to be an OpIterator, which can go through each tuple in one table. Besides, it will rename the tuple descriptor, adding on the table name to the fieldname in the form of "tableName.fieldName".

-Predicate: Predicate compares specified fields of the tuples. The most important function is filter(), which returns true if the tuple meets the requirements.
-Filter: This operator only returns tuples that satisfy a Predicate (satisfy the filter condition) that is specified as part of its constructor. Hence, it filters out any tuples that do not match the predicate. fetchnext() is used to iterate all the eligible tuples.
-JoinPredicate: Similar to Predicate, while it compares specified fields from two different tuples.
-Join: This operator joins tuples from its two children according to a JoinPredicate that is passed in as part of its constructor. Only the tuple pairs that meet the requirements of JoinPredicate will be joined on specified field, and return one tuple.  fetchnext() is used to iterate all the eligible tuples.
-IntegerAggregator: If the aggregate field is integer, create IntegerAggregator. It can deal with COUNT, SUM, AVG, MIN, MAX on aggregate field. Each tuple in the result is a pair of the form (groupValue, aggregateValue). If the group by field is given as NO_GROUPING, result is a single tuple of the form (aggregateValue). COUNT, SUM, AVG, MIN, MAX are still available for NO_GROUPING situation.
-StringAggregator: Similar as IntegerAggregator while the aggregate field is String. It can only deal with COUNT since other operations are not suitable for String. 
-Aggregate: It is designed to perform aggregation of tables. It will create either IntegerAggregator or StringAggregator depending on the type of aggregate field. Invoking the mergeTupleIntoGroup() function of aggregators and return a iterator of all result tuples.
-AddTuples for HeapPage: If the Page is not full, write the data to the page and modify the bitmap of useage.
-AddTuples for HeapFile: If the HeapFile has one Page which is not full, add tuples to that page. Otherwises, new a Page in that File, use writePage() to write on disk.
-AddTuples for BufferPool: Add tuples to specified Files and mark all the modified pages as dirty. Need to get those pages into BufferPool first.
-Delete Tuple for HeapPage: Set the bitmap of that tuple to 0.
-Delete Tuple for HeapFile: Find the page in which the tuple is. Delete it through page.delete()	
-Delete Tuple for BufferPool: Get the corresponding file, and mark it as dirty. Delete it through file.delete()
-Eviction: The policy I use here is MRU (most recently used), that is, deleting the last page of the BufferPool if it is full.
