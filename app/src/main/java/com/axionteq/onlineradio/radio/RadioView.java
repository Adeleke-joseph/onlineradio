package com.axionteq.onlineradio.radio;

import java.util.List;

public interface RadioView {

    void showLoading();
    void hideLoading();
    void onGetResult(List<Radio> list);
    void onErrorLoading(String message);
}
