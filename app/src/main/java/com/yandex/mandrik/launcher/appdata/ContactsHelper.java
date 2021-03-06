package com.yandex.mandrik.launcher.appdata;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

public class ContactsHelper {

    public static ArrayList<ContactInfo> fetchContacts(Context context, int count) {

        ArrayList<ContactInfo> contacts = new ArrayList();

        String phoneNumber = "none";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        // Loop for every contact in the phone
        int countInArray = 0;
        if (cursor.getCount() > 0) {
            for (int i = 0; cursor.moveToNext()&& countInArray < count; i++) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);
                    if (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phoneCursor.close();

                    Uri photoUri = getPhotoUri(context, contactId);
                    contacts.add(new ContactInfo(contactId, name, phoneNumber, photoUri));
                    countInArray++;
                }
            }
        }

        return contacts;
    }



    public static ArrayList<ContactInfo> fetchContactsByIds
            (Context context, ArrayList<String> listIdContacts) {

        ArrayList<ContactInfo> contacts = new ArrayList();

        String phoneNumber = "none";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if(listIdContacts.contains(contactId)) {
                    //int position = listIdContacts.indexOf(contactId);
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if (hasPhoneNumber > 0) {
                        // Query and loop for every phone number of the contact
                        Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);
                        if (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        phoneCursor.close();

                        Uri photoUri = getPhotoUri(context, contactId);
                        contacts.add(/*position, */new ContactInfo(contactId, name, phoneNumber, photoUri));
                    }
                }
            }
        }

        return contacts;
    }

    /**
     * @return the photo URI
     */
    public static Uri getPhotoUri(Context context, String contactId) {
        try {
            Cursor cur = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + contactId + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
                .parseLong(contactId));
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }



    /**
     * Query the Uri and read contact details. Return the picked contact info.
     * @param context used to get content resolver
     * @param data intent of picked contact
     * @return info of picked contact
     */
    public static ContactInfo getContactInfoPicked(Context context, Intent data) {

        Uri uri = data.getData();
        //Query the content uri
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));

        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        int hasPhoneNumber = Integer.parseInt(cursor.getString(
                cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
        if (hasPhoneNumber > 0) {
            // Query and loop for every phone number of the contact
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Uri photoUri = getPhotoUri(context, contactId);

            Cursor cur = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + contactId, null,
                    null);

            cur.moveToFirst();


            return new ContactInfo(contactId, name, phoneNumber, photoUri);
        }

        return null;
    }
}
