package com.axionteq.onlineradio.radio.radio;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.axionteq.onlineradio.R;


public class CustomToast {

    private SparseIntArray statusCode = new SparseIntArray();
    private SparseIntArray statusColor = new SparseIntArray();
    private Context context;


    public CustomToast(Context context) {
        this.context = context;

        statusColor.append(0, R.drawable.btn_error_background);
        statusColor.append(1, R.drawable.btn_success_background);
        statusColor.append(2, R.drawable.btn_warning_background);
        statusColor.append(3, R.drawable.btn_info_background);

        statusCode.append(0, R.drawable.ic_error);
        statusCode.append(1, R.drawable.ic_check);
        statusCode.append(2, R.drawable.ic_warning);
        statusCode.append(3, R.drawable.ic_info);
    }

    void createToast(String message, int status) {
        Toast toast = new Toast(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        View view = inflater.inflate(R.layout.layout_custom_toast, null);
        LinearLayout layout = view.findViewById(R.id.custom_toast_ll);
        TextView tvMessage = view.findViewById(R.id.custom_toast_text);
        ImageView ivStatus = view.findViewById(R.id.custom_toast_iv);

        layout.setBackground(context.getDrawable(statusColor.get(status)));
        ivStatus.setImageResource(statusCode.get(status));
        tvMessage.setText(message);

        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
