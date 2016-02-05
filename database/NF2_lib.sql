--Practical project Datenbanken und Objektorientierung
--Winterterm 2015/2016
--Authors: Benjamin Kindt und Daniel Dethloff
--Task: Implementierung einer NF² Bibliothek und einer Anwendung dazu
--Chemnitz, den 05.02.16

--SQL Libary for PostgreSQL 9.4

--get the primary Key from given table
CREATE OR REPLACE FUNCTION get_PrimaryKey(_t regclass)
	RETURNS name[] AS
$BODY$
DECLARE
	res name[];
	i name;
	j int := 1;
BEGIN
	--Loop over the primary keys for the table
	FOR i IN (SELECT a.attname
			FROM   pg_index i
			JOIN   pg_attribute a ON a.attrelid = i.indrelid
					     AND a.attnum = ANY(i.indkey)
			WHERE  i.indrelid = _t::regclass
			AND    i.indisprimary)
	LOOP
	--add them to an array
		res[j] = i;
		j = j +1;
	END LOOP;
	RETURN res;
END;
$BODY$ LANGUAGE plpgsql;
COMMENT ON FUNCTION get_PrimaryKey(regclass) IS 'Get the Primary key of a given table';

--get the values with are not effected by the nest/unnest operation
CREATE OR REPLACE FUNCTION get_notToNestedColumnNames(_t regclass,_c text)
	RETURNS text[] AS
$BODY$
DECLARE
	res text[];
	tmp text;
	i int := 1;
	prim_key name[]:= get_PrimaryKey(_t);
BEGIN
	FOR tmp IN (SELECT COLUMN_NAME
		FROM INFORMATION_SCHEMA.COLUMNS
		WHERE
		TABLE_NAME = _t::text AND NOT
		COLUMN_NAME = _c )
	LOOP
		--add them to an array, exept the primary key
		IF NOT ARRAY[tmp::name] <@ prim_key
		THEN
		res[i] = tmp;
		i := i +1;
		END IF;
	END LOOP;

	RETURN res;
END;
$BODY$ LANGUAGE plpgsql;
COMMENT ON FUNCTION get_notToNestedColumnNames(regclass,text) IS 'Helpfunction for nf2_nest to find out witch columns were not nested';


--get the type of a column from given table and columnname
CREATE OR REPLACE FUNCTION get_column_type(_t regclass,_c text)
	RETURNS text AS
$BODY$
DECLARE res text := (SELECT DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE
     TABLE_NAME = _t::text AND
     COLUMN_NAME = _c);
BEGIN
	RETURN res;
END;
$BODY$ LANGUAGE plpgsql;
COMMENT ON FUNCTION get_column_type(regclass,text) IS 'Get Type of column from given table and column';

--create a new table by coping a source table with its constraints
create or replace function create_table_like(source text, newtable text)
returns void language plpgsql
as $$
declare
    _query text;
begin
    execute
	--create the not table
        format(
            'create table %s (like %s including all)',
            newtable, source);
    for _query in
        select
        --copy the constraints
            format (
                'alter table %s add constraint %s %s',
                newtable,
                replace(conname, source, newtable),
                pg_get_constraintdef(oid))
        from pg_constraint
        where contype = 'f' and conrelid = source::regclass
    loop
        execute _query;
    end loop;
end $$;



--Insert a new Movie with title, year, array of actors, array of directors, array of genres
CREATE OR REPLACE FUNCTION insertMovie(title text,yearnumber integer,actor character varying[],director character varying[],genre character varying[])
RETURNS void AS
$$
--insert into the individual tables
INSERT INTO movie (mapper_id,title,year) VALUES ((SELECT max(mapper_id) FROM movie)+1,title,yearnumber);
INSERT INTO genre_nest_genre (id,mapper_id,genre) VALUES ((SELECT max(id) FROM genre_nest_genre)+1,(SELECT max(mapper_id) FROM movie),genre);
INSERT INTO actor_nest_actor (id,mapper_id,actor) VALUES ((SELECT max(id) FROM actor_nest_actor)+1,(SELECT max(mapper_id) FROM movie),actor);
INSERT INTO director_nest_director (id,mapper_id,director) VALUES ((SELECT max(id) FROM director_nest_director)+1,(SELECT max(mapper_id) FROM movie),director);
$$
LANGUAGE SQL;

--update the title, year, array of actors, array of directors and array of genres from given movie_id (=mapper_id)
CREATE OR REPLACE FUNCTION updateMovie(movie_id int,newtitle text,newyearnumber integer,newactor character varying[],newdirector character varying[],newgenre character varying[])
RETURNS void AS
$$
--update the individual tables
UPDATE movie SET title = newtitle,year=newyearnumber WHERE movie.mapper_id = movie_id;
UPDATE genre_nest_genre SET genre = newgenre WHERE mapper_id=movie_id;
UPDATE actor_nest_actor SET actor = newactor WHERE mapper_id = movie_id;
UPDATE director_nest_director SET director = newdirector WHERE mapper_id = movie_id;
$$
LANGUAGE SQL;

--implementation of the nest function. Inputs are the table name and the column to nest.
CREATE OR REPLACE FUNCTION nf2_nest(
    _t regclass,
    _c text)
  RETURNS text AS
$BODY$
DECLARE
	--create a new name for the nested table
	_nest text := quote_ident(_t::text || '_nest_' || _c);
	--get the column names which are not effected by the nesting operation
	notnest text[]:= get_notToNestedColumnNames(_t,_c);
	notnestval text[];
	notnestvalforquery text;
	i text;
	j text;
	tab text= _t::text;
	t text := array_to_string(notnest,',');
	tem text;
	resarr text[];
	nestarr text[];
	res text;
	k2 text;
	k text;
	lengthArr int := 1;
	length int := 1;
	coltype text := get_column_type(_t,_c);
	used text[][];

BEGIN
		--Drop the table if it is allready existing
		EXECUTE 'DROP TABLE IF EXISTS '|| _nest;
		--create a copy of the table which you want to nest
		EXECUTE 'SELECT create_table_like('''||tab||''','''||_nest||''')';
		--create a new column with a primary key inside from type bigserial to make insertation easy
		EXECUTE 'ALTER TABLE '|| _nest || ' DROP COLUMN '||array_to_string(get_PrimaryKey(_t),',');
		EXECUTE 'ALTER TABLE '|| _nest || ' ADD COLUMN id bigserial PRIMARY KEY';
		--create a column witch is a array of the type the source table has.
		EXECUTE 'ALTER TABLE '|| _nest || ' DROP COLUMN ' || _c;
		EXECUTE 'ALTER TABLE '|| _nest || ' ADD COLUMN ' || _c ||' '||get_column_type(_t,_c)||'[]';
		RAISE NOTICE 'CREATE NEW TABLE!';
	--iterate over all rows witch contains more than 1 same entry in the column to nest.
	FOR k IN EXECUTE('SELECT '|| notnest[1] ||' FROM '|| tab ||' GROUP BY '|| t ||' HAVING (COUNT( '|| notnest[1] ||' )>1)')
		LOOP
		length:=1;
		notnestval[1] = quote_literal(k);
		notnestvalforquery = notnest[1]||'='||quote_literal(k);
		--get the other values from column for the nested values (with are the same per column)
		FOR length IN 2..array_length(notnest,1)
		LOOP
			EXECUTE ('SELECT '|| notnest[length] ||' FROM '|| tab ||' WHERE '||notnestvalforquery||' GROUP BY '|| t ||' HAVING (COUNT( '|| notnest[length] ||' )>1) ') INTO tem;
			notnestval[length] = quote_literal(tem);
			notnestvalforquery := notnestvalforquery || ' AND '||notnest[length]||'='||notnestval[length];
			tem:='';
		END LOOP;
		--get the values to nest
		FOR j in EXECUTE 'SELECT '|| _c ||' FROM '|| tab ||' WHERE '|| notnestvalforquery
		LOOP
			nestarr := nestarr || (quote_literal(j));
		END LOOP;
		--build an array with the values witch are not effected by the nesting
		FOR lengthArr IN 1..array_length(notnest,1)
		LOOP
			EXECUTE ('SELECT '||notnest[lengthArr]||' FROM '||tab||' WHERE '||_c||'='||nestarr[1]||' AND '||notnestvalforquery) INTO i;
			i := quote_literal(i);
			IF (lengthArr = 1)
			THEN
				res := i;
			ELSE
				res := res || ','||i;
			END IF;
		END LOOP;
		--insert the values and the array into the new table
		EXECUTE 'INSERT INTO ' || _nest || '('||t||','||_c||') VALUES ('|| res ||',(ARRAY['|| array_to_string(nestarr,',') ||']::'||coltype||'[]))';
		nestarr := array[]::text[];
		j := '';
		res := '';
		notnestvalforquery := '';
		notnestval := array[]::text[];
	END LOOP;
	k := '';
	--iterate over all rows witch contains  1 same entry in the column to nest.
	FOR k IN EXECUTE 'SELECT '|| notnest[1] ||' FROM '|| tab ||' GROUP BY '|| t ||' HAVING (COUNT( '|| notnest[1] ||' )=1)'
	LOOP

		notnestval[1] = quote_literal(k);
		notnestvalforquery = notnest[1]||'='||quote_literal(k);

		length := 1;
		lengthArr :=1;
		--select the values for from the column witch are not effected by the nesting.
		FOR length IN 2..array_length(notnest,1)
		LOOP

			EXECUTE ('SELECT '|| notnest[length] ||' FROM '|| tab ||' WHERE '||notnestvalforquery||' GROUP BY '|| t ||' HAVING (COUNT( '|| notnest[length] ||' )=1) ') INTO tem;
			notnestval[length] = quote_literal(tem);
			notnestvalforquery := notnestvalforquery || ' AND '||notnest[length]||'='||notnestval[length];

		END LOOP;
		--get the one value to nest
		EXECUTE 'SELECT '||_c||' FROM '||tab||' WHERE ( '||notnestvalforquery||')' INTO k2;
		k2 := quote_literal(k2);
		--insert everything into the resulting table
		EXECUTE 'INSERT INTO '|| _nest ||'('||t||','||_c||') VALUES ('||array_to_string(notnestval,',')||',(ARRAY['||k2||']::'||coltype||'[]))';
		k2 := '';
		notnestvalforquery := '';
		notnestval := array[]::text[];
		k := '';
	END LOOP;
	RETURN _nest;
END;
$BODY$
  LANGUAGE plpgsql;
COMMENT ON FUNCTION nf2_nest(regclass, text) IS 'Nesting function to nest all data in the given column for NF²';

CREATE OR REPLACE FUNCTION nf2_unnest(_t regclass, _c text)
	--return the name of the new table
	RETURNS text AS
$BODY$
DECLARE
	--create a new tablename by adding "_unnest_" and the column to unnest.
	unnest text := quote_ident(_t::text || '_unnest_'|| _c);
	--call helpfunction to get all columns which are not effected by the unnesting
	notnest text[] := get_notToNestedColumnNames(_t,_c);
	t text := array_to_string(notnest,',');
	tab text = _t::text;
	nest text[];
	arr text[];
	otherval text[];
	tmp text;
	i int;
	j int;
	k int;
	strarr text;
	typeOfArr text;
	used text[] := array[]::text[];
BEGIN
	--get the type of the nestarray
	EXECUTE 'SELECT pg_typeof('||_c||') AS t FROM ( SELECT '||_c||' FROM '||tab||' LIMIT 1)AS x' INTO typeOfArr;
	typeOfArr := trim(both '[]' from typeOfArr);
	RAISE NOTICE 'Type of arr %',typeOfArr;
	IF NOT EXISTS(SELECT * FROM pg_class WHERE relname=unnest)
	THEN
		--create a new table by copied the nested one
		RAISE NOTICE 'CREATE NEW TABLE!';
		EXECUTE 'SELECT create_table_like('''||tab||''','''||unnest||''')';
		--create a new primary key column with type bigserial so its easier to insert new data
		EXECUTE 'ALTER TABLE '|| unnest || ' DROP COLUMN '||array_to_string(get_PrimaryKey(_t),',');
		EXECUTE 'ALTER TABLE '|| unnest || ' ADD COLUMN id bigserial PRIMARY KEY';
		--change the nested column's type to a basic type
		EXECUTE 'ALTER TABLE '|| unnest || ' DROP COLUMN ' || _c;
		EXECUTE 'ALTER TABLE '|| unnest || ' ADD COLUMN ' || _c ||' '||typeOfArr;
	END IF;
	--iterate throw all rows and get the array
	FOR arr IN EXECUTE('SELECT '||_c||' FROM '||tab)
	LOOP
		--escape special characters
		FOR k IN 1..array_length(arr,1)
		LOOP
			arr[k] := quote_literal(arr[k]);
		END LOOP;
		strarr := array_to_string(arr,',');
		--get the values from the columns witch are not nested
		FOR j IN 1..array_length(notnest,1)
		LOOP
			EXECUTE 'SELECT '||notnest[j]||' FROM '||tab||' WHERE NOT ('||notnest[j]||'= ANY (ARRAY['||array_to_string(used,',')||']::'||get_column_type(unnest,notnest[j])||'[])) AND '|| _c||'=(ARRAY['||strarr||']::'||get_column_type(unnest,_c)||'[])' INTO tmp;
			tmp := quote_literal(tmp);
			otherval[j] := tmp;

			used := used || tmp;
		END LOOP;
		--insert the unnested data into the new table
		FOR i IN 1..array_length(arr,1)
		LOOP
			tmp := arr[i];
			EXECUTE 'INSERT INTO '||unnest||' ('||t||','||_c||') VALUES ('||array_to_string(otherval,',')||','||tmp||')';
		END LOOP;

		otherval := array[]::text[];
		strarr := '';
		tmp := '';
	END LOOP;
	RETURN unnest;
END;
$BODY$ LANGUAGE plpgsql;
COMMENT ON FUNCTION nf2_unnest(regclass,text) IS 'Unnest function to unnest all data in the given column to undo NF² nesting';

--function witch is called on application start
CREATE OR REPLACE FUNCTION unnest_func()
	RETURNS void AS
	$BODY$
	BEGIN
		--Unnest all nested tables
		PERFORM nf2_unnest('actor_nest_actor','actor');
		PERFORM nf2_unnest('director_nest_director','director');
		PERFORM nf2_unnest('genre_nest_genre','genre');
	END;
	$BODY$ LANGUAGE plpgsql;
COMMENT ON FUNCTION unnest_func() IS 'This function is called on application start and unnests all nested tables once.';

--check if first array is subset of the second array
CREATE OR REPLACE FUNCTION nf2_subset(anyarray,anyarray)
	RETURNS boolean AS
$BODY$
DECLARE
	res boolean := TRUE;
	sizea int := array_length($1,1);
BEGIN
	IF NOT sizea = 0
	THEN
		FOR i IN 1..sizea
		LOOP
			--return false if the i'th element is not containt by the second array
			IF not(ARRAY[$1[i]] <@ $2)
			THEN
				res := FALSE;
			END IF;
		END LOOP;
	END IF;
	RETURN res;
END;
$BODY$
	LANGUAGE plpgsql;
COMMENT ON FUNCTION nf2_subset(anyarray,anyarray) IS 'Implementation auf << operator for NF2';

--Operator for subset operation
DROP OPERATOR IF EXISTS << (anyarray,anyarray);
CREATE OPERATOR << (
	LEFTARG = anyarray,
	RIGHTARG = anyarray,
	PROCEDURE = nf2_subset,
	COMMUTATOR = <<
);
COMMENT ON OPERATOR << (anyarray,anyarray) IS 'Compare two arrays and return true if the first array is subset of the second for NF2';

--check if the firstarray is propersubset of the second one
CREATE OR REPLACE FUNCTION nf2_propersubset(anyarray,anyarray)
	RETURNS boolean AS
$BODY$
DECLARE
	res boolean := TRUE;
	sizea int := array_length($1,1);
	sizeb int := array_length($2,1);
BEGIN
	IF not(nf2_subset($1,$2) = TRUE) OR (sizeb <= sizea)
	THEN
		--if the firstarray is not a subset of the second its not a propersubset and its also no propersubset if the size of the firstarray is smaler or equal the second one.
		res := FALSE;
	END IF;
	RETURN res;
END;
$BODY$ LANGUAGE plpgsql;
COMMENT ON FUNCTION nf2_propersubset(anyarray,anyarray) IS 'Implementation auf <<= operator for NF2';

--operator for the poropersubset operation
DROP OPERATOR IF EXISTS <<= (anyarray,anyarray);
CREATE OPERATOR <<= (
	LEFTARG = anyarray,
	RIGHTARG = anyarray,
	PROCEDURE = nf2_propersubset,
	COMMUTATOR = <<=
);
COMMENT ON OPERATOR <<= (anyarray,anyarray) IS 'Compare two arrays and return true if the first array is proper subset of the second for NF2';

--Compare two arrays and give back an array witch not contains the elements of the first array, but the elements of the second.
CREATE OR REPLACE FUNCTION nf2_difference(anyarray,anyarray)
	RETURNS anyarray AS
$$
	--unnest the arrays and execpt select the second
	SELECT ARRAY(select unnest($1) except select unnest($2));
$$ LANGUAGE SQL;
COMMENT ON FUNCTION nf2_difference(anyarray,anyarray) IS 'Implementation of / operator for NF2';

--Operator for the difference operation
DROP OPERATOR IF EXISTS / (anyarray,anyarray);
CREATE OPERATOR / (
	LEFTARG = anyarray,
	RIGHTARG = anyarray,
	PROCEDURE = nf2_difference,
	COMMUTATOR = /
);
COMMENT ON OPERATOR / (anyarray,anyarray) IS 'Compare two arrays and return an array which is the the first one without item of the second array for NF2';

--prove if an set is equal to another
CREATE OR REPLACE FUNCTION nf2_equal(anyarray,anyarray)
  RETURNS boolean AS
$BODY$
DECLARE
	res boolean := TRUE;
	sizea int := array_length($1, 1);
	sizeb int := array_length($2, 1);
BEGIN
	IF sizea = sizeb
	THEN
		--check if every element of the first array is contain in the second.
		FOR i IN 1..sizea
		LOOP
			IF not(ARRAY[$1[i]] <@ $2)
			THEN
				res := FALSE;
			END IF;
		END LOOP;
	ELSE
		res := FALSE;
	END IF;
	RETURN res;
END;
$BODY$
  LANGUAGE plpgsql;
COMMENT ON FUNCTION nf2_equal(anyarray,anyarray) IS 'Implementation auf == operator for NF2';


--operator for the equal operation of two sets.
DROP OPERATOR IF EXISTS == (anyarray,anyarray);
CREATE OPERATOR == (
	LEFTARG = anyarray,
	RIGHTARG = anyarray,
	PROCEDURE = nf2_equal,
	COMMUTATOR = ==
);
COMMENT ON OPERATOR == (anyarray,anyarray) IS 'Compare two arrays and return true if they are equal for NF2';

--function to create the intersection of two given arrays
CREATE OR REPLACE FUNCTION nf2_intersection(anyarray,anyarray)
	RETURNS anyarray AS
$$
	--select to unnested arrays and check if which elements are the same.
	SELECT ARRAY(SELECT * FROM UNNEST($1) WHERE UNNEST = ANY($2));
$$ LANGUAGE SQL;
COMMENT ON FUNCTION nf2_intersection(anyarray,anyarray) IS 'Implementation auf the & operator for NF2';

--operator for the inersection operation
DROP OPERATOR IF EXISTS & (anyarray,anyarray);
CREATE OPERATOR & (
	LEFTARG = anyarray,
	RIGHTARG = anyarray,
	PROCEDURE = nf2_intersection,
	COMMUTATOR = &
);
COMMENT ON OPERATOR & (anyarray,anyarray) IS 'Compare two arrays and return an array which is the intersection auf those two arrays for NF2';

--function to check if two given arrays ar not the same set
CREATE OR REPLACE FUNCTION nf2_notequal(anyarray,anyarray)
  RETURNS boolean AS
$BODY$
DECLARE
	res boolean := FALSE;
	sizea int := array_length($1, 1);
	sizeb int := array_length($2, 1);
BEGIN
	--if the sizes are the same we have to look forward for differences between the arrays
	IF sizea = sizeb
	THEN
		--look for if any element of the first array is not containd in the second array
		FOR i IN 1..sizea
		LOOP
			IF not(ARRAY[$1[i]] <@ $2)
			THEN
				res := TRUE;
			END IF;
		END LOOP;
	ELSE
		res := TRUE;
	END IF;
	RETURN res;
END;
$BODY$
  LANGUAGE plpgsql;
COMMENT ON FUNCTION nf2_notequal(anyarray,anyarray) IS 'Implementation auf !== operator for NF2';

--operator for the check if two sets are not equal
DROP OPERATOR IF EXISTS !== (anyarray,anyarray);
CREATE OPERATOR !== (
	LEFTARG = anyarray,
	RIGHTARG = anyarray,
	PROCEDURE = nf2_notequal,
	COMMUTATOR = !==
);
COMMENT ON OPERATOR !== (anyarray,anyarray) IS 'Compare two arrays and return true if they are NOT equal for NF2';

--build a union array from to given ones.
CREATE OR REPLACE FUNCTION nf2_union(anyarray,anyarray)
	RETURNS anyarray AS
$BODY$
BEGIN
	RETURN nf2_array_sort_unique(array_cat($1, $2));
END;
$BODY$ LANGUAGE plpgsql;
COMMENT ON FUNCTION nf2_union(anyarray,anyarray) IS 'Implementation of | operator for NF2';

--delete dublicate entries from an given array
CREATE OR REPLACE FUNCTION nf2_array_sort_unique (anyarray)
	RETURNS anyarray
AS $body$
  SELECT ARRAY(
    SELECT DISTINCT $1[s.i]
    FROM generate_series(array_lower($1,1), array_upper($1,1)) AS s(i)
  );
$body$
LANGUAGE SQL;
COMMENT ON FUNCTION nf2_array_sort_unique(anyarray) IS 'Helpfunction to delete dublicates in an array';

--operator for the union operation of two sets
DROP OPERATOR IF EXISTS | (anyarray,anyarray);
CREATE OPERATOR | (
	LEFTARG = anyarray,
	RIGHTARG = anyarray,
	PROCEDURE = nf2_union,
	COMMUTATOR = |
);
COMMENT ON OPERATOR | (anyarray,anyarray) IS 'Compare two arrays and return an array which is the union auf those two arrays for NF2';
