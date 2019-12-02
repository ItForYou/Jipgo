package com.dreamforone.jipgo;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FirstViewAdapter  extends PagerAdapter{
    LayoutInflater layoutInflater;
    String subjectArray[];
    int m[];
    public FirstViewAdapter(LayoutInflater inflater,String[] subjectArray,int[] m){
        this.layoutInflater=inflater;
        this.subjectArray=subjectArray;
        this.m=m;
    }
    @Override
    public int getCount() {
        return 3;
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
        ((ViewPager) container).removeView((View) object);

    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view=null;
        //새로운 View 객체를 Layoutinflater를 이용해서 생성
        //만들어질 View의 설계는 res폴더>>layout폴더>>viewpater_childview.xml 레이아웃 파일 사용
        view= layoutInflater.inflate(R.layout.first_view, null);
        ViewHolder viewHolder=new ViewHolder();
        viewHolder.subjectTxt=(TextView)view.findViewById(R.id.subjectTxt);
        viewHolder.swipeImg=(ImageView)view.findViewById(R.id.swipeImg);
        viewHolder.swipeImg.setImageDrawable(view.getResources().getDrawable(m[position]));
        Log.d("subject",subjectArray[position]);
        viewHolder.subjectTxt.setText(subjectArray[position]);
        viewHolder.subjectTxt.setTextColor(Color.parseColor("#000000"));
        container.addView(view);
        return view;
    }
    class ViewHolder{
        ImageView swipeImg;
        TextView subjectTxt;
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }
}
