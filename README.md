# DataMiningProject
Employing the ROCAT algorithm on a dataset.
ROCAT Algorithm:
Relevant Overlapping Subspace Clusters on Categorical Data
Xiao He1, Jing Feng1, Bettina Konte1
Son T.Mai1, Claudia Plant2

Data set:
Battle, Juan, Antonio Jay Pastrana, and Jessie Daniels. 
Social Justice Sexuality Project: 2010 National Survey, including Puerto Rico. ICPSR34363-v1. Ann Arbor, MI: Inter-university Consortium for Political and Social Research [distributor], 
2013-08-09. http://doi.org/10.3886/ICPSR34363.v1

If you have a data set D in a localhost MySQL server, 
this application will find relevant overlapping subspace clusters of D when you
input the test attribute name NONE.

If you would like to try and use the application to classify objects - whether for classification or to 
check for correlations between attributes - split the data set into two MySQL table - 1 for which the 
subspace clusters will be found, and in the other, the application will give the attribute name you give it
to find the value distributions of each resulting cluster.
