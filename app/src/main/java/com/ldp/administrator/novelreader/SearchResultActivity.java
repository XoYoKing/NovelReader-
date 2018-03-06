package com.ldp.administrator.novelreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        setSearchActionButtonClicked();
    }
    private Elements mainElement;
    private Document novelDocument;
    private  Document Page;
    private String InputString;
    private EditText SearchEditText ;



    private   void setSearchActionButtonClicked(){
      Button SearchActionButton=(Button) findViewById(R.id.search_action_button);
      SearchActionButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              SearchEditText=(EditText)findViewById(R.id.search_edittext);
              InputString=SearchEditText.getText().toString();
              InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

              imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//开启或者关闭软键盘
              Log.e("runnable","Inputstring:"+InputString);
              new Thread(GetTextRunnable).start();

          }
      });


  }
    private Runnable GetTextRunnable= new Runnable(){
        @Override
        public void run() {
            Looper.prepare();
            try{

                String URLSearch= "http://www.sodu.cc/result.html?searchstr="+ URLEncoder.encode(InputString,"utf-8");

                novelDocument= Jsoup.connect(URLSearch)
                        .header("Accept", "*/*")
                        .header("Accept-Encoding", "gzip, deflate")
                        .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                        .header("Referer", "http://www.sodu.cc/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")
                        .timeout(5000)
                        .get();
                //   Page=Jsoup.connect("http://www.liushuba.com/files/article/html/42/42409/22570888.html").get();


            }catch (Exception e){
                Log.e("documentGetErr",e.toString());
            }
            Elements ResultShelfItems=novelDocument.getElementsByClass("main-html");
            ArrayList<String> NovelNameANDHrefString=new ArrayList<>();
            for (Element Item : ResultShelfItems) {

                // linkHref = Item.getElementsByTag("div").select("a").attr("href")+linkHref;
                Elements DivItems= Item.getElementsByTag("div");



                  //  String hrefString=  DivItems.first().select("a").first().attr("href");
                  //  String NameIn= DivItems.first().select("a").first().text();
                     Element firstItem= DivItems.first().select("a").first();
                String hrefString=  firstItem.attr("href");
                  String NameIn= firstItem.text();
                NovelNameANDHrefString.add(NameIn+"<"+hrefString);
                   // Log.e("hrefString",NameIn+hrefString);


            }
            Message message=new Message();
            Bundle bundle=new Bundle();

            //  Log.e("htmlGet",novelDocument.toString());
            //  bundle.putString("msg",mainElement.select("a").attr("title") );
            // mainElement=novelDocument.select("div.main-html");

            // mainElement =  novelDocument.getElementsContainingOwnText("章");
            // bundle.putString("msg",mainElement.select("a").text() );

           // String saf=novelDocument.toString();
         //   Log.e("ARRAY",NovelNameANDHrefString.toString());
            bundle.putStringArrayList("NovelNameANDHrefString",NovelNameANDHrefString);
         //  Log.e("runnable",saf);
            message.setData(bundle);
            handler.sendMessage(message);
            Looper.loop();


        }
    };
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle=msg.getData();
            ArrayList<String> NovelNameANDHrefString=bundle.getStringArrayList("NovelNameANDHrefString");
           Log.e("NovelNameANDHrefString",NovelNameANDHrefString.toString());
            ListView SearchResultListView=(ListView)findViewById(R.id.SearchResultListView);
            SearchResultListView.setAdapter(new BookListViewAdapter(SearchResultActivity.this,NovelNameANDHrefString));

            //  Toast.makeText(MainActivity.this, string,Toast.LENGTH_LONG).show();


        }
    };
    private Itemholder itemHolder;
    private class BookListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
        private ArrayList<String> NovelNameANDHrefString;

        /**构造函数*/
        public BookListViewAdapter(Context context,ArrayList<String> NovelNameANDHrefString) {
            this.mInflater = LayoutInflater.from(context);
           this.NovelNameANDHrefString=NovelNameANDHrefString;
        }
        @Override

        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_search_result_item,null);
                itemHolder = new Itemholder();
                convertView.setTag(itemHolder);


            }else {
                itemHolder=  (Itemholder) convertView.getTag();
            }


            itemHolder.novelName=(TextView)convertView.findViewById(R.id.SearchResultNovelName);
            itemHolder.novelName.setText((NovelNameANDHrefString.get(position)).split("<")[0]);
            itemHolder.addBook=(Button) convertView.findViewById(R.id.addBook);
            itemHolder.addBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                String NameANDHref=(NovelNameANDHrefString.get(position));
                    Intent intent=new Intent();
                    intent.putExtra("NameANDHref",NameANDHref);
                    setResult(RESULT_OK,intent);
                    finish();
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

private class Itemholder{
    TextView novelName;
    Button addBook;
}
}









