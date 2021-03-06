SELECT * FROM Sailors;
SELECT Sailors.A FROM Sailors;
SELECT Boats.F, Boats.D FROM Boats;
SELECT Reserves.G, Reserves.H FROM Reserves;
SELECT * FROM Sailors WHERE Sailors.B >= Sailors.C;
SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C
SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C AND Sailors.B < Sailors.C;
SELECT * FROM Reserves, Boats ORDER BY Reserves.G;
SELECT * FROM Reserves, Boats WHERE Reserves.G = 4;
SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.A;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D AND Sailors.B < 150;
SELECT Sailors.C, Reserves.H FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D AND Sailors.B < 150;
SELECT * FROM TestTable3;
SELECT * FROM TestTable3, Boats WHERE TestTable3.N < Boats.D;
SELECT * FROM TestTable3, TestTable4;
SELECT * FROM Sailors S;
SELECT * FROM Sailors S WHERE S.A < 3;
SELECT S.A FROM Sailors S;
SELECT * FROM Sailors S, Reserves R WHERE S.A = R.G;
SELECT S.C, R.H FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D AND S.B < 150;
SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A ORDER BY S1.A;
SELECT * FROM Sailors S1, Sailors S2, Reserves R WHERE S1.A < S2.A AND S1.A = R.G ORDER BY S1.A;
SELECT S1.A, S2.A, S3.A FROM Sailors S1, Sailors S2, Sailors S3 WHERE S1.A < S2.A AND S2.A < S3.A AND S3.A < 5;
SELECT DISTINCT Reserves.G FROM Reserves;
SELECT DISTINCT R.G FROM Reserves R;
SELECT DISTINCT * FROM Sailors;
SELECT DISTINCT * FROM TestTable1;
SELECT * FROM TestTable2 WHERE TestTable2.K >= TestTable2.L AND TestTable2.L <= TestTable2.M; 
SELECT * FROM TestTable1 T1, TestTable2 T2 WHERE T1.J = T2.M ORDER BY T1.J;
SELECT * FROM TestTable2 T2A, TestTable2 T2B ORDER BY T2A.K;
SELECT TestTable2.M, TestTable2.L FROM TestTable2 ORDER BY TestTable2.L;
SELECT * FROM TestTable3 ORDER BY TestTable3.N;
SELECT * FROM Sailors ORDER BY Sailors.B;
SELECT Boats.F, Boats.D FROM Boats ORDER BY Boats.D;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C, Boats.F;
SELECT DISTINCT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C, Boats.F;
SELECT B.F, B.D FROM Boats B ORDER BY B.D;
SELECT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;
