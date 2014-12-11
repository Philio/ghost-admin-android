package me.philio.ghostadmin.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghostadmin.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditorFragment extends Fragment {

    /**
     * Modes
     */
    public static final int MODE_VIEW = 0;
    public static final int MODE_EDIT = 1;

    /**
     * Arguments
     */
    private static final String ARG_POST_ID = "post_ID";
    private static final String ARG_MODE = "mode";

    /**
     * Post id
     */
    private long mPostId;

    /**
     * Default mode
     */
    private int mMode = MODE_VIEW;

    /**
     * Views
     */
    @InjectView(R.id.viewpager)
    ViewPager mViewPager;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param postId The id of the post to load
     * @return A new instance of fragment EditorFragment.
     */
    public static EditorFragment newInstance(long postId, int mode) {
        EditorFragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_POST_ID, postId);
        args.putInt(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mPostId = args.getLong(ARG_POST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_editor, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // If layout contains a ViewPager then init fragments within it
        if (mViewPager != null) {

        }
    }

}
