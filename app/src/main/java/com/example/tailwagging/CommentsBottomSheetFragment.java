package com.example.tailwagging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentsBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_POST_ID = "post_id";
    private String postId;
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private DatabaseReference commentsRef;
    private EditText etComment;
    private ImageView ivCurrentUser;

    public static CommentsBottomSheetFragment newInstance(String postId) {
        CommentsBottomSheetFragment fragment = new CommentsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
        commentsRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/")
                .getReference("comments").child(postId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_comments_sheet, container, false);

        rvComments = view.findViewById(R.id.rvComments);
        etComment = view.findViewById(R.id.etComment);
        TextView btnPost = view.findViewById(R.id.btnPostComment);
        ivCurrentUser = view.findViewById(R.id.ivCurrentUserComment);

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvComments.setAdapter(adapter);

        loadCurrentUser();
        loadComments();

        btnPost.setOnClickListener(v -> postComment());

        return view;
    }

    private void loadCurrentUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/")
                .getReference("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String photo = snapshot.child("photoUrl").getValue(String.class);
                            if (isAdded()) {
                                Glide.with(CommentsBottomSheetFragment.this)
                                        .load(photo)
                                        .placeholder(R.drawable.ic_profile)
                                        .into(ivCurrentUser);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comment comment = ds.getValue(Comment.class);
                    commentList.add(comment);
                }
                adapter.notifyDataSetChanged();
                if (!commentList.isEmpty()) {
                    rvComments.smoothScrollToPosition(commentList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void postComment() {
        String text = etComment.getText().toString().trim();
        if (text.isEmpty()) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/")
                .getReference("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String photo = snapshot.child("photoUrl").getValue(String.class);

                        String commentId = commentsRef.push().getKey();
                        Comment comment = new Comment(commentId, uid, name, photo, text);
                        
                        if (commentId != null) {
                            commentsRef.child(commentId).setValue(comment)
                                    .addOnSuccessListener(aVoid -> {
                                        etComment.setText("");
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
