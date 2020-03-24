package com.axionteq.onlineradio.radio.radio;

import java.util.List;

public interface RadioView {

    void showLoading();
    void hideLoading();
    void onGetResult(List<RadioType> list);
    void onErrorLoading(String message);
}
