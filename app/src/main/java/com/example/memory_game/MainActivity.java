package com.example.memory_game;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    GameView gv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gv = new GameView(this);
        setContentView(gv);
    }

    //도형 정보
    class Shape{
        final static int RECT = 0;
        final static int CIRCLE = 1;
        final static int TRIANGLE = 2;
        int what; // RECT, CIRCLE, TRIANGLE 중 하나
        int color; //5가지 색 중 하나
        Rect rt; // RECT,CIRCLE, TRIANGLE이 그려질 위치와 크기
    }
    // 게임 뷰 (실질적 메인)
    class GameView extends View {
        //그리기 모드. 빈화면 또는 도형 출력
        final static int BLANK = 0; // 검은 빈 화면 출력
        final static int PLAY = 1; // 도형 출력
        final static int DELAY = 1500; //게임 진행 속도
        int status; //현재 그리기 모드
                    // 도형을 보여줄 것인지 안보여줄 것인지 결정
        ArrayList<Shape> arShape = new ArrayList<Shape>();
        Random rnd = new Random();
        Activity mParent;

        // 새 도형 추가하고 화면에 다시 그림
        // 시간 딜레이를 주기 위해 핸들러 사용
        Handler mHandler = new Handler(){
            public void handleMessage(Message msg){
                AddNewShape();
                status = PLAY;
                invalidate();

                String title = "MemoryPower - " + arShape.size() + "단계";
                mParent.setTitle(title);
            }
        };

        public GameView(Context context){
            super(context);
            mParent = (Activity)context;
            status = BLANK;
            mHandler.sendEmptyMessageDelayed(0, DELAY);
        }

        // 뷰에 그림 그림림
       public void onDraw(Canvas canvas){
            canvas.drawColor(Color.BLACK); // 검정색 배경으로 지움.
            //빈 화면이면 지우기만 하고 return
            if(status == BLANK){
                return;
            }

            //arShape에 저장된 모든 도형 출력
            int idx;
            for(idx = 0; idx < arShape.size(); idx++){
                Paint pnt = new Paint();
                pnt.setColor(arShape.get(idx).color);
                Rect rt = arShape.get(idx).rt;
                switch(arShape.get(idx).what){
                    case Shape.RECT:
                        canvas.drawRect(rt, pnt);
                        break;
                    case Shape.CIRCLE:
                        canvas.drawCircle(rt.left + rt.width()/2, rt.top + rt.height()/2, rt.width()/2, pnt);
                        break;
                    case Shape.TRIANGLE:
                        Path path = new Path();
                        path.moveTo(rt.left + rt.width()/2, rt.top); // 삼각형의 상단 꼭짓점 위치로 좌표 이동
                        path.lineTo(rt.left, rt.bottom); // 삼각형의 왼쪽 꼭짓점으로 선 그림
                        path.lineTo(rt.right, rt.bottom); // 삼각형의 오른쪽 꼭짓점으로 선 그림
                        canvas.drawPath(path, pnt);
                        break;
                }
            }
        }

        public boolean onTouchEvent(MotionEvent event){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                int sel; // 터치한 도형 번호
                sel = FindShapeIdx((int)event.getX(), (int)event.getY());
                if(sel == -1) //빈 바닥을 터치했다면 무시
                    return true;

                if (sel == arShape.size()-1){ //마지막 도형 터치시
                    // 잠시 빈 화면 보여주고, 새로운 도형 추가하여 보여줌
                    status = BLANK;
                    invalidate(); // => onDraw 메소드 호출됨
                    mHandler.sendEmptyMessageDelayed(0, DELAY);
                }else{
                    // 다른 도형 선택시
                    // 알림창을 통해 한 번 더 할지, 종료할지 선택
                    new AlertDialog.Builder(getContext())
                            .setTitle("게임 끝")
                            .setMessage("재밌지! 또 할래?")
                            .setPositiveButton("한번더", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton){
                            arShape.clear();
                            status = BLANK;
                            invalidate();
                            mHandler.sendEmptyMessageDelayed(0, DELAY);
                        }
                    })
                    .setNegativeButton("안해", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton){
                            mParent.finish();
                        }
                    })
                            .show();
                }
                return true;
            }
            return false;
        }

        void AddNewShape(){
            Shape shape = new Shape();
            int idx;
            boolean bFindIntersect; // 기존 도형과 겹치는지 여부
            Rect rt = new Rect();

//            기존 도형과 겹치지 않는 새 위치 찾기
            while(true){
                // 크기는 32, 38, 64 중 하나로 선택
                int size = 32 + 16*rnd.nextInt(3);
                rt.left = rnd.nextInt(getWidth());
                rt.top = rnd.nextInt(getHeight());
                rt.right = rt.left + size;
                rt.bottom = rt.top + size;
                // 화면 벗어나면 다시 설정
                if (rt.right > getWidth() || rt.bottom > getHeight()){
                    continue;
                }
                // 기존 도형 순회하며 겹치는 도형이 있는지 확인
                bFindIntersect = false;
                for(idx = 0; idx < arShape.size(); idx++){
                    if(rt.intersect(arShape.get(idx).rt) == true){
                        bFindIntersect = true; // 기존 도형과 겹침
                    }
                }
                // 겹치지 않을 떼 위치 확정(반복문 종료)
                if (!bFindIntersect){
                    break;
                }
            }

            //새 도형 정보 작성
            //모양
            shape.what = rnd.nextInt(3);
            //색
            switch(rnd.nextInt(5)){
                case 0:
                    shape.color = Color.WHITE;
                    break;
                case 1:
                    shape.color = Color.RED;
                    break;
                case 2:
                    shape.color = Color.GREEN;
                    break;
                case 3:
                    shape.color = Color.BLUE;
                    break;
                case 4:
                    shape.color = Color.YELLOW;
                    break;
            }
            shape.rt = rt;
            arShape.add(shape);
        }

        int FindShapeIdx(int x, int y){
            for(int i = 0; i < arShape.size(); i++){
                if(arShape.get(i).rt.contains(x, y)){
                    return i;
                }
            }
            return -1;
        }
    }


}