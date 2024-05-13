/*
 * Copyright 2024 Benjamin Dittwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dittwald.cinemap.repository.movie.dto;

import de.dittwald.cinemap.repository.validation.Iso6391Constraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

public record MovieFlatDto(
        @NotNull UUID uuid,
        @Min(value = -2147483648) // From TMDB API Reference movie Details
        @Max(value = 2147483647) // https://developer.themoviedb.org/reference/movie-details
        Integer tmdbId,
        @Min(value = 1700)
        Integer releaseYear,
        Map<Integer, @Size(min = 1, max = 50) String> genres,
        @Size(min = 1, max = 50)
        String imdbId,
        @Iso6391Constraint
        String locale,
        @Size(min = 1, max = 255)
        String title,
        @Size(min = 1, max = 5000)
        String overview,
        @Size(min = 1, max = 255)
        String tagline,
        URL posterUrl) {
}