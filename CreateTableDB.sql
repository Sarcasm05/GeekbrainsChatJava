-- Table: public.Clients

-- DROP TABLE public."Clients";

CREATE TABLE public."Clients"
(
    "ClientId" integer NOT NULL DEFAULT nextval('id_autoincrement'::regclass),
    login character varying(255) COLLATE pg_catalog."default" NOT NULL,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
    nickname character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT "Clients_pkey" PRIMARY KEY ("ClientId")
)

TABLESPACE pg_default;

ALTER TABLE public."Clients"
    OWNER to postgres;