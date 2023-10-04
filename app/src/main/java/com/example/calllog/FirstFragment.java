package com.example.calllog;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.example.calllog.databinding.FragmentFirstBinding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private int i = 0;
    private final List<String> phoneNumbers = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();
        readCallLog(context);
    }

    @SuppressWarnings("all")
    private void build(Context context, Cursor cursor) {
        if (cursor != null && cursor.moveToNext()) {
            ConstraintLayout layout = binding.layout;

            ConstraintSet constraintSet = new ConstraintSet();

            CardView topCardView = getCardView(context, cursor);
            layout.addView(topCardView);
            constraintSet.clone(layout);

            constraintSet.connect(topCardView.getId(), ConstraintSet.TOP, layout.getId(),
                    ConstraintSet.TOP, 35);

            constraintSet.applyTo(layout);

            while (cursor.moveToNext()) {
                CardView cardView = getCardView(context, cursor);
                if (cardView == null)
                    continue;

                layout.addView(cardView);

                constraintSet.clone(layout);

                constraintSet.connect(cardView.getId(), ConstraintSet.TOP, topCardView.getId(),
                        ConstraintSet.BOTTOM, 35);

                constraintSet.applyTo(layout);

                topCardView = cardView;
            }
            cursor.close();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private CardView getCardView(Context context, Cursor cursor) {
        CardView cardView = new CardView(context);
        cardView.setId(++i);

        LinearLayout basic = new LinearLayout(context);
        basic.setOrientation(LinearLayout.HORIZONTAL);

        imageView(context, basic, cursor);

        LinearLayout rightInfo = getRightInfo(context, cursor);
        if (rightInfo == null)
            return null;

        basic.addView(rightInfo);

        cardView.addView(basic);
        cardView.setRadius(8);
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardView.setLayoutParams(cardLayoutParams);

        return cardView;
    }

    private void imageView(Context context, LinearLayout basic, Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
        long contactId = cursor.getLong(columnIndex);

        LinearLayout imageLinearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                130, 140
        );

        ImageView userImage = new ImageView(context);
        InputStream is = openPhoto(contactId, context);

        if (is != null)
            userImage.setImageBitmap(BitmapFactory.decodeStream(is));
        else
            userImage.setBackgroundResource(R.drawable.user);

        imageLinearLayout.addView(userImage);

        basic.addView(imageLinearLayout, imageLayoutParams);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private LinearLayout getRightInfo(Context context, Cursor cursor) {
        int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

        String number = cursor.getString(numberIndex);
        String name = cursor.getString(nameIndex);

        if (phoneNumbers.contains((number.length() == 9) ? "+998".concat(number) : number))
            return null;

        phoneNumbers.add(number);

        LinearLayout rightInfo = new LinearLayout(context);
        rightInfo.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams rightLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        rightInfo.setLayoutParams(rightLayoutParams);
        rightInfo.setPadding(30, 20, 30, 20);

        TextView nameText = getTextView(context, name != null ? name : number, 18);
        rightInfo.addView(nameText);

        bottomInfo(context, cursor, rightInfo);
        return rightInfo;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void bottomInfo(Context context, Cursor cursor, LinearLayout rightInfo) {
        int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
        int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

        int type = cursor.getInt(typeIndex);
        String number = cursor.getString(numberIndex);

        LinearLayout bottomInfo = new LinearLayout(context);
        TextView typeTextView = new TextView(context);

        switch (type) {
            case 1: {
                typeTextView.setText("↙");
                typeTextView.setTextColor(this.getResources().getColor(R.color.blue, null));
            }
            break;
            case 2: {
                if (cursor.getLong(durationIndex) == 0)
                    typeTextView.setTextColor(this.getResources().getColor(R.color.red, null));
                else
                    typeTextView.setTextColor(this.getResources().getColor(R.color.green, null));

                typeTextView.setText("↗");
            }
            break;
            case 3: {
                typeTextView.setText("↙");
                typeTextView.setTextColor(this.getResources().getColor(R.color.red, null));
            }
        }

        typeTextView.setTypeface(Typeface.create(null, 700, false));
        typeTextView.setTextSize(22);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMarginEnd(10);
        typeTextView.setLayoutParams(p);

        bottomInfo.addView(typeTextView);

        TextView phoneNumber = new TextView(context);
        phoneNumber.setText(number);
        bottomInfo.addView(phoneNumber);

        LocalDateTime date = Instant.ofEpochMilli(cursor.getLong(dateIndex)).atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TextView dateTextView = new TextView(context);
        dateTextView.setText(DateTimeFormatter.ofPattern("HH:mm dd/MM").format(date));

        LinearLayout.LayoutParams dateTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dateTextParams.setMarginStart(20);
        dateTextView.setLayoutParams(dateTextParams);

        bottomInfo.addView(dateTextView);

        rightInfo.addView(bottomInfo);
    }

    public InputStream openPhoto(long contactId, Context context) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @NonNull
    private static TextView getTextView(Context context, String text, int size) {
        TextView name = new TextView(context);
        name.setText(text);
        name.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        name.setTextSize(size);
        return name;
    }

    public void readCallLog(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {
                CallLog.Calls.DATE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                ContactsContract.PhoneLookup._ID
        };

        Cursor cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
        );
        build(context, cursor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}