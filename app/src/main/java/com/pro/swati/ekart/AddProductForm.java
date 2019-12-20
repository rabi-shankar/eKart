package com.pro.swati.ekart;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.github.oliveiradev.lib.Rx2Photo;
import com.github.oliveiradev.lib.shared.TypeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class AddProductForm extends AppCompatActivity {

    TextView productid, title, type, description, price, quantity;
    Button imageBtn;
    ImageView image;

    DatabaseReference myref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_form);

        myref = FirebaseDatabase.getInstance().getReference("sellers/" +
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        productid = (TextView) findViewById(R.id.addProductId);
        title = (TextView) findViewById(R.id.addProductTitle);
        type = (TextView) findViewById(R.id.addProductType);
        description = (TextView) findViewById(R.id.addProductDescription);
        price = (TextView) findViewById(R.id.addProductPrice);
        quantity = (TextView) findViewById(R.id.addProductQuantity);
        imageBtn = (Button) findViewById(R.id.addimage);
        image = (ImageView) findViewById(R.id.imgeper);


        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker(v,image);
            }
        });

        findViewById(R.id.addProductSubmit).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (productid.getText().toString().matches("") ||
                        title.getText().toString().matches("") ||
                        type.getText().toString().matches("") ||
                        description.getText().toString().matches("") ||
                        price.getText().toString().matches("") ||
                        quantity.getText().toString().matches("")) {

                    Toast.makeText(getApplicationContext(), "Fill everything", Toast.LENGTH_SHORT).show();

                } else {



                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                             boolean flg = imageUploader(image,productid.getText().toString());

                        }
                    }, 0);



                    myref.addListenerForSingleValueEvent(new ValueEventListener() {
                        ArrayList<ShoppingItem> productList = new ArrayList<>();
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean islistempty = Boolean.valueOf(Objects.requireNonNull(dataSnapshot.child("isEmpty").getValue()).toString());
                            if (islistempty) {
                                myref.child("isEmpty").setValue(Boolean.FALSE.toString());
                            } else {
                                for (DataSnapshot snap : dataSnapshot.child("products").getChildren()) {
                                    int itemPrice = -1;
                                    try {
                                        itemPrice = Integer.valueOf(NumberFormat.getCurrencyInstance()
                                                .parse(String.valueOf(snap.child("price").getValue()))
                                                .toString());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    String productID = snap.child("productID").getValue().toString();

                                    productList.add(new ShoppingItem(
                                            productID,
                                            snap.child("title").getValue().toString(),
                                            snap.child("type").getValue().toString(),
                                            snap.child("description").getValue().toString(),
                                            itemPrice,
                                            Integer.valueOf(snap.child("quantity").getValue().toString())
                                    ));
                                }
                            }
//                            same product id can be added. note to future devs. remove that feature.
//                            and change the way ids are generated
                            productList.add(new ShoppingItem(
                                    productid.getText().toString(),
                                    title.getText().toString(),
                                    type.getText().toString(),
                                    description.getText().toString(),
                                    Integer.valueOf(price.getText().toString()),
                                    Integer.valueOf(quantity.getText().toString()))
                            );

                            Map<String, Object> cartItemsMap = new HashMap<>();
                            cartItemsMap.put("products", productList);
                            myref.updateChildren(cartItemsMap);
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("", "Failed to read value.", databaseError.toException());
                        }
                    });

                }
            }
        });
    }



    public boolean imagePicker(View view, final ImageView image){

        final boolean flg[] = {false};
        //imageBtn.setImageBitmap(null);

        Rx2Photo.with(view.getContext())
                .requestBitmap(TypeRequest.GALLERY, 300, 300)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {

                        image.setImageBitmap(bitmap);
                        flg[0]= true;

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        flg[0] = false;
                        Log.e("TAG", throwable.getMessage(), throwable);
                    }
                });

        return flg[0];
    }

    public boolean imageUploader(ImageView image,String id) {

        final boolean[] flg = {false};

        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();


        // Get reference to the file
        StorageReference imgRef = storageRef.child("productImage/productImageId"+id);

        //get image form ImageView
        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap bitmap = image.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] data = baos.toByteArray();


        // Update image
        UploadTask uploadTask = imgRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Log.e("test", "uploads Failure! ");
                flg[0] = false;

            }

        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                flg[0] = true;
                Log.e("test", "Successful uploads ");


            }
        });

        return flg[0];
    }

}
