package com.ozsaat.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainListActivity extends ListActivity {

    protected String[] mBlogPostTitles;
    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected JSONObject mBlogData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        if (isNetworkAvailable()) {
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();
        }
        else {
            Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();

        }
            //Toast.makeText(this, getString(R.string.no_items),Toast.LENGTH_LONG).show();
        }


        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.my_list, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            return id == R.id.action_settings || super.onOptionsItemSelected(item);
        }

        private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

            @Override
            protected JSONObject doInBackground(Object[] objects) {
                int responseCode = -1;
                JSONObject jsonResponse = null;
                try {
                    URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
                    HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                    connection.connect();

                    responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        Reader reader = new InputStreamReader(inputStream);
                        int nextCharacter; // read() returns an int, cast to char later
                        String responseData = "";
                        while(true){ // Infinate loop. Can only b stopped by a break
                            nextCharacter = reader.read(); // read() without parameters returns one character
                            if(nextCharacter == -1) // A return value of -1 means we reached the end
                                break;
                            responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
                        }

                        jsonResponse = new JSONObject(responseData);
                    }
                    else {
                        Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                    }

                } catch (MalformedURLException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (Exception e) {
                    Log.e(TAG, "Exception caught: ", e);
                }

                return jsonResponse;

            }

            @Override
            protected void onPostExecute(JSONObject result) {
                mBlogData = result;
                updateList();
            }

        }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
        isAvailable = true;
        }
        return isAvailable;
    }


// Dialog and button
private void updateList() {
        if (mBlogData == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.error_title));
            builder.setMessage(getString(R.string.error_message));
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            try {
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                mBlogPostTitles = new String[jsonPosts.length()];
                for (int i = 0; i < jsonPosts.length(); i++) {
                    JSONObject post = jsonPosts.getJSONObject(i);
                    String title = post.getString("title");
                    title = Html.fromHtml(title).toString();
                    mBlogPostTitles[i] = title;

  // Below code causing errors when inside AsyncTask. Had to move.
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, mBlogPostTitles);
                setListAdapter(adapter);
            }
            catch (JSONException e) {
                Log.e(TAG, "Exception caught!", e);
            }
        }
    }
}