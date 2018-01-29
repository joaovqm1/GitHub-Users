/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
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
package com.githubusers.presentation.features.movie;

import android.support.annotation.NonNull;

import com.githubusers.presentation.di.PerActivity;
import com.githubusers.presentation.exception.ErrorMessageFactory;
import com.githubusers.presentation.features.Presenter;
import com.sample.githubusers.domain.exception.DefaultErrorBundle;
import com.sample.githubusers.domain.exception.ErrorBundle;
import com.sample.githubusers.domain.features.DefaultObserver;
import com.sample.githubusers.domain.features.UseCase;
import com.sample.githubusers.domain.features.movie.GetMovie;
import com.sample.githubusers.domain.features.movie.GetMovieFromAPI;
import com.sample.githubusers.domain.features.movie.GetMovieFromLOD;
import com.sample.githubusers.domain.features.movie.GetMovieFromWebsite;
import com.sample.githubusers.domain.features.movie.Movie;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * {@link Presenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class MovieDetailsPresenter implements Presenter {

  private MovieDetailsView viewDetailsView;

  private final UseCase getMovieFromAPIUseCase;
  private final UseCase getMovieFromLODUseCase;
  private final UseCase getMovieFromWebUseCase;
  private final UseCase getMovieUseCase;
  private final MovieModelMapper modelMapper;

  @Inject
  MovieDetailsPresenter(@Named(GetMovieFromAPI.NAME) UseCase getMovieFromAPIUseCase,
                        @Named(GetMovieFromLOD.NAME) UseCase getMovieFromLODUseCase,
                        @Named(GetMovieFromWebsite.NAME) UseCase getMovieFromWebUseCase,
                        @Named(GetMovie.NAME) UseCase getMovieUseCase,
                        MovieModelMapper modelMapper) {
    this.getMovieFromAPIUseCase = getMovieFromAPIUseCase;
    this.getMovieFromLODUseCase = getMovieFromLODUseCase;
    this.getMovieFromWebUseCase = getMovieFromWebUseCase;
    this.getMovieUseCase = getMovieUseCase;
    this.modelMapper = modelMapper;
  }

  public void setView(@NonNull MovieDetailsView view) {
    this.viewDetailsView = view;
  }

  @Override
  public void resume() {}

  @Override
  public void pause() {}

  @Override
  public void destroy() {
    this.getMovieFromAPIUseCase.dispose();
    this.getMovieFromLODUseCase.dispose();
    this.viewDetailsView = null;
  }

  /**
   * Initializes the presenter by showing/hiding proper views
   * and retrieving movieFromAPI details.
   */
  public void initialize(String title, String button) {
    this.hideViewRetry();
    this.showViewLoading();

    switch (button) {
      case "API":
        this.getMovieFromAPI(title);
        break;
      case "LOD":
        this.getMovieFromLOD(title);
        break;
      case "WEB":
        this.getMovieFromWeb(title);
        break;
      case "ALL":
        this.getMovie(title);
        break;
    }
  }

  private void getMovieFromAPI(String title) {
    this.getMovieFromAPIUseCase.execute(new MovieObserver(), GetMovieFromAPI.Params.forMovie(title));
  }

  private void getMovieFromLOD(String title) {
    this.getMovieFromLODUseCase.execute(new MovieObserver(), GetMovieFromLOD.Params.forMovie(title));
  }

  private void getMovieFromWeb(String title) {
    this.getMovieFromWebUseCase.execute(new MovieObserver(), GetMovieFromWebsite.Params.forMovie(title));
  }

  private void getMovie(String title) {
    this.getMovieUseCase.execute(new MovieObserver(), GetMovie.Params.forMovie(title));
  }

  private void showViewLoading() {
    this.viewDetailsView.showLoading();
  }

  private void hideViewLoading() {
    this.viewDetailsView.hideLoading();
  }

  private void showViewRetry() {
    this.viewDetailsView.showRetry();
  }

  private void hideViewRetry() {
    this.viewDetailsView.hideRetry();
  }

  private void showErrorMessage(ErrorBundle errorBundle) {
    String errorMessage = ErrorMessageFactory.create(this.viewDetailsView.context(),
        errorBundle.getException());
    this.viewDetailsView.showError(errorMessage);
  }

  private void showMovieDetailsInView(Movie movie) {
    final MovieModel movieModel = this.modelMapper.transform(movie);
    this.viewDetailsView.onMovie(movieModel);
  }

  private final class MovieObserver extends DefaultObserver<Movie> {

    @Override
    public void onComplete() {
      MovieDetailsPresenter.this.hideViewLoading();
    }

    @Override
    public void onError(Throwable e) {
      MovieDetailsPresenter.this.hideViewLoading();
      MovieDetailsPresenter.this.showErrorMessage(new DefaultErrorBundle((Exception) e));
    }

    @Override
    public void onNext(Movie movie) {
      MovieDetailsPresenter.this.showMovieDetailsInView(movie);
    }
  }
}
