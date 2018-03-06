package com.ldp.administrator.novelreader;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends Activity {
   // public int bookAccountInt;
    private  ArrayList<String>   NovelNameANDHrefString=new ArrayList<>();
    ListView bookShelfListView;
    BookListViewAdapter bookListViewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


             bookShelfListView=(ListView)findViewById(R.id.bookShelfListView);
           bookListViewAdapter=new BookListViewAdapter(MainActivity.this,NovelNameANDHrefString);
            bookShelfListView.setAdapter(bookListViewAdapter);
        /*SQLiteDatabase db= SQLiteDatabase.openOrCreateDatabase(MainActivity.this.getFilesDir().getPath()+"/novel.db",null);
        String novel_list="create table  if not exists novel_list(ID integer primary key autoincrement,novel_name text,novel_reading_page text,reading_href text)";
        db.execSQL(novel_list);*/
        setSearchButtonClicked();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

                  if(requestCode==1&&resultCode==RESULT_OK)
                  {

                      NovelNameANDHrefString.add( data.getStringExtra("NameANDHref"));
                     bookListViewAdapter.notifyDataSetChanged();
                      Log.e("NameANDHref",data.getStringExtra("NameANDHref"));
                  }
                    if(requestCode==2&&resultCode==RESULT_OK){
            String URLNewChapter=data.getStringExtra("URLNewChapter");
                        int position=data.getIntExtra("position",0);

                        if ((NovelNameANDHrefString.get(position).split("<")).length<3)
                        NovelNameANDHrefString.set(position,NovelNameANDHrefString+"<"+URLNewChapter);
                        Log.e("WithNewURL",NovelNameANDHrefString.get(position));
                    }
    }

    public void setSearchButtonClicked(){
        Button SearchButton=(Button) findViewById(R.id.SeacheBut);



        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* SearchEditText=(EditText)findViewById(R.id.search_edittext);
                InputString=SearchEditText.getText().toString();
                new Thread(GetTextRunnable).start();
                Log.e("runnable",InputString);*/
               Intent SearchIntent=new Intent(MainActivity.this,SearchResultActivity.class);
                startActivityForResult(SearchIntent,1);
            }
        });
    }

    private ItemHoder itemHoder;
    private class BookListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
        ArrayList<String> NovelNameANDHrefString;
        /**构造函数*/
        public BookListViewAdapter(Context context,ArrayList<String> NovelNameANDHrefString) {
            this.mInflater = LayoutInflater.from(context);
          this.NovelNameANDHrefString=NovelNameANDHrefString;
        }
        @Override
        public View getView( final int position, View convertView, ViewGroup parent) {

           if (convertView == null) {
                convertView = mInflater.inflate(R.layout.novel_shelf_item,null);
                itemHoder = new ItemHoder();
               convertView.setTag(itemHoder);


            }else {
              itemHoder=  (ItemHoder) convertView.getTag();
            }

            itemHoder.NovelAuthorTxv=(TextView)convertView.findViewById(R.id.ItemNovelAuthor);
            itemHoder.NovelNameTxv=(TextView)convertView.findViewById(R.id.ItemNovelName);
            itemHoder.NovelRecentUpdateTxv=(TextView)convertView.findViewById((R.id.ItemRecentUpdate));
            itemHoder.NovelRecentUpdateTimeTxv=(TextView)convertView.findViewById(R.id.ItemRecentUpdateTime);
           // Log.e("itemHoder",iteMHolder.toString);
          itemHoder.NovelNameTxv.setText((NovelNameANDHrefString.get(position)).split("<")[0]);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                      Intent intent=new Intent(MainActivity.this,ReaderPageActivity.class);
                     intent.putExtra("NovelNameANDHrefString",NovelNameANDHrefString.get(position));
                    intent.putExtra("position",position);
                    startActivityForResult(intent,2);
                    Log.e("ShelfItemClick",NovelNameANDHrefString.get(position));
                }
            });
            return convertView;
        }

        @Override
        public int getCount() {
            return NovelNameANDHrefString.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }
    }

     private  class ItemHoder{
       public TextView NovelNameTxv,NovelAuthorTxv,NovelRecentUpdateTxv,NovelRecentUpdateTimeTxv;
       public ImageView NovelCoverImv,NovelHasNewChapterImv;

    }

}
