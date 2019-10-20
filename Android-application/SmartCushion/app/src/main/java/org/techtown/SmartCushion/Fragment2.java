package org.techtown.SmartCushion;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class Fragment2 extends Fragment {
    Fragment2_1 fragment2_1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        return rootView;
    }
    private void initUI(ViewGroup rootView) {
        ImageButton hotView = rootView.findViewById(R.id.hotView);
        ImageButton privateView = rootView.findViewById(R.id.privateView);
        ImageButton likeView = rootView.findViewById(R.id.likeView);
        ImageButton listView = rootView.findViewById(R.id.listView);
        hotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView();
            }
        });
        privateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView();
            }
        });
        likeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView();
            }
        });
        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView();
            }
        });

    }
    private void videoView() {
        fragment2_1 = new Fragment2_1();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment2_1)
                .addToBackStack(null)
                .commit();
    }
}
