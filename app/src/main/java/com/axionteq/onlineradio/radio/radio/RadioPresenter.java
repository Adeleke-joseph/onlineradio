package com.axionteq.onlineradio.radio.radio;


import com.axionteq.onlineradio.api.ApiClient;
import com.axionteq.onlineradio.api.ApiInterface;

public class RadioPresenter {
    private RadioView view;

    RadioPresenter(RadioView view){
        this.view = view;
    }

    void getData(){
        view.showLoading();

        ApiInterface apiInterface = ApiClient.getApiClient().create( ApiInterface.class );
    }
}
