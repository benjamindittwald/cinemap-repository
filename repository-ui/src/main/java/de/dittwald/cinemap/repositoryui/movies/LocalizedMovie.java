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

package de.dittwald.cinemap.repositoryui.movies;

import de.dittwald.cinemap.repositoryui.validation.Iso6391Constraint;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.net.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizedMovie implements Serializable {

    @Iso6391Constraint
    private String locale;

    @Size(max = 255)
    private String title;

    @Size(max = 5000)
    private String overview;

    @Size(max = 255)
    private String tagline;

    private URL posterUrl;
}
