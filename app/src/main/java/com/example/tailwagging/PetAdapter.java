package com.example.tailwagging;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    private List<Pet> petList;
    private Context context;
    private OnPetListener onPetLongClickListener;
    private OnPetSimpleClickListener onPetSimpleClickListener;
    private boolean isMyPetsVersion = false;

    public interface OnPetListener {
        void onPetLongClick(Pet pet);
    }

    public interface OnPetSimpleClickListener {
        void onPetCardClicked(Pet pet);
    }

    public PetAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
        this.isMyPetsVersion = false;
    }

    public PetAdapter(Context context, List<Pet> petList, OnPetListener onPetLongClickListener) {
        this.context = context;
        this.petList = petList;
        this.onPetLongClickListener = onPetLongClickListener;
        this.isMyPetsVersion = true;
    }

    public void setOnPetSimpleClickListener(OnPetSimpleClickListener listener) {
        this.onPetSimpleClickListener = listener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isMyPetsVersion) {
            view = LayoutInflater.from(context).inflate(R.layout.item_pet_card, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_pet_horizontal, parent, false);
        }
        return new PetViewHolder(view, isMyPetsVersion);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.petName.setText(pet.getName());
        String imageUrl = pet.getImageUrl();

        if (isMyPetsVersion) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_cancel)
                        .into(holder.petImageCircle);
            } else {
                holder.petImageCircle.setImageResource(R.drawable.ic_profile);
            }

            holder.itemView.setOnLongClickListener(v -> {
                if (onPetLongClickListener != null) {
                    onPetLongClickListener.onPetLongClick(pet);
                    return true;
                }
                return false;
            });
        } else {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_pet_placeholder)
                        .error(R.drawable.ic_pet_placeholder)
                        .into(holder.petImageSquare);
            } else {
                holder.petImageSquare.setImageResource(R.drawable.ic_pet_placeholder);
            }
            if (onPetSimpleClickListener != null) {
                holder.itemView.setOnClickListener(v -> onPetSimpleClickListener.onPetCardClicked(pet));
            }
        }
    }

    @Override
    public int getItemCount() {
        return petList == null ? 0 : petList.size();
    }

    public void updatePets(List<Pet> newPetList) {
        Log.d("PetAdapter", "updatePets called. newPetList size: " + (newPetList != null ? newPetList.size() : 0));
        this.petList.clear();
        if (newPetList != null) {
            this.petList.addAll(newPetList);
        }
        Log.d("PetAdapter", "petList size after update: " + this.petList.size());
        notifyDataSetChanged();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView petImageSquare;
        TextView petName;
        CircleImageView petImageCircle;

        public PetViewHolder(@NonNull View itemView, boolean isMyPetsHolder) {
            super(itemView);
            if (isMyPetsHolder) {
                petImageCircle = itemView.findViewById(R.id.imageViewPetCard);
                petName = itemView.findViewById(R.id.textViewPetNameCard);
            } else {
                petImageSquare = itemView.findViewById(R.id.petImage);
                petName = itemView.findViewById(R.id.petName);
            }
        }
    }
}