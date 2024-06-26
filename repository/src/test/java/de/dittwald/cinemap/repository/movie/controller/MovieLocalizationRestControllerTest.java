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

package de.dittwald.cinemap.repository.movie.controller;

import de.dittwald.cinemap.repository.exceptions.NotFoundException;
import de.dittwald.cinemap.repository.util.DummyData;
import de.dittwald.cinemap.repository.movie.service.MovieLocalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieLocalizationRestController.class)
@AutoConfigureMockMvc
class MovieLocalizationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieLocalizationService movieLocalizationService;

    private DummyData dummyData;

    @BeforeEach
    void setUp() throws MalformedURLException, URISyntaxException {
        this.dummyData = new DummyData();
    }

    @Test
    void shouldFindAllMovieLocalizations() throws Exception {
        when(this.movieLocalizationService.getMovieLocalizations(
                this.dummyData.getWolfLocalizationDto().movieUuid())).thenReturn(
                this.dummyData.getWolfLocalizationDto());

        this.mockMvc.perform(get("/api/v1/movies/" + this.dummyData.getWolf().getUuid() + "/localizations"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.movieUuid", is(this.dummyData.getWolfLocalizationDto().movieUuid().toString())))
                .andExpect(jsonPath("$.localizations[0].locale", is("en")))
                .andExpect(jsonPath("$.localizations[0].title", is("Dances with Wolves - Title")))
                .andExpect(jsonPath("$.localizations[1].locale", is("de")))
                .andExpect(jsonPath("$.localizations[1].title", is("Der mit dem Wolf tanzt - Title")));

        verify(this.movieLocalizationService, times(1)).getMovieLocalizations(
                this.dummyData.getWolfLocalizationDto().movieUuid());
    }

    @Test
    void shouldFailFindAllMovieLocalizationsDueToMovieNotFound() throws Exception {
        when(this.movieLocalizationService.getMovieLocalizations(
                this.dummyData.getWolfLocalizationDto().movieUuid())).thenThrow(NotFoundException.class);

        this.mockMvc.perform(get("/api/v1/movies/" + this.dummyData.getWolf().getUuid() + "/localizations"))
                .andExpect(status().isNotFound());

        verify(this.movieLocalizationService, times(1)).getMovieLocalizations(
                this.dummyData.getWolfLocalizationDto().movieUuid());
    }

    @Test
    void shouldUpdateMovieNoOverride() throws Exception {
        doNothing().when(this.movieLocalizationService)
                .update(this.dummyData.getWolfLocalizationDto(),
                        this.dummyData.getWolfLocalizationDto().movieUuid(), false);

        this.mockMvc.perform(
                        put("/api/v1/movies/" + this.dummyData.getWolf().getUuid() + "/localizations").contentType(
                                        MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(this.dummyData.getValidWolfMovieLocalizationsJson))
                .andExpect(status().isNoContent());

        verify(this.movieLocalizationService, times(1)).update(this.dummyData.getWolfLocalizationDto(),
                this.dummyData.getWolfLocalizationDto().movieUuid(), false);
    }

    @Test
    void shouldUpdateMovieLocalizationsWithOverride() throws Exception {
        doNothing().when(this.movieLocalizationService)
                .update(this.dummyData.getWolfLocalizationDto(),
                        this.dummyData.getWolfLocalizationDto().movieUuid(), true);

        this.mockMvc.perform(put("/api/v1/movies/" + this.dummyData.getWolf().getUuid() +
                "/localizations?override=true").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(this.dummyData.getValidWolfMovieLocalizationsJson)).andExpect(status().isNoContent());

        verify(this.movieLocalizationService, times(1)).update(this.dummyData.getWolfLocalizationDto(),
                this.dummyData.getWolfLocalizationDto().movieUuid(), true);
    }

    @Test
    void shouldFailUpdateMovieLocalizationsNoOverrideDueToMovieNotFound() throws Exception {

        doThrow(NotFoundException.class).when(this.movieLocalizationService).update(this.dummyData.getWolfLocalizationDto(),
                this.dummyData.getWolfLocalizationDto().movieUuid(), false);

        this.mockMvc.perform(
                        put("/api/v1/movies/" + this.dummyData.getWolf().getUuid() + "/localizations").contentType(
                                        MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(this.dummyData.getValidWolfMovieLocalizationsJson))
                .andExpect(status().isNotFound());

        verify(this.movieLocalizationService, times(1)).update(this.dummyData.getWolfLocalizationDto(),
                this.dummyData.getWolfLocalizationDto().movieUuid(), false);
    }
}