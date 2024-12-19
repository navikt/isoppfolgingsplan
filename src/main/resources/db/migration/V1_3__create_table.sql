
CREATE TABLE FORESPORSEL
(
    id                             SERIAL PRIMARY KEY,
    uuid                           CHAR(36)    NOT NULL UNIQUE,
    created_at                     TIMESTAMPTZ NOT NULL,
    arbeidstaker_personident       VARCHAR(11) NOT NULL,
    veilederident                  VARCHAR(7)  NOT NULL,
    narmesteleder_personident      VARCHAR(11) NOT NULL,
    virksomhetsnummer              VARCHAR(9)  NOT NULL,
    published_at                   TIMESTAMPTZ
);

CREATE INDEX IX_FORESPORSEL_ARBEIDSTAKER_PERSONIDENT on FORESPORSEL (arbeidstaker_personident);
