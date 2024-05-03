/*
 * Copyright 2024 Benjamin Dittwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE TABLE movies
(
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    uuid    UUID                                    NOT NULL,
    version BIGINT,
    tmdb_id INTEGER                                 NOT NULL,
    CONSTRAINT pk_movies PRIMARY KEY (id)
);

CREATE TABLE title_locale_mapping
(
    locale_id    BIGINT       NOT NULL,
    title        VARCHAR(255) NOT NULL,
    title_locale VARCHAR(255) NOT NULL,
    CONSTRAINT pk_title_locale_mapping PRIMARY KEY (locale_id, title_locale)
);

ALTER TABLE movies
    ADD CONSTRAINT uc_movies_uuid UNIQUE (uuid);

ALTER TABLE title_locale_mapping
    ADD CONSTRAINT fk_title_locale_mapping_on_movie FOREIGN KEY (locale_id) REFERENCES movies (id);

CREATE TABLE description_locale_mapping
(
    locale_id          BIGINT       NOT NULL,
    description        VARCHAR(255) NOT NULL,
    description_locale VARCHAR(255) NOT NULL,
    CONSTRAINT pk_description_locale_mapping PRIMARY KEY (locale_id, description_locale)
);

CREATE TABLE movie_scenes
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    uuid     UUID                                    NOT NULL,
    lon      BIGINT                                  NOT NULL,
    lat      BIGINT                                  NOT NULL,
    version  BIGINT,
    movie_id BIGINT                                  NOT NULL,
    CONSTRAINT pk_movie_scenes PRIMARY KEY (id)
);

ALTER TABLE movie_scenes
    ADD CONSTRAINT uc_movie_scenes_uuid UNIQUE (uuid);

ALTER TABLE movie_scenes
    ADD CONSTRAINT FK_MOVIE_SCENES_ON_MOVIE FOREIGN KEY (movie_id) REFERENCES movies (id);

ALTER TABLE description_locale_mapping
    ADD CONSTRAINT fk_description_locale_mapping_on_movie_scene FOREIGN KEY (locale_id) REFERENCES movie_scenes (id);