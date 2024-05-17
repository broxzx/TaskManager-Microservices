create table users
(
    id             BIGSERIAL PRIMARY KEY,
    username       varchar(64) unique  not null,
    email          varchar(128) unique not null,
    email_verified boolean             not null,
    is_deleted     boolean             not null
);

create table user_roles
(
    user_id BIGINT,
    role    varchar(64),
    primary key (user_id, role),
    foreign key (user_id) references users (id) on delete cascade
);