create table gruppe_dto
(
    id serial primary key,
    gruppen_name varchar(20),
    geschlossen boolean,
    ausgabe_getaetigt boolean
);

create table ausgabe_dto
(
    id serial primary key,
    gruppe_dto int references gruppe_dto (id),
    gruppe_dto_key integer,
    kosten numeric
);

create table teilnehmer_dto
(
    id serial primary key,
    ausgabe_dto int references ausgabe_dto (id),
    ausgabe_dto_key int,
    name text
);

create table ausleger_dto
(
    id serial primary key,
    ausgabe_dto int references ausgabe_dto (id),
    name text
);

create table transaktion_dto
(
    id serial primary key,
    gruppe_dto int references gruppe_dto (id),
    gruppe_dto_key integer,
    netto_betrag numeric
);

create table zahler_dto
(
    id serial primary key,
    transaktion_dto int references transaktion_dto (id),
    name text

);

create table zahlungsempfaenger_dto
(
    id serial primary key,
    transaktion_dto int references transaktion_dto (id),
    name text
);


create table person_dto
(
    id serial primary key,
    gruppe_dto int references gruppe_dto (id),
    gruppe_dto_key int,
    name text

);


create table aktivitaet_dto
(
    ausgabe_dto int primary key references ausgabe_dto (id),
    name text
);