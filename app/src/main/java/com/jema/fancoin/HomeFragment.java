package com.jema.fancoin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jema.fancoin.Adapter.PostAdapter;
import com.jema.fancoin.Model.PostCard;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button signOut;
    private RecyclerView tiktokFeed, entertainmentFeed, musicFeed;

    ArrayList<PostCard> postCardArrayList;
    PostAdapter postAdapter;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

//        signOut = (Button)rootView.findViewById(R.id.signOutBtn);
//
//        signOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//                Toast.makeText(getActivity(), "Logged Out!", Toast.LENGTH_SHORT).show();
//
//                Intent intent = new Intent(getActivity(), Login.class);
//                startActivity(intent);
//            }
//        });


        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching data...");
        progressDialog.show();


        tiktokFeed = (RecyclerView)rootView.findViewById(R.id.tiktokFeed);
        tiktokFeed.setHasFixedSize(true);
        tiktokFeed.setLayoutManager(new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL , false));

        entertainmentFeed = (RecyclerView)rootView.findViewById(R.id.entertainmentFeed);
        entertainmentFeed.setHasFixedSize(true);
        entertainmentFeed.setLayoutManager(new LinearLayoutManager(getContext()  , LinearLayoutManager.HORIZONTAL , false));

        musicFeed = (RecyclerView)rootView.findViewById(R.id.musicFeed);
        musicFeed.setHasFixedSize(true);
        musicFeed.setLayoutManager(new LinearLayoutManager(getContext()  , LinearLayoutManager.HORIZONTAL , false));



        db = FirebaseFirestore.getInstance();
        postCardArrayList = new ArrayList<PostCard>();
        postAdapter = new PostAdapter(getContext(),postCardArrayList);

        tiktokFeed.setAdapter(postAdapter);
        EventChangeListener();

        return rootView;
    }


    private void EventChangeListener() {

        db.collection("Users").whereEqualTo("category", "tiktok")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error != null) {
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                            Log.e("Firebase Error", error.getMessage());
                            return;
                        }
                        Log.i("Firebase Info", "success gettting data");

                        for(DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType() == DocumentChange.Type.ADDED){
                                postCardArrayList.add(dc.getDocument().toObject(PostCard.class));
                            }
                            postAdapter.notifyDataSetChanged();
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }
                });
    }
}