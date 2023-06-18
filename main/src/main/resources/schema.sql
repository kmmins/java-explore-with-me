CREATE TABLE IF NOT EXISTS public.users
(
    id     bigint                 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    email  character varying(254) NOT NULL,
    "name" character varying(250) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS public.categories
(
    id     bigint                 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    "name" character varying(50) NOT NULL,
    CONSTRAINT categories_pkey PRIMARY KEY (id),
    CONSTRAINT categories_name_key UNIQUE ("name")
);

CREATE TABLE IF NOT EXISTS public.locations
(
    id  bigint  NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    lat numeric NOT NULL,
    lon numeric NOT NULL,
    CONSTRAINT locations_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.events
(
    id                bigint                 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    title             character varying(120) NOT NULL,
    description       character varying(7000) NOT NULL,
    annotation        character varying(2000) NOT NULL,
    event_date         timestamp without time zone,
    created_on         timestamp without time zone,
    published_on       timestamp without time zone,
    participant_limit  integer,
    request_moderation boolean,
    paid              boolean,
    state             character varying,
    category_id       bigint,
    initiator_id      bigint,
    location_id       bigint,
    CONSTRAINT events_pkey PRIMARY KEY (id),
    CONSTRAINT events_category_id_fkey FOREIGN KEY (category_id)
        REFERENCES public.categories (id),
    CONSTRAINT events_initiator_id_fkey FOREIGN KEY (initiator_id)
        REFERENCES public.users (id),
    CONSTRAINT events_location_id_fkey FOREIGN KEY (location_id)
        REFERENCES public.locations (id)
);

CREATE TABLE IF NOT EXISTS public.compilations
(
    id     bigint                 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    title  character varying(50) NOT NULL,
    pinned boolean,
    CONSTRAINT compilation_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.events_compilations
(
    event_id       bigint NOT NULL,
    compilation_id bigint NOT NULL,
    CONSTRAINT events_compilations_pkey PRIMARY KEY (event_id, compilation_id),
    CONSTRAINT events_compilations_compilation_id_fkey FOREIGN KEY (compilation_id)
        REFERENCES public.compilations (id),
    CONSTRAINT events_compilations_event_id_fkey FOREIGN KEY (event_id)
        REFERENCES public.events (id)
);

CREATE TABLE IF NOT EXISTS public.requests
(
    id        bigint NOT NULL,
    event_id   bigint NOT NULL,
    requester bigint NOT NULL,
    created   timestamp without time zone,
    status    character varying,
    CONSTRAINT requests_pkey PRIMARY KEY (id),
    CONSTRAINT requests_event_fkey FOREIGN KEY (event_id)
        REFERENCES public.events (id),
    CONSTRAINT requests_requester_fkey FOREIGN KEY (requester)
        REFERENCES public.users (id)
);

CREATE TABLE IF NOT EXISTS public.comments
(
    id      bigint                      NOT NULL,
    "text"    character varying(512)      NOT NULL,
    author_id  bigint                      NOT NULL,
    created timestamp without time zone NOT NULL,
    event_id   bigint                      NOT NULL,
    CONSTRAINT comments_pkey PRIMARY KEY (id),
    CONSTRAINT comments_autor_fkey FOREIGN KEY (author_id)
        REFERENCES public.users (id),
    CONSTRAINT comments_event_fkey FOREIGN KEY (event_id)
        REFERENCES public.events (id)
)
