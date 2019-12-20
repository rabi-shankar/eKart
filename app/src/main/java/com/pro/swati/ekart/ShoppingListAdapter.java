package com.pro.swati.ekart;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by swati on 2/3/19.
 */

public class ShoppingListAdapter extends ArrayAdapter<ShoppingItem> {

    Context context;

    public ShoppingListAdapter(Context context, List<ShoppingItem> items){
        super(context, 0, items);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false
            );
        }

        ShoppingItem currentItem = getItem(position);

        final ImageView img = (ImageView) listItemView.findViewById(R.id.itemIcon);

        /*Picasso.with(getContext())
                .load(context.getApplicationContext().getString(R.string.ip)
                        + String.valueOf(currentItem.getProductID())
                        + ".jpg")
                .fit().centerCrop()
                .into(img);*/


        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("productImage/productImageId"+currentItem.getProductID());

        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL

                Picasso.with(getContext())
                        .load(uri)
                        .fit()
                        .into(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e("firebase","fail to image download");
            }
        });

        TextView name = (TextView) listItemView.findViewById(R.id.itemName);
        name.setText(currentItem.getTitle());

        TextView description = (TextView) listItemView.findViewById(R.id.itemDescription);
        description.setText(currentItem.getDescription());

        TextView cost = (TextView) listItemView.findViewById(R.id.itemPrice);
        cost.setText(currentItem.getPrice());

        return listItemView;
    }
}