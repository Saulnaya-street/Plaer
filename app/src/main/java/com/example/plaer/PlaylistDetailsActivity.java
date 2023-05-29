package com.example.plaer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Bundle;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaylistDetailsActivity extends AppCompatActivity {
    private String playlistName;
    private ListView songsListView;
    private ArrayAdapter<String> songsListAdapter;
    private List<String> playlistSongs;
    private static final String PREFERENCES_PLAYLIST_CONTENT = "playlist_content_";
    private ArrayList<File> tracks; // Объявите ArrayList<File> вместо ArrayList<Track>


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_details);

        songsListView = findViewById(R.id.playlist_details_view);

        ListView tracksListView = findViewById(R.id.playlist_details_view);
        Intent intent = getIntent();
        playlistName = intent.getStringExtra("Название плейлиста");

        tracks = (ArrayList<File>) intent.getSerializableExtra("songs");

        Log.d("PlaylistDetails", "Получено имя плейлиста: " + playlistName);
        playlistSongs = getAllSongsFromPlaylist(playlistName);

        songsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistSongs);
        songsListView.setAdapter(songsListAdapter);
        tracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTrackPath = playlistSongs.get(position);

                // Создаём и отправляем intent на PlayerActivity
                Intent playerIntent = new Intent(PlaylistDetailsActivity.this, PlayerActivity.class);
                playerIntent.putExtra("songs", playlistSongs.toArray(new String[0])); // Конвертировать ArrayList<String> в String[]
                playerIntent.putExtra("songname", selectedTrackPath);
                playerIntent.putExtra("pos", position);
                startActivity(playerIntent);
            }
        });

        songsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String songKey = String.valueOf(position);
                removeSongFromPlaylist(playlistName, songKey);
                updateSongsListview(); // обновить ListView после удаления песни
                Toast.makeText(PlaylistDetailsActivity.this, "Удалена песня из плейлиста!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    // Получить список всех песен из плейлиста
public List<String> getAllSongsFromPlaylist(String playlistName){
    SharedPreferences playlistPreferences = getSharedPreferences(PREFERENCES_PLAYLIST_CONTENT + playlistName , MODE_PRIVATE);
    Map<String,?> keys = playlistPreferences.getAll();
    List<String> songs = new ArrayList<>();
    for (Map.Entry<String,?> entry: keys.entrySet()){
        songs.add(entry.getValue().toString());

        Log.d("Playlist", "Извлечена песня: " + entry.getValue().toString() + " из плейлиста: " + playlistName);
    }
    Log.d("PlaylistDetails", "Все песни в плейлисте " + playlistName + ": " + songs);
    return songs;
    }
 public void updateSongsListview(){
     playlistSongs = getAllSongsFromPlaylist(playlistName);
     if (songsListAdapter == null) {
         songsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistSongs);
         songsListView.setAdapter(songsListAdapter);
     } else {
         songsListAdapter.clear();
         songsListAdapter.addAll(playlistSongs);
         songsListAdapter.notifyDataSetChanged();
     }
 }
 public void removeSongFromPlaylist(String playlistName, String songKey){
     SharedPreferences playlistContentPreferences = getSharedPreferences(PREFERENCES_PLAYLIST_CONTENT + playlistName, MODE_PRIVATE);
     SharedPreferences.Editor editor = playlistContentPreferences.edit();
     editor.remove(songKey);
     editor.apply();
     Log.d("PlaylistDetails", "Удалена песня: " + songKey + " из плейлиста: " + playlistName);
 }
 protected void onResume(){
        super.onResume();

        updateSongsListview();
 }
}