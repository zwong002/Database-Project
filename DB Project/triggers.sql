CREATE SEQUENCE plane_id_seq START WITH 67;

--CREATE LANGUAGE plpgsql;

Create OR REPLACE FUNCTION plane_id_func()
Returns "trigger" AS
$BODY$ 
BEGIN 
	new.id := nextval('plane_id_seq');
	return NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER funcTrig
BEFORE INSERT 
ON Plane
FOR EACH ROW 
EXECUTE PROCEDURE plane_id_func();


CREATE SEQUENCE pilot_id_seq START WITH 250;

--CREATE LANGUAGE plpgsql;

Create OR REPLACE FUNCTION pilot_id_func()
Returns "trigger" AS
$BODY$ 
BEGIN 
	new.id := nextval('pilot_id_seq');
	return NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER pilotTrig
BEFORE INSERT 
ON Pilot
FOR EACH ROW 
EXECUTE PROCEDURE pilot_id_func();




CREATE SEQUENCE fiid_seq START WITH 2000;
CREATE SEQUENCE flight_id_seq START WITH 2000;
--CREATE LANGUAGE plpgsql;

Create OR REPLACE FUNCTION fiid_func()
Returns "trigger" AS
$BODY$ 
BEGIN 
	new.fiid := nextval('fiid_seq');
	new.flight_id :=nextval('flight_id_seq');
	return NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER fiidTrig
BEFORE INSERT 
ON FlightInfo
FOR EACH ROW 
EXECUTE PROCEDURE fiid_func();


CREATE SEQUENCE fnum_seq START WITH 2000;

--CREATE LANGUAGE plpgsql;

Create OR REPLACE FUNCTION fnum_func()
Returns "trigger" AS
$BODY$ 
BEGIN 
	new.fnum:= nextval('fnum_seq');
	return NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER fnumTrig
BEFORE INSERT 
ON Flight
FOR EACH ROW 
EXECUTE PROCEDURE fnum_func();



CREATE SEQUENCE tech_seq START WITH 250;

--CREATE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION tech_func()
Returns "trigger" AS
$BODY$
BEGIN
	new.id := nextval('tech_seq');
	return NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER techTrig
BEFORE INSERT 
ON Technician
FOR EACH ROW
EXECUTE PROCEDURE tech_func();

CREATE SEQUENCE res_seq START WITH 9999;


CREATE OR REPLACE FUNCTION res_func()
Returns "trigger" AS
$BODY$
BEGIN
	new.rnum := nextval('res_seq');
	return NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER resTrig
BEFORE INSERT 
ON Reservation
FOR EACH ROW
EXECUTE PROCEDURE res_func();

CREATE SEQUENCE cus_seq START WITH 250;


CREATE OR REPLACE FUNCTION cus_func()
Returns "trigger" AS
$BODY$
BEGIN
	new.id := nextval('cus_seq');
	return NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER cusTrig
BEFORE INSERT 
ON Customer
FOR EACH ROW
EXECUTE PROCEDURE cus_func();

