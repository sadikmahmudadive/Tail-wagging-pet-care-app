package com.example.tailwagging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailedReviewAdapter extends RecyclerView.Adapter<DetailedReviewAdapter.DetailedViewHolder> {

    private final Context context;
    private final List<Review> reviews;

    public DetailedReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public DetailedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_detailed, parent, false);
        return new DetailedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailedViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUserName.setText(review.userName);
        holder.tvComment.setText(review.comment);
        holder.tvRatingNum.setText(String.format(Locale.getDefault(), "%.1f", review.rating));
        holder.rbReview.setRating(review.rating);
        holder.tvTime.setText(getRelativeTime(review.timestamp));

        if (review.userPhotoUrl != null && !review.userPhotoUrl.isEmpty()) {
            Glide.with(context).load(review.userPhotoUrl).placeholder(R.drawable.ic_profile).into(holder.ivUser);
        } else {
            holder.ivUser.setImageResource(R.drawable.ic_profile);
        }
    }

    private String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + " minutes ago";
        if (diff < 86400000) return (diff / 3600000) + " hours ago";
        if (diff < 604800000) return (diff / 86400000) + " days ago";
        
        return android.text.format.DateFormat.format("dd MMM yyyy", timestamp).toString();
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class DetailedViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivUser;
        TextView tvUserName, tvTime, tvRatingNum, tvComment;
        RatingBar rbReview;

        public DetailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUser = itemView.findViewById(R.id.ivUserDetail);
            tvUserName = itemView.findViewById(R.id.tvUserNameDetail);
            tvTime = itemView.findViewById(R.id.tvReviewTime);
            tvRatingNum = itemView.findViewById(R.id.tvRatingNum);
            tvComment = itemView.findViewById(R.id.tvReviewCommentDetail);
            rbReview = itemView.findViewById(R.id.rbReviewItem);
        }
    }
}