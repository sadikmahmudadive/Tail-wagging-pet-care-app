package com.example.tailwagging;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<Review> reviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUserName.setText(review.userName);
        holder.tvComment.setText(review.comment);
        holder.rbReview.setRating(review.rating);
        
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(review.timestamp);
        String date = DateFormat.format("dd MMM yyyy", cal).toString();
        holder.tvDate.setText(date);

        if (review.userPhotoUrl != null && !review.userPhotoUrl.isEmpty()) {
            Glide.with(context).load(review.userPhotoUrl).placeholder(R.drawable.ic_profile).into(holder.ivUser);
        } else {
            holder.ivUser.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivUser;
        TextView tvUserName, tvDate, tvComment;
        RatingBar rbReview;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUser = itemView.findViewById(R.id.ivReviewUser);
            tvUserName = itemView.findViewById(R.id.tvReviewUserName);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            rbReview = itemView.findViewById(R.id.rbReview);
        }
    }
}