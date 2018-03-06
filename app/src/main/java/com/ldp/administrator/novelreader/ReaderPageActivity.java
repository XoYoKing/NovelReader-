package com.ldp.administrator.novelreader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.util.RangeValueIterator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.annotation.Target;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class ReaderPageActivity extends Activity {
    String NovelName,HrefString ;
    int position;
    boolean isFromMainSOWithoutFolder=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent=getIntent();
        position=intent.getIntExtra("position",0);
        setContentView(R.layout.activity_reader_page);
        FoderListView=(ListView)findViewById(R.id.FoderListView);
       String NovelNameANDHrefString=intent.getStringExtra("NovelNameANDHrefString");
        String[] SplitResult=NovelNameANDHrefString.split("<");
        if (SplitResult.length>=3){
            IsInited=true;
            isFromMainSOWithoutFolder=true;
            URLNewChapter=SplitResult[2];
        }
        NovelName=SplitResult[0];HrefString=SplitResult[1] ;
        Log.e("ReaderPage",NovelNameANDHrefString);
           new Thread(GetTextRunnable).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent=new Intent();
           intent.putExtra("URLNewChapter",URLNewChapter);
          intent.putExtra("position",position);
            setResult(RESULT_OK,intent);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Document novelDocument;
  private  String   URLToGetDomain;
 private String FoderLinkObserve;
    private String RealLink;
    private  String NextChapterURL="";
    String BaseDomain="";
 private int CurrentNovelWebsite=0,AllNovelWebSite=5;
     LinearLayout FolderANDSettings;
boolean IsInited=false,IsFoderInited=false;
    String  URLNewChapter;
    Button button;
    private Runnable GetTextRunnable= new Runnable(){
        @Override
        public void run() {
            if (IsInited==true)
            Log.e("IsInited", "Inited");
            Looper.prepare();

            try{

   if (IsInited==false) {
       String URLSearch = HrefString;
       //  URLSearch= Jsoup.connect(URLSearch).get().getElementsByClass("main-html").first().getElementsByTag("div").first().select("a").first().attr("href");
       URLSearch = Jsoup.connect(URLSearch).get().getElementsByClass("main-html").get(CurrentNovelWebsite).getElementsByTag("div").first().select("a").first().attr("href");
       CurrentNovelWebsite++;

       Log.e("URLSearch", URLSearch);
       URLNewChapter = URLSearch.split("url=")[1];
       String a = URLNewChapter.substring(0, URLNewChapter.lastIndexOf("/"));
       URLToGetDomain = a;
       Log.e("URLSearchArray+Integer", URLSearch.split("url=")[1] + a);

   }

               TrustSSL.trustEveryone();
                novelDocument= Jsoup.connect(URLNewChapter)
                        .header("Accept", "*/*")
                        .header("Accept-Encoding", "gzip, deflate")
                        .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                        .header("Referer", "http://www.sodu.cc/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")
                        .timeout(5000)
                        .get();

            }catch (Exception e){
               // run();
                Log.e("documentGetErr",e.toString());
            }
            Message message=new Message();
            Bundle bundle=new Bundle();




            //mainElement =  novelDocument.getElementsContainingOwnText("章");


            Log.e("NovelDocumentToString",novelDocument.toString());

               Log.e("ExtraLinks", novelDocument.select("#content").select("a").toString());
           // novelDocument.select("#content").select("a").;
            String NovelContent=novelDocument.select("#content").html();

            Log.e("Whyemptycontents","Novelcontent"+novelDocument.select("#content").toString());

            if(NovelContent.isEmpty()){
                 NovelContent=novelDocument.select("#contents").html();
                if(NovelContent.isEmpty())
                    NovelContent=novelDocument.select("#BookText").html();
                if(NovelContent.isEmpty()){

                    Elements DivElment = novelDocument.getElementsByTag("div");
                    Log.e("DIV",DivElment.toString());
                   //NovelContent = DivElment.select("#content").html();
       NovelContent=DivElment.toString();
                }
            }
            bundle.putString("msgNovelContentWithHtml",NovelContent);
            Log.e("msgNoveContent"," MsgLength"+NovelContent.length());
            message.setData(bundle);
            message.what=1;


                Elements Alllinks = novelDocument.getElementsByTag("a");
                for (Element Alllink : Alllinks) {
                    Log.e("AFLink", Alllink.text());
                    Pattern p = Pattern.compile("下一章|下一页");
                    Matcher m = p.matcher(Alllink.text());
                    if (m.find()) {
                        NextChapterURL= Alllink.attr("abs:href");

                    }
                }
              if(NextChapterURL.isEmpty())
                           NextChapterURL = novelDocument.getElementsContainingOwnText("下一章").toString();//first().attr("abs:href");
            Log.e("NextChapterURL",NextChapterURL+"         NextURL");


      //     String FolderLink=novelDocument.getElementsMatchingOwnText("武侠修真").toString();
           // novelDocument.getElementsMatchingOwnText()
            if(IsInited==false|isFromMainSOWithoutFolder==true) {
                String FolderLink = novelDocument.getElementsMatchingOwnText("(章节)|(目录)").first().attr("abs:href");
                if (FolderLink.isEmpty()) {
               //     Elements Alllinks = novelDocument.getElementsByTag("a");
                    for (Element Alllink : Alllinks) {
                        Log.e("AFLink", Alllink.text());
                        Pattern p = Pattern.compile("(章节)|(目录)|(书目)");
                        Matcher m = p.matcher(Alllink.text());
                        if (m.find()) {
                            FolderLink = Alllink.attr("abs:href");
                        }

                    }
                }
                Log.e("FolderLink", FolderLink);

             /*   Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");

                Matcher m = p.matcher(URLToGetDomain);
                if (m.find()) {
                    BaseDomain = m.group();
                }
                if (FolderLink.contains("http")) {
                    RealLink = FolderLink;
                } else RealLink = BaseDomain + FolderLink;
                if (URLToGetDomain.contains("https"))
                    RealLink = "https://" + RealLink;
                else RealLink = "http://" + RealLink;*/
             RealLink=FolderLink;

                Log.e("DomaFolderlinkContents", "BaseDomainAND" + "Folderlink:" + BaseDomain + FolderLink + "    " + "Novelcontent" + NovelContent);
                //  Log.e("Contents",NovelContent);
                FolderANDSettings = (LinearLayout) findViewById(R.id.FolderANDSettings);

                button = (Button) findViewById(R.id.FolderButtonPopup);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("PopupFolderListview", "BtFolderClicked");
                        if(!IsFoderInited)
                        {
                        new Thread(getFolderANDNovelIformation).start();
                        IsFoderInited=true;
                            Toast.makeText(ReaderPageActivity.this,"正在生成目录 请稍后",Toast.LENGTH_LONG).show();

                        }else {
                            FoderListView=(ListView)findViewById(R.id.FoderListView);
                        FoderListView.setAdapter(new FoderListViewAdapter(ReaderPageActivity.this));
                        FoderListView.setVisibility(View.VISIBLE);
                                 }
                    }
                });
            }   //init后不再使用的代码 if END
            IsInited=true;
            handler.sendMessage(message);
            Looper.loop();


        }//run()END
    };
    private ListView FoderListView;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle=msg.getData();
            switch (msg.what) {
                case 1:
                    String string = bundle.getString("msgNovelContentWithHtml");
                    updateView(string);break;
                case 2:


                    FoderListView.setAdapter(new FoderListViewAdapter(ReaderPageActivity.this));
                    FoderListView.setVisibility(View.VISIBLE);
                    Log.e("ShowListView","ListViewShown?");
                    break;
            }
        }};
    private TextView Chapter;
    private class FoderListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局


        /**构造函数*/
        public FoderListViewAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);

        }
        @Override

        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.forder_listview_item,null);

                convertView.setTag(Chapter);


            }else {
                Chapter=  (TextView) convertView.getTag();
            }

Chapter=(TextView)convertView.findViewById(R.id.ForderListViewTextView);
           Chapter.setText(ArrarOfForderChapterString.get(position));

            Chapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 Log.e("TheChapterClicked",ArrarOfForderChapterString.get(position));
                    ShowNewPage(ArrarOfForderLinkString.get(position));
                    FoderListView.setVisibility(View.GONE);
                  //  v.setVisibility(View.GONE);
                }
            });
            return convertView;
        }

        @Override
        public int getCount() {
           return ArrarOfForderChapterString.size();

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
    private TextView ReaderTextView;
    CharSequence oldContent;
 private void updateView(String string){
     ReaderTextView=(TextView) findViewById(R.id.ReaderPageTextView);
      //ArrayList<String> EachPara=string.split("  ");

   ReaderTextView.setText(Html.fromHtml(string));
   //  oldContent = ReaderTextView.getText();
oldContent=Html.fromHtml(string);
resize();

ReaderTextView.setOnTouchListener(new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float ClickX=event.getX(),ClickY=event.getY();
        if(ClickX>240&&ClickX<480)
        {
           // if(FoderListView.isShown())
               // FoderListView.setVisibility(View.GONE);
             if (!FolderANDSettings.isShown())
            FolderANDSettings.setVisibility(View.VISIBLE);
             else {
                 FolderANDSettings.setVisibility(View.GONE);
             }

        }else{
            if(FolderANDSettings.isShown())
            FolderANDSettings.setVisibility(View.GONE);
            else
            resize();
        }


        Log.e("TouchClicked","Clicked");
        Log.e("TouchnowDispaly",""+ReaderTextView.getText().toString());
        return false;
    }
});
  String nowDisplay  =ReaderTextView.getText().toString();
     Log.e("nowDispaly",""+nowDisplay.length());
 }
   private ArrayList<String> ArrarOfForderLinkString=new ArrayList<>();
   private ArrayList<String> ArrarOfForderChapterString=new ArrayList<>();
 private Runnable getFolderANDNovelIformation=new Runnable() {
     @Override
     public void run() {
         Looper.prepare();
         try{

             TrustSSL.trustEveryone();
             Document FoderContentsDocuments= Jsoup.connect(RealLink)
                     .header("Accept", "*/*")
                     .header("Accept-Encoding", "gzip, deflate")
                     .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                     .header("Referer", "http://www.sodu.cc/")
                     .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")
                     .timeout(5000)
                     .get();
             Log.e("FoderContentsDocuments",FoderContentsDocuments.toString());
            Elements ForderLinksEl= FoderContentsDocuments.getElementsByTag("a");
           int IsFirstChapterFound=0;
             for(Element e:ForderLinksEl){
              if   (e.text().matches("(.*章.*)|(.*节.*)|(/d+.*)")){
                   if(  e.text().matches("((第一章)|(第1章)|(章一)|(一章)|(第一节)|(1)).*"))
                            IsFirstChapterFound++;

                if(IsFirstChapterFound>0) {
                      ArrarOfForderChapterString.add(e.text());//目录名字
                      ArrarOfForderLinkString.add(e.attr("abs:href"));//目录链接
                                            }
              }
             }
             Log.e("ChapterLink",ArrarOfForderLinkString.toString());
             Log.e(" ForderLinksEl","ForderLinkContain章"+ForderLinksEl.toString());
             Bundle bundle=new Bundle();
             Message message=new Message();
             message.setData(bundle);
             message.what=2;
              handler.sendMessage(message);
         }catch (Exception e){

             Log.e("documentGetErr",e.toString());
         }
         Looper.loop();
     }

 };//getFolderANDNovelSettings END
    private boolean NextChapterCounter=false;
    public int resize() {
        CharSequence newContent="";
            if(oldContent.length()>getCharNum()){
         newContent = oldContent.subSequence(0, getCharNum());
        oldContent=oldContent.subSequence(getCharNum(),oldContent.length()-1);
            }else {
                if(!NextChapterCounter){
                newContent=oldContent;
                NextChapterCounter=true;}else {
                    URLNewChapter=NextChapterURL;
                 //   Log.e("URLNewChapterINResize",URLNewChapter);
                    new Thread(GetTextRunnable).start();
                }
            }

        ReaderTextView.setText(newContent);
        Log.e("oldContent",""+oldContent.length());
        return oldContent.length() - newContent.length();
    }
    public int getCharNum() {
        return ReaderTextView.getLayout().getLineEnd(getLineNum());
    }
    public int getLineNum() {
        Layout layout = ReaderTextView.getLayout();
        int topOfLastLine = ReaderTextView.getHeight() - ReaderTextView.getPaddingTop() - ReaderTextView.getPaddingBottom() - ReaderTextView.getLineHeight();
        return layout.getLineForVertical(topOfLastLine);
    }
   public void ShowNewPage(String RealLink){
      URLNewChapter=RealLink;
       new Thread(GetTextRunnable).start();
   }
}//ReaderPagegeActivity  结束括号
/*
TrustSSL,Https链接的信任处理
* */
 class TrustSSL {
    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} //ssl结束
