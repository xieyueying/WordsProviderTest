package com.example.wordsprovidertest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ContentResolver resolver;
    private static ListView listView;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.contextmenu, menu);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(listView);
        resolver = this.getContentResolver();
        Button buttonAll = (Button) findViewById(R.id.showAll);
        Button buttonInsert = (Button) findViewById(R.id.insert);
        Button buttonDeleteAll = (Button) findViewById(R.id.allDetele);
        Button buttonSearch = (Button) findViewById(R.id.search);
        buttonAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAll();
            }
        });

        //增加单词
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InsertDialog();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "插入失败", Toast.LENGTH_LONG).show();
                }
            }
        });

        //查询单词
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchDialog();
            }
        });
        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteAllDialog();
            }
        });
    }
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        TextView textWord = null;
        TextView textMeaning = null;
        TextView textSimple = null;
        TextView textSmeaning = null;
        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;
        switch (item.getItemId()) {
            case R.id.delete: {
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textWord = (TextView) itemView.findViewById(R.id.word);
                if (textWord != null) {
                    String strWord = textWord.getText().toString();
                    DeleteDialog(strWord);
                }
                break;
            }
            case R.id.update: {
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textWord = (TextView) itemView.findViewById(R.id.word);
                textMeaning = (TextView) itemView.findViewById(R.id.meaning);
                textSimple = (TextView) itemView.findViewById(R.id.simple);
                textSmeaning = (TextView) itemView.findViewById(R.id.smeaning);
                if (textWord != null && textMeaning != null && textSimple != null && textSmeaning != null) {
                    String strWord = textWord.getText().toString();
                    String strMeaning = textMeaning.getText().toString();
                    String strSimple = textSimple.getText().toString();
                    String strSmeaning = textSmeaning.getText().toString();
                    UpdateDialog(strWord, strMeaning, strSimple, strSmeaning);
                }
                break;
            }
            default:
                return true;
        }
        return super.onContextItemSelected(item);
    }

    //插入对话框
    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strSimple = ((EditText) tableLayout.findViewById(R.id.txtSimple)).getText().toString();
                        String strSmeaning = ((EditText) tableLayout.findViewById(R.id.txtSmeaning)).getText().toString();
                        ContentValues values = new ContentValues();
                        values.put(Words.Word.COLUMN_NAME_WORD, strWord);
                        values.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
                        values.put(Words.Word.COLUMN_NAME_SIMPLE, strSimple);
                        values.put(Words.Word.COLUMN_NAME_SMEANING, strSmeaning);
                        resolver.insert(Words.Word.CONTENT_URI, values);
                        Toast.makeText(MainActivity.this, "插入成功", Toast.LENGTH_LONG).show();
                        setAll();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
    }

    //查询对话框
    private void SearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.search, null);
        new AlertDialog.Builder(this)
                .setTitle("查询单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING + "/" + strWord);
                        Cursor cursor = resolver.query(uri, new String[]{Words.Word.COLUMN_NAME_WORD
                                , Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SIMPLE
                                , Words.Word.COLUMN_NAME_SMEANING}, null, null, null);
                        if (cursor == null) {
                            Toast.makeText(MainActivity.this, "没有找到记录", Toast.LENGTH_LONG).show();
                            return;
                        }
                        setView(cursor);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
    }

    //删除对话框
    private void DeleteDialog(String strWord) {
        new AlertDialog.Builder(this)
                .setTitle("删除单词")
                .setMessage("是否删除单词？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING + "/" + strWord);
                        int m = resolver.delete(uri,null,null);
                        setAll();
                        if (m < 0) {
                            Toast.makeText(MainActivity.this, "没有找到记录", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
    }
    //删除全部对话框
    private void DeleteAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除单词")
                .setMessage("是否删除全部单词？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resolver.delete(Words.Word.CONTENT_URI,null,null);
                        setAll();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
    }
    //更新对话框
    private void UpdateDialog(String strWord,String strMeaning,String strSimple,String strSmeaning){
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        ((EditText) tableLayout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText) tableLayout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText) tableLayout.findViewById(R.id.txtSimple)).setText(strSimple);
        ((EditText) tableLayout.findViewById(R.id.txtSmeaning)).setText(strSmeaning);
        new AlertDialog.Builder(this)
                .setTitle("更新单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strNewWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strNewMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strNewSimple = ((EditText) tableLayout.findViewById(R.id.txtSimple)).getText().toString();
                        String strNewSmeaning = ((EditText) tableLayout.findViewById(R.id.txtSmeaning)).getText().toString();
                        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING+"/"+strWord);
                        ContentValues values = new ContentValues();
                        values.put(Words.Word.COLUMN_NAME_WORD,strNewWord);
                        values.put(Words.Word.COLUMN_NAME_MEANING,strNewMeaning);
                        values.put(Words.Word.COLUMN_NAME_SIMPLE,strNewSimple);
                        values.put(Words.Word.COLUMN_NAME_SMEANING,strNewSmeaning);
                        int m = 0;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            m = resolver.update(uri,values,null);
                        }
                        setAll();
                        if (m < 0) {
                            Toast.makeText(MainActivity.this, "没有找到记录", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create().show();
    }

    private void setView(Cursor cursor){
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        int wordInt = cursor.getColumnIndex(Words.Word.COLUMN_NAME_WORD);
        int meaningInt = cursor.getColumnIndex(Words.Word.COLUMN_NAME_MEANING);
        int simpleInt = cursor.getColumnIndex(Words.Word.COLUMN_NAME_SIMPLE);
        int smeaningInt = cursor.getColumnIndex(Words.Word.COLUMN_NAME_SMEANING);
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<String, String>();
            String word = cursor.getString(wordInt);
            String meaning = cursor.getString(meaningInt);
            String simple = cursor.getString(simpleInt);
            String smeaning = cursor.getString(smeaningInt);
            map.put(Words.Word.COLUMN_NAME_WORD, word);
            map.put(Words.Word.COLUMN_NAME_MEANING, meaning);
            map.put(Words.Word.COLUMN_NAME_SIMPLE, simple);
            map.put(Words.Word.COLUMN_NAME_SMEANING, smeaning);
            list.add(map);
        }
        setWordsListView(list);
        cursor.close();
    }
    private void setAll(){
        Cursor cursor = resolver.query(Words.Word.CONTENT_URI, new String[]{Words.Word.COLUMN_NAME_WORD,
                Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SIMPLE,
                Words.Word.COLUMN_NAME_SMEANING}, null, null, null);
        if (cursor == null) {
            Toast.makeText(MainActivity.this, "没有找到记录", Toast.LENGTH_LONG).show();
            return;
        }
        setView(cursor);
    }
    public void setWordsListView(ArrayList<Map<String, String>> items) {
        SimpleAdapter adapter = new SimpleAdapter(this, items,
                R.layout.listview_item, new String[]{Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING,
                Words.Word.COLUMN_NAME_SIMPLE, Words.Word.COLUMN_NAME_SMEANING}, new int[]{R.id.word,
                R.id.meaning, R.id.simple, R.id.smeaning});
        listView.setAdapter(adapter);
    }
}