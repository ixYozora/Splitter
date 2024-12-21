insert into gruppe_dto(gruppen_name, geschlossen, ausgabe_getaetigt)
values ('Reisegruppe', false, true);

insert into ausgabe_dto(gruppe_dto, gruppe_dto_key, kosten)
values (1,0, 40);

insert into teilnehmer_dto(ausgabe_dto, ausgabe_dto_key, name)
values (1,0,'MaxHub');

insert into teilnehmer_dto(ausgabe_dto, ausgabe_dto_key, name)
values (1,1,'GitLisa');

insert into ausleger_dto(ausgabe_dto, name)
values (1, 'MaxHub');

insert into transaktion_dto(gruppe_dto, gruppe_dto_key, netto_betrag)
values (1,0,40);

insert into zahler_dto(transaktion_dto, name)
values (1, 'GitLisa');

insert into zahlungsempfaenger_dto(transaktion_dto, name)
values (1, 'MaxHub');

insert into person_dto(gruppe_dto, gruppe_dto_key, name)
values (1,0,'MaxHub');

insert into person_dto(gruppe_dto, gruppe_dto_key, name)
values (1,1,'GitLisa');

insert into aktivitaet_dto (ausgabe_dto, name)
values (1, 'Doener');







insert into gruppe_dto(id, gruppen_name, geschlossen, ausgabe_getaetigt)
values (2, 'DieGang', false, false);

insert into ausgabe_dto(gruppe_dto, gruppe_dto_key, kosten)
values (2,1, 100);

insert into teilnehmer_dto(ausgabe_dto, ausgabe_dto_key, name)
values (2,1,'Freddy');

insert into teilnehmer_dto(ausgabe_dto, ausgabe_dto_key, name)
values (2,2,'Otto');

insert into ausleger_dto(ausgabe_dto, name)
values (2, 'Otto');

insert into transaktion_dto(gruppe_dto, gruppe_dto_key, netto_betrag)
values (2,0,40);

insert into zahler_dto(transaktion_dto, name)
values (2, 'Freddy');

insert into zahlungsempfaenger_dto(transaktion_dto, name)
values (2, 'Otto');

insert into person_dto(gruppe_dto, gruppe_dto_key, name)
values (2,1,'Otto');

insert into person_dto(gruppe_dto, gruppe_dto_key, name)
values (2,2,'Freddy');

insert into aktivitaet_dto (ausgabe_dto, name)
values (2, 'Club');




