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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swati on 11/4/17.
 */

public class ShoppingCartAdapter extends ArrayAdapter {
    Context context;
    ArrayList<ShoppingItem> items;

    public ShoppingCartAdapter(Context context, List<ShoppingItem> items){
        super(context, 0, items);
        this.context = context;
        this.items = (ArrayList<ShoppingItem>) items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.cart_item, parent, false
            );
        }

        ShoppingItem currentItem = (ShoppingItem) getItem(position);

        final ImageView img = (ImageView) listItemView.findViewById(R.id.cartItemIcon);

        /*Picasso.with(getContext())
                .load(context.getApplicationContext().getString(R.string.ip)
                        + String.valueOf(currentItem.getProductID())
                        + ".jpg")
                .fit()
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

        ((TextView) listItemView.findViewById(R.id.cartItemName))
                .setText(currentItem.getTitle());

        String x = "x " + String.valueOf(currentItem.getQuantity());
        ((TextView) listItemView.findViewById(R.id.cartItemQuantity))
                .setText(x);

        int itemPrice=0;
        try{
            itemPrice = Integer.valueOf(NumberFormat.getCurrencyInstance()
                    .parse(String.valueOf(currentItem.getPrice()))
                    .toString());
        } catch (ParseException e){
            e.printStackTrace();
        }
        ((TextView) listItemView.findViewById(R.id.cartItemPrice))
                .setText(NumberFormat.getCurrencyInstance().format(itemPrice));

        ((TextView) listItemView.findViewById(R.id.cartItemTotal))
                .setText(NumberFormat.getCurrencyInstance().format(itemPrice * currentItem.getQuantity()));

        // No idea how to implement remove individual item from cart
        // Appreciated if anyone can fix it.
//        ImageView removeFromCart = (ImageView) listItemView.findViewById(R.id.removeFromCart);
//        removeFromCart.setImageResource(R.drawable.ic_clear_black_24dp);
//        removeFromCart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                items.remove()
//            }
//        });

        return listItemView;
    }
}
