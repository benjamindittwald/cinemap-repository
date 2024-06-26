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

package de.dittwald.cinemap.repository.movie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.dittwald.cinemap.repository.exceptions.LocaleNotFoundException;
import de.dittwald.cinemap.repository.exceptions.NotFoundException;
import de.dittwald.cinemap.repository.exceptions.TmdbReadException;
import de.dittwald.cinemap.repository.exceptions.UuidInUseException;
import de.dittwald.cinemap.repository.scene.repository.SceneRepository;
import de.dittwald.cinemap.repository.tmdb.TmdbClient;
import de.dittwald.cinemap.repository.util.DummyData;
import de.dittwald.cinemap.repository.movie.dto.MovieFlatDto;
import de.dittwald.cinemap.repository.movie.entity.Movie;
import de.dittwald.cinemap.repository.movie.repository.MovieRepository;
import de.dittwald.cinemap.repository.movie.util.LocalizedMovieDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@WebMvcTest({MovieService.class})
@AutoConfigureMockMvc
public class MovieServiceTest {

    @Autowired
    private MovieService movieService;

    @MockBean
    private MovieRepository movieRepository;

    @MockBean
    private SceneRepository sceneRepository;

    @MockBean
    private TmdbClient tmdbClient;

    private DummyData dummyData;

    @BeforeEach
    void setUp() throws URISyntaxException, MalformedURLException {
        this.dummyData = new DummyData();
    }

    @Test
    void shouldFindTwoMovies() throws LocaleNotFoundException {
        when(this.movieRepository.findAll()).thenReturn(List.of(this.dummyData.getNobody(), this.dummyData.getWolf()));
        List<MovieFlatDto> movieFlatDtos = this.movieService.findAll("en");
        assertThat(movieFlatDtos.size()).isEqualTo(2);
        verify(this.movieRepository, times(1)).findAll();
    }

    @Test
    void shouldFindTwoMoviesWhereFirstTitleIsNobodyEn() throws LocaleNotFoundException {
        when(this.movieRepository.findAll()).thenReturn(List.of(this.dummyData.getNobody(), this.dummyData.getWolf()));
        List<MovieFlatDto> movieFlatDtos = this.movieService.findAll("en");
        assertThat(movieFlatDtos.getFirst().title()).isEqualTo("My Name is Nobody - Title");
        assertThat(movieFlatDtos.getLast().title()).isEqualTo("Dances with Wolves - Title");

        verify(this.movieRepository, times(1)).findAll();
    }

    @Test
    void shouldSaveMovie() throws UuidInUseException {
        when(this.movieRepository.save(this.dummyData.getWolf())).thenReturn(this.dummyData.getWolf());
        this.movieService.save(this.dummyData.getWolfFlatEnDto());
        verify(this.movieRepository, times(1)).save(this.dummyData.getWolf());
    }

    @Test
    void shouldFailSaveMovieDueToUuidAlreadyExists() {
        when(this.movieRepository.existsByUuid(this.dummyData.getNobody().getUuid())).thenReturn(true);
        Exception exception = assertThrows(UuidInUseException.class,
                () -> this.movieService.save(LocalizedMovieDtoMapper.entityToDto(this.dummyData.getNobody(), "en")));
        assertThat(exception.getMessage()).isEqualTo("UUID already in use");
        verify(this.movieRepository, times(1)).existsByUuid(this.dummyData.getNobody().getUuid());
    }

    @Test
    void shouldUpdateMovie() throws NotFoundException, MalformedURLException, URISyntaxException {
        MovieFlatDto movieFlatDto = this.dummyData.getWolfFlatEnDto();
        Optional<Movie> movieOptional = Optional.of(this.dummyData.getWolf());
        Movie movie = this.dummyData.getWolf();

        when(this.movieRepository.findByUuid(movieFlatDto.uuid())).thenReturn(movieOptional);
        when(this.movieRepository.save(LocalizedMovieDtoMapper.dtoToEntity(movieFlatDto))).thenReturn(movie);

        this.movieService.update(movieFlatDto, movieFlatDto.uuid());

        verify(this.movieRepository, times(1)).findByUuid(movieFlatDto.uuid());
        verify(this.movieRepository, times(1)).save(movie);
    }

    @Test
    void shouldFailUpdateMovieDueToMovieDoesNotExist() {
        UUID notExistingMovieUuid = UUID.randomUUID();
        when(this.movieRepository.findByUuid(notExistingMovieUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class,
                () -> this.movieService.update(this.dummyData.getWolfFlatEnDto(), notExistingMovieUuid));
        assertThat(exception.getMessage()).isEqualTo("Movie not found");
        verify(this.movieRepository, times(1)).findByUuid(notExistingMovieUuid);
    }

    @Test
    void shouldDeleteMovieInclScenes() throws NotFoundException {
        when(this.movieRepository.existsByUuid(this.dummyData.getWolf().getUuid())).thenReturn(true);
        when(this.sceneRepository.findAllScenesOfMovieUuid(this.dummyData.getWolf().getUuid())).thenReturn(
                Optional.of(List.of(this.dummyData.getWolfSceneOne(), this.dummyData.getWolfSceneTwo())));
        this.movieService.deleteByUuid(this.dummyData.getWolf().getUuid());

        verify(this.movieRepository, times(1)).existsByUuid(this.dummyData.getWolf().getUuid());
        verify(this.sceneRepository, times(1)).findAllScenesOfMovieUuid(this.dummyData.getWolf().getUuid());
        // Todo: Find a better way in testing this for real
        verify(this.sceneRepository, times(1)).deleteAllById(any());
        verify(this.movieRepository, times(1)).deleteByUuid(this.dummyData.getWolf().getUuid());
    }

    @Test
    void shouldDeleteMovieWithOutScenes() throws NotFoundException {
        when(this.movieRepository.existsByUuid(this.dummyData.getWolf().getUuid())).thenReturn(true);
        when(this.sceneRepository.findAllScenesOfMovieUuid(this.dummyData.getWolf().getUuid())).thenReturn(
                Optional.empty());
        doNothing().when(this.movieRepository).deleteByUuid(this.dummyData.getWolf().getUuid());

        this.movieService.deleteByUuid(this.dummyData.getWolf().getUuid());

        verify(this.movieRepository, times(1)).existsByUuid(this.dummyData.getWolf().getUuid());
        verify(this.sceneRepository, times(1)).findAllScenesOfMovieUuid(this.dummyData.getWolf().getUuid());
        verify(this.movieRepository, times(1)).deleteByUuid(this.dummyData.getWolf().getUuid());
    }

    @Test
    void shouldFailDeleteMoveDueToMovieDoesNotExist() {
        UUID notExistingMovieUuid = UUID.randomUUID();
        when(this.movieRepository.existsByUuid(notExistingMovieUuid)).thenReturn(false);
        Exception exception =
                assertThrows(NotFoundException.class, () -> this.movieService.deleteByUuid(notExistingMovieUuid));
        assertThat(exception.getMessage()).isEqualTo("Movie not found");
        verify(this.movieRepository, times(1)).existsByUuid(notExistingMovieUuid);
    }

    @Test
    void shouldFindMovieByUuid() throws NotFoundException, LocaleNotFoundException {
        Optional<Movie> persistedMovie = Optional.of(this.dummyData.getWolf());
        when(this.movieRepository.findByUuid(persistedMovie.get().getUuid())).thenReturn(persistedMovie);
        assertThat(this.movieService.findByUuid(persistedMovie.get().getUuid(), "en").uuid()).isEqualTo(
                this.dummyData.getWolfFlatEnDto().uuid());
        verify(this.movieRepository, times(1)).findByUuid(persistedMovie.get().getUuid());
    }

    @Test
    void shouldFailFindMovieByUuidDueToMovieDoesNotExist() {
        UUID notExistingMovieUuid = UUID.randomUUID();
        when(this.movieRepository.findByUuid(notExistingMovieUuid)).thenReturn(Optional.empty());
        Exception exception =
                assertThrows(NotFoundException.class, () -> this.movieService.findByUuid(notExistingMovieUuid, "en"));
        assertThat(exception.getMessage()).isEqualTo("Movie not found");
        verify(this.movieRepository, times(1)).findByUuid(notExistingMovieUuid);
    }

    @Test
    void shouldDeleteAllMovies() {
        doNothing().when(this.sceneRepository).deleteAll();
        doNothing().when(this.movieRepository).deleteAll();
        this.movieService.deleteAll();
        verify(this.movieRepository, times(1)).deleteAll();
        verify(this.sceneRepository, times(1)).deleteAll();
    }

    @Test
    void shouldCreateMovieViaTmdbId()
            throws MalformedURLException, URISyntaxException, JsonProcessingException, TmdbReadException {
        when(this.tmdbClient.getMovieDetails(anyInt())).thenReturn(this.dummyData.getWolf());
        when(this.movieRepository.save(any())).thenReturn(this.dummyData.getWolf());

        this.movieService.createMovieViaTmdbId(5);

        verify(this.tmdbClient, times(1)).getMovieDetails(anyInt());
        verify(this.movieRepository, times(1)).save(any());
    }
}
