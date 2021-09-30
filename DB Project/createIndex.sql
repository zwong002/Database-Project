CREATE INDEX C_customerid ON Customer USING hashing (id);

CREATE INDEX Pl_planeid ON Plane USING hashing (id);

CREATE INDEX R_repairid ON Repairs USING hashing (id);

CREATE INDEX F_fnum ON Flight USING hashing (fnum);

CREATE INDEX F_cost ON Flight USING hashing (cost);

CREATE INDEX F_numsold ON Flight USING hashing (num_sold);

CREATE INDEX P_pilotid ON Pilot USING hashing (id);

CREATE INDEX T_technicianid ON Technician USING hashing (id);

CREATE INDEX Res_rnum ON Reservation USING hashing (rnum);

CREATE INDEX Res_status ON Reservation USING hashing (status);

