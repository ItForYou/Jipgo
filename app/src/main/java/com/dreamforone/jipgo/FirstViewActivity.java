package com.dreamforone.jipgo;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import util.Common;

public class FirstViewActivity extends AppCompatActivity {
    ViewPager viewPager;
    TextView prevTxt,nextTxt;
    String subjectArray[]={"우리 집 주변 수많은 인테리어 회사, 전문가를 만나보세요","당신의 기호에 맞는 무료 견적, 상담을 받아보세요","견적을 비교해보고 리뷰를 살펴보고 마음에 드는 회사를 선정하세요"};
    int m[]={R.drawable.m01,R.drawable.m02,R.drawable.m03};
    FirstViewAdapter adapter;
    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_view);
        setLayout();
    }
    public void setLayout(){
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        prevTxt=(TextView)findViewById(R.id.prevTxt);
        nextTxt=(TextView)findViewById(R.id.nextTxt);
        prevTxt.setOnClickListener(mOnClickListener);
        nextTxt.setOnClickListener(mOnClickListener);
        adapter=new FirstViewAdapter(getLayoutInflater(),subjectArray,m);
        int imgId=getResources().getIdentifier("selectImg1","id","com.dreamforone.jipgo");//사진 ImageView
        ImageView imageView=(ImageView)findViewById(imgId);
        imageView.setVisibility(View.VISIBLE);
        imageView.setSelected(true);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 2){
                    nextTxt.setText("Done");
                }else{
                    nextTxt.setText("Next");
                }
                for(int i=0;i<3;i++){

                    int j=i+1;
                    int imgId=getResources().getIdentifier("selectImg"+j,"id","com.dreamforone.jipgo");//사진 ImageView
                    ImageView imageView=(ImageView)findViewById(imgId);
                    imageView.setVisibility(View.VISIBLE);
                    if(0<position){
                        prevTxt.setVisibility(View.VISIBLE);
                    }else{
                        prevTxt.setVisibility(View.INVISIBLE);
                    }

                    if(position==i){

                        imageView.setSelected(true);
                    }else{
                        imageView.setSelected(false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(adapter);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.prevTxt:
                    position=viewPager.getCurrentItem();
                    if(0<position) {
                        viewPager.setCurrentItem(position - 1);
                    }
                    break;
                case R.id.nextTxt:
                    position=viewPager.getCurrentItem();
                    if(position<2) {
                        viewPager.setCurrentItem(position + 1);
                    }else if(position==2){
                        Common.savePref(getApplicationContext(),"first",true);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                    break;

            }
        }
    };

}
