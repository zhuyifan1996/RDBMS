SELECT * FROM Sailors S, Boats B, Reserves R
WHERE S.A = 1 AND S.A = B.E AND R.H = B.D
ORDER BY S.A;

SELECT * FROM Sailors S, Boats B
WHERE S.A = B.E
ORDER BY S.A;

SELECT * FROM Boats B, Reserves R
WHERE B.D = R.H
ORDER BY B.D;

SELECT * FROM Sailors Swagmuffins
WHERE Swagmuffins.A = 1
ORDER BY Swagmuffins.A;

SELECT * FROM Test Test2
WHERE Test.A > Test2.A
ORDER BY Test.A;
