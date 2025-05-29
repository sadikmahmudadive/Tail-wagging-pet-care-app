package com.example.tailwagging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView; // For item_pet_card

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    private List<Pet> petList;
    private Context context;
    private OnPetListener onPetListener; // For long click functionality
    private boolean isMyPetsVersion = false; // Flag to determine which layout and logic to use

    // --- Interface for click/long-click handling ---
    public interface OnPetListener {
        void onPetLongClick(Pet pet);
        // void onPetClick(Pet pet); // Add if you need regular click too
    }

    // --- Original Constructor (for item_pet_horizontal.xml) ---
    public PetAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
        this.isMyPetsVersion = false; // Uses original layout and ViewHolder logic
    }

    // --- New Constructor (for item_pet_card.xml and long click) ---
    public PetAdapter(Context context, List<Pet> petList, OnPetListener onPetListener) {
        this.context = context;
        this.petList = petList;
        this.onPetListener = onPetListener;
        this.isMyPetsVersion = true; // Uses new layout and ViewHolder logic
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isMyPetsVersion) {
            // Inflate for MyPetsActivity (item_pet_card.xml)
            view = LayoutInflater.from(context).inflate(R.layout.item_pet_card, parent, false);
        } else {
            // Inflate for original usage (item_pet_horizontal.xml)
            view = LayoutInflater.from(context).inflate(R.layout.item_pet_horizontal, parent, false);
        }
        return new PetViewHolder(view, isMyPetsVersion);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);

        // Common data binding
        holder.petName.setText(pet.getName());
        String imageUrl = pet.getImageUrl();

        if (isMyPetsVersion) {
            // --- Logic for MyPetsActivity (item_pet_card.xml) ---
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile) // Placeholder for CircleImageView
                        .error(R.drawable.ic_cancel)      // Error placeholder
                        .into(holder.petImageCircle); // Use CircleImageView
            } else {
                holder.petImageCircle.setImageResource(R.drawable.ic_profile);
            }

            holder.itemView.setOnLongClickListener(v -> {
                if (onPetListener != null) {
                    onPetListener.onPetLongClick(pet);
                    return true; // Consume the long click
                }
                return false;
            });
        } else {
            // --- Logic for original usage (item_pet_horizontal.xml) ---
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_pet_placeholder) // Original placeholder
                        .error(R.drawable.ic_pet_placeholder)     // Original error placeholder
                        .into(holder.petImageSquare); // Use regular ImageView
            } else {
                holder.petImageSquare.setImageResource(R.drawable.ic_pet_placeholder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public void updatePets(List<Pet> newPetList) {
        this.petList.clear();
        if (newPetList != null) {
            this.petList.addAll(newPetList);
        }
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    // Updated to handle both layouts by checking the isMyPetsVersion flag
    public static class PetViewHolder extends RecyclerView.ViewHolder {
        // Fields for item_pet_horizontal.xml (original)
        ImageView petImageSquare;
        TextView petName; // This TextView can be shared if IDs are the same or handled carefully

        // Fields for item_pet_card.xml (new)
        CircleImageView petImageCircle;
        // TextView petNameCard; // If item_pet_card has a different ID for name

        public PetViewHolder(@NonNull View itemView, boolean isMyPetsHolder) {
            super(itemView);
            if (isMyPetsHolder) {
                // Initialize views for item_pet_card.xml
                petImageCircle = itemView.findViewById(R.id.imageViewPetCard);
                petName = itemView.findViewById(R.id.textViewPetNameCard); // Assuming this is the ID in item_pet_card
            } else {
                // Initialize views for item_pet_horizontal.xml
                petImageSquare = itemView.findViewById(R.id.petImage); // ID from item_pet_horizontal
                petName = itemView.findViewById(R.id.petName);       // ID from item_pet_horizontal
            }
        }
    }
}