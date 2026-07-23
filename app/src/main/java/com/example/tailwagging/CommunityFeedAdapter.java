package com.example.tailwagging;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CommunityFeedAdapter extends RecyclerView.Adapter<CommunityFeedAdapter.ViewHolder> {

    private final List<FeedPost> posts;
    private final String currentUserId;
    private final DatabaseReference dbRef;

    public CommunityFeedAdapter(List<FeedPost> posts) {
        this.posts = posts;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
        this.dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("community_posts");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedPost post = posts.get(position);
        
        // Concatenate username and content for Instagram style
        String styledContent = post.userName + " " + post.content;
        holder.tvContent.setText(styledContent);
        holder.tvName.setText(post.userName);
        // Maybe use Spannable for bold username
        holder.tvType.setText(post.postType);
        
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(post.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        holder.tvTime.setText(timeAgo.toString().toUpperCase());

        Glide.with(holder.itemView.getContext())
                .load(post.userPhoto)
                .placeholder(R.drawable.ic_profile)
                .into(holder.ivUser);

        if (post.imageUrl != null && !post.imageUrl.isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.imageUrl)
                    .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }

        // Like Logic
        holder.tvLikes.setText(post.likesCount + " likes");
        boolean isLiked = post.likedBy != null && post.likedBy.containsKey(currentUserId);
        
        holder.ivLikeIcon.setImageResource(isLiked ? R.drawable.ic_heart_fill : R.drawable.ic_heart);
        holder.ivLikeIcon.setColorFilter(holder.itemView.getContext().getColor(isLiked ? R.color.md_theme_light_primary : R.color.black));

        holder.ivLikeIcon.setOnClickListener(v -> {
            UiUtils.animateClick(v);
            toggleLike(post);
        });

        // Double Tap to Like
        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isLiked) {
                    toggleLike(post);
                    animateHeart(holder.ivHeartOverlay);
                }
                return true;
            }
        });

        holder.itemView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        holder.btnComment.setOnClickListener(v -> {
            UiUtils.animateClick(v);
            if (holder.itemView.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                androidx.fragment.app.FragmentManager fm = ((androidx.fragment.app.FragmentActivity) holder.itemView.getContext()).getSupportFragmentManager();
                CommentsBottomSheetFragment.newInstance(post.postId).show(fm, "Comments");
            }
        });

        holder.btnShare.setOnClickListener(v -> {
            UiUtils.animateClick(v);
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = post.userName + " shared a " + post.postType + " on Tail Wagging: " + post.content;
            if (post.imageUrl != null && !post.imageUrl.isEmpty()) {
                shareMessage += "\n\nImage: " + post.imageUrl;
            }
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
            holder.itemView.getContext().startActivity(android.content.Intent.createChooser(shareIntent, "Share Post"));
        });
        
        holder.btnMore.setOnClickListener(v -> {
            UiUtils.animateClick(v);
            // More options logic (e.g., report, delete if owner)
            Toast.makeText(v.getContext(), "Options coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // Verified Badge Simulation (Based on userId or points if available in Post model)
        // For now, let's assume specific users are verified
        holder.ivVerified.setVisibility(View.GONE);
    }

    private void animateHeart(ImageView heart) {
        heart.setAlpha(0.9f);
        heart.setScaleX(0f);
        heart.setScaleY(0f);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(heart, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(heart, "scaleY", 0f, 1.2f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(400);
        set.setInterpolator(new OvershootInterpolator());
        
        set.start();
        heart.postDelayed(() -> heart.animate().alpha(0f).scaleX(0f).scaleY(0f).setDuration(300).start(), 600);
    }

    private void toggleLike(FeedPost post) {
        DatabaseReference postRef = dbRef.child(post.postId);
        if (post.likedBy != null && post.likedBy.containsKey(currentUserId)) {
            post.likedBy.remove(currentUserId);
            post.likesCount--;
        } else {
            if (post.likedBy == null) post.likedBy = new java.util.HashMap<>();
            post.likedBy.put(currentUserId, true);
            post.likesCount++;
        }
        postRef.setValue(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUser, ivPostImage, ivLikeIcon, ivHeartOverlay, ivVerified;
        TextView tvTime, tvType, tvContent, tvLikes, tvName;
        View btnComment, btnShare, btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUser = itemView.findViewById(R.id.ivPostUser);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivLikeIcon = itemView.findViewById(R.id.ivLikeIcon);
            ivHeartOverlay = itemView.findViewById(R.id.ivPostHeartOverlay);
            ivVerified = itemView.findViewById(R.id.ivVerifiedBadge);
            tvName = itemView.findViewById(R.id.tvPostUserName); // This might be null if you removed it, check layout
            tvTime = itemView.findViewById(R.id.tvPostTime);
            tvType = itemView.findViewById(R.id.tvPostTypeBadge);
            tvContent = itemView.findViewById(R.id.tvPostContent);
            tvLikes = itemView.findViewById(R.id.tvLikesCount);
            btnComment = itemView.findViewById(R.id.btnCommentPost);
            btnShare = itemView.findViewById(R.id.btnSharePost);
            btnMore = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}