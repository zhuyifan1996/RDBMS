1. Top Level Class of the Project:

/src/main/Main.java
  
Compiled jar file:
p3.jar

2. Method of handling partition reset in SMJ
We keep a head pointer indicating the first tuple of the inner relation in the current partition. If the current inner tuple is bigger than the current outer tuple, we call the reset(int) method on inner relation and proceed the outer relation. The implementation of reset(int) in sort operator uses the reset(index) method in BufferedReader, which calculates the pageIndex and tupleIndex based on page size and passed in argument, so no unbound state here.

4. File Structures
	/src 	# all the sources codes
	/tmp 	# contains all the intermediated files. Each intermediated files is named by its object hash (basically memory address)
			# thus it is important that the main program has the permissions to create folder under /tmp
			# i.e. 	all the temporary files created by a ExternalSortOperator with hash ABCD will be stored under /tmp/ABCD/
			# 		everything inside that folder will be governed by instance itself
	/logs	# contains all the logs. Each Log file is named by time/date
	/output # default output path
	/input	# default input path
	/test	# contain all the tests for the file
	/experiment #tests for final benchmark