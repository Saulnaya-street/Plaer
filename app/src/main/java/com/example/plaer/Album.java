package com.example.plaer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.internal.NavigationMenu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Album extends AppCompatActivity {
    // Компоненты пользовательского интерфейса
    private BottomNavigationView NavigationMenu;
    private ExtendedFloatingActionButton AddPlaylistFab;
    private ListView playlistListView;
    private ArrayAdapter<String> playlistListAdapter;

    // Константы для SharedPreferences
    private SharedPreferences playlistPreferences;
    private static final String PREFERENCES_NAME = "playlist_preferences";
    private static final String PREFERENCES_PLAYLIST_CONTENT = "playlist_content_";
    private static final String PREFERENCES_PLAYLIST_TITLES = "playlist_titles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        init();// Инициализация компонентов пользовательского интерфейса и слушателей

        Intent i = getIntent();
        String song = i.getStringExtra("song");
        // Слушатель кликов по элементам плейлиста
        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playlistName = (String) parent.getItemAtPosition(position);

                Intent intent = new Intent(Album.this, PlaylistDetailsActivity.class);
                intent.putExtra("Название плейлиста", playlistName);
                startActivity(intent);
            }
        });

        // Слушатель клика по кнопке "Add Playlist"
        AddPlaylistFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog();
                Log.d("Playlist", "Кнопка нажата, вызывается showDialog()");
            }
        });
        // Слушатель кликов по элементам нижней навигации
        NavigationMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int ItemId = item.getItemId();
                if (ItemId == R.id.playlist) {

                } else if (ItemId == R.id.music) {
                    startActivity(new Intent(Album.this, MainActivity.class));
                }

                return true;
            }
        });
    }
    // Показать диалоговое окно для добавления нового плейлиста
    private void showDialog() {
        final List<String> songs = getAllAudioFromDevice(this);


        Log.d("Playlist", "Песни: " + songs.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите песню для добавления в плейлист");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(songs);


        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String song = arrayAdapter.getItem(which);
                showPlaylistSelectionDialog(song);
            }
        });
        builder.show();
    }
    // Показать диалоговое окно для выбора плейлиста для песни
    private void showPlaylistSelectionDialog(final String song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите плейлист");

        final List<String> playlistNames = getAllPlaylistNames();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(playlistNames);

        builder.setPositiveButton("Добавить плейлист", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showNewPlaylistDialog(song);// Показать диалоговое окно для создания нового плейлиста
            }
        });

        builder.setNegativeButton("назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playlistName = arrayAdapter.getItem(which);
                addToPlaylist(playlistName ,song);

            }
        });
        builder.show();
    }
    // Показать диалоговое окно для создания нового плейлиста
    private void showNewPlaylistDialog(final String song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите имя нового плейлиста");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playlistName = input.getText().toString();
                addNewPlaylist(playlistName);
                addToPlaylist(playlistName ,song );
                updatePlaylistView();

            }
        });
        builder.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.show();
    }
    // Добавить песню в плейлист
    public void addToPlaylist( String playlistName, String song) {
        SharedPreferences playlistContentPreferences = getSharedPreferences(PREFERENCES_PLAYLIST_CONTENT + playlistName , MODE_PRIVATE);
        int songCount = playlistContentPreferences.getAll().size();
        SharedPreferences.Editor editor = playlistContentPreferences.edit();
        editor.putString(String.valueOf(songCount), song); // сохраняет полный путь к песне
        editor.apply();

        Log.d("Playlist", "Добавлена песня: " + song + " в плейлист: " + playlistName);

        Map<String,?> keys = playlistContentPreferences.getAll();
        for (Map.Entry<String,?> entry : keys.entrySet()) {
            Log.d("Playlist", "Песня в плейлисте " + playlistName + ": " + entry.getValue().toString());
        }
    }
    // Добавить новый плейлист
    public void addNewPlaylist(String playlistName) {
        if (playlistName.isEmpty()) {
            Toast.makeText(this, "Плейлист не может быть без имени!", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> existingPlaylists = getAllPlaylistNames();
        if (existingPlaylists.contains(playlistName)) {
            Toast.makeText(this, "Плейлист с таким именем уже существует!", Toast.LENGTH_SHORT).show();
            return;
        }
        addToPreferences(PREFERENCES_PLAYLIST_TITLES, playlistName, playlistName);
        updatePlaylistView();
    }
    // Добавить значение в SharedPreferences
    private void addToPreferences(String preferencesName, String key, String value){
        SharedPreferences sharedPreferences = getSharedPreferences(preferencesName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    // Получить список всех имен плейлистов
    public List<String> getAllPlaylistNames(){
        SharedPreferences playlistTitlesPreferences = getSharedPreferences(PREFERENCES_PLAYLIST_TITLES,MODE_PRIVATE);
        Map<String,?> keys = playlistTitlesPreferences.getAll();
        List<String> playlistNames = new ArrayList<>();
        for (Map.Entry<String,?> entry: keys.entrySet()){
            playlistNames.add(entry.getValue().toString());
        }
        return playlistNames;
    }
    // Получить список всех аудиофайлов на устройстве
    public static List<String> getAllAudioFromDevice(final Context context){
    final List<String> tempAudioList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA,MediaStore.Audio.AudioColumns.TITLE};

        Cursor c =context.getContentResolver().query(uri,projection,null,null,null);

        if (c != null){
            while (c.moveToNext()){
                String audioPath = c.getString(0);
                String audioName = c.getString(1);
                tempAudioList.add(audioName);
            }
            c.close();
        }
        return tempAudioList;
    }
    public void removePlaylist(String playlistName){
        SharedPreferences playlistContentPreferences = getSharedPreferences(PREFERENCES_PLAYLIST_CONTENT + playlistName , MODE_PRIVATE);
        playlistContentPreferences.edit().clear().apply();

        // Удалить имя плейлиста из списка всех плейлистов
        SharedPreferences playlistTitlesPreferences = getSharedPreferences(PREFERENCES_PLAYLIST_TITLES, MODE_PRIVATE);
        playlistTitlesPreferences.edit().remove(playlistName).apply();

        // Обновить список плейлистов в пользовательском интерфейсе
        updatePlaylistView();
    }

    public void updatePlaylistView(){
        List<String> playlistNames = getAllPlaylistNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistNames);
        playlistListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public List<String> getAllSongsFromPlaylist(String playlistName){
        SharedPreferences playlistPreferences = getSharedPreferences(PREFERENCES_PLAYLIST_CONTENT + playlistName , MODE_PRIVATE);
        Map<String,?> keys = playlistPreferences.getAll();
        List<String> songs = new ArrayList<>();
        for (Map.Entry<String,?> entry: keys.entrySet()){
            songs.add(entry.getValue().toString());
        }
        return songs;
    }
    private void init()
    {
        NavigationMenu = (BottomNavigationView) findViewById(R.id.navigation_panel);
        AddPlaylistFab = (ExtendedFloatingActionButton) findViewById(R.id.fab_add_playlist);
        playlistPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        playlistListView = findViewById(R.id.playlist_list_view);

        List<String> allPlaylistNames = getAllPlaylistNames();

        playlistListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allPlaylistNames);
        playlistListView.setAdapter(playlistListAdapter);

        updatePlaylistView();

        playlistListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String playlistName = (String) parent.getItemAtPosition(position);

                // Диалоговое окно с подтверждением удаления
                new AlertDialog.Builder(Album.this)
                        .setTitle("Удалить плейлист")
                        .setMessage("Вы уверены, что хотите удалить плейлист " + playlistName + "?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                removePlaylist(playlistName);
                            }
                        })
                        .setNegativeButton("Нет", null).show();

                return true;
            }
            });
        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playlistName = (String) parent.getItemAtPosition(position);

                Intent intent = new Intent(Album.this, PlaylistDetailsActivity.class);
                intent.putExtra("Название плейлиста", playlistName);
                startActivity(intent);
            }
        });

    }

}
