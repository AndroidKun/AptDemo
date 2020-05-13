package com.kennor.aptdemeo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kennor.annotations.BindView;
import com.kennor.annotations.Builder;

@Builder
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.view)
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnife.bind(this);
        textView.setText("11112222222222111111111111");
//        view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//        IShapeFactory.create("Circle").draw();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyButterKnife.unBind(this);
    }
}
