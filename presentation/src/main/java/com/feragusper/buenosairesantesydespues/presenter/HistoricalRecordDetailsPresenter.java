package com.feragusper.buenosairesantesydespues.presenter;

import android.support.annotation.NonNull;

import com.feragusper.buenosairesantesydespues.di.PerActivity;
import com.feragusper.buenosairesantesydespues.domain.HistoricalRecord;
import com.feragusper.buenosairesantesydespues.domain.exception.DefaultErrorBundle;
import com.feragusper.buenosairesantesydespues.domain.exception.ErrorBundle;
import com.feragusper.buenosairesantesydespues.domain.interactor.DefaultSubscriber;
import com.feragusper.buenosairesantesydespues.domain.interactor.UseCase;
import com.feragusper.buenosairesantesydespues.exception.ErrorMessageFactory;
import com.feragusper.buenosairesantesydespues.mapper.HistoricalRecordModelDataMapper;
import com.feragusper.buenosairesantesydespues.model.HistoricalRecordModel;
import com.feragusper.buenosairesantesydespues.view.HistoricalRecordDetailsView;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Fernando.Perez
 * @since 0.1
 *
 * {@link Presenter} that controls communication between views and models of the presentation
 * layer.
 */
@PerActivity
public class HistoricalRecordDetailsPresenter implements Presenter {

    /** id used to retrieve user details */
    private String historicalRecordId;

    private HistoricalRecordDetailsView viewDetailsView;

    private final UseCase getUserDetailsUseCase;
    private final HistoricalRecordModelDataMapper historicalRecordModelDataMapper;
    private HistoricalRecord historicalRecord;

    @Inject
    public HistoricalRecordDetailsPresenter(@Named("historicalRecordDetails") UseCase getUserDetailsUseCase, HistoricalRecordModelDataMapper historicalRecordModelDataMapper) {
        this.getUserDetailsUseCase = getUserDetailsUseCase;
        this.historicalRecordModelDataMapper = historicalRecordModelDataMapper;
    }

    public void setView(@NonNull HistoricalRecordDetailsView view) {
        this.viewDetailsView = view;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        this.getUserDetailsUseCase.unsubscribe();
    }

    /**
     * Initializes the presenter by start retrieving user details.
     * @param historicalRecordId The id of the Historical Record
     */
    public void initialize(String historicalRecordId) {
        this.historicalRecordId = historicalRecordId;
        this.loadHistoricalRecordDetails();
    }

    /**
     * Loads user details.
     */
    private void loadHistoricalRecordDetails() {
        this.hideViewRetry();
        this.showViewLoading();
        this.getHistoricalRecordDetails();
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
        String errorMessage = ErrorMessageFactory.create(this.viewDetailsView.getContext(), errorBundle.getException());
        this.viewDetailsView.showError(errorMessage);
    }

    private void showUserDetailsInView(HistoricalRecord historicalRecord) {
        final HistoricalRecordModel historicalRecordModel = this.historicalRecordModelDataMapper.transform(historicalRecord);
        this.viewDetailsView.renderHistoricalRecord(historicalRecordModel);
    }

    private void getHistoricalRecordDetails() {
        if (historicalRecord == null) {
            getUserDetailsUseCase.execute(new HistoricalRecordDetailsSubscriber());
        } else {
            showUserDetailsInView(historicalRecord);
        }
    }

    public void onImageLoadSuccess() {
        hideViewLoading();
    }

    private final class HistoricalRecordDetailsSubscriber extends DefaultSubscriber<HistoricalRecord> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
            showViewRetry();
        }

        @Override
        public void onNext(HistoricalRecord historicalRecord) {
            HistoricalRecordDetailsPresenter.this.historicalRecord = historicalRecord;
            showUserDetailsInView(historicalRecord);
        }
    }
}
