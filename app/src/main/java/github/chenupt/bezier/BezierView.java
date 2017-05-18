package github.chenupt.bezier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenupt@gmail.com on 11/20/14.
 * Description : custom layout to draw bezier
 */

//FrameLayout帧布局,一层叠一层
public class BezierView extends FrameLayout {

    // 默认定点圆半径
    public static final float DEFAULT_RADIUS = 150;

    //Paint是画笔
    private Paint paint;
    //Path是Canvas中绘制的
    private Path path;

    // 手势坐标
    float x = 300;
    float y = 300;

    // 锚点坐标
    float anchorX = 200;
    float anchorY = 300;

    // 起点坐标
    float startX = 100;
    float startY = 100;

    // 定点圆半径
    float radius = DEFAULT_RADIUS;

    // 判断动画是否开始
    boolean isAnimStart;
    // 判断是否开始拖动
    boolean isTouch;

    ImageView exploredImageView;
    ImageView tipImageView;

    //构造函数,执行init()函数
    public BezierView(Context context) {
        super(context);
        //加载了99+小红点和小红点被移走后的逐帧动画,将这俩加进了ViewGroup
        init();
    }

    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BezierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        path = new Path();

        //paint大概是个类似canvas一样的画布
        paint = new Paint();
        //设置抗锯齿
        paint.setAntiAlias(true);

        //Paint.Style.STROKE 只绘制图形轮廓（描边）
        //Paint.Style.FILL 只绘制图形内容
        //Paint.Style.FILL_AND_STROKE既绘制轮廓也绘制内容
        //paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStyle(Paint.Style.STROKE);
        //设置线宽
        paint.setStrokeWidth(2);
        //线的颜色
        paint.setColor(Color.RED);

        //LayoutParmas封装了Layout的信息
        //ViewGroup是View的集合容器
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //ImageView是个图片容器
        exploredImageView = new ImageView(getContext());
        exploredImageView.setLayoutParams(params);

        //移走小红点后的逐帧动画
        exploredImageView.setImageResource(R.drawable.tip_anim);
        //设置不可见
        exploredImageView.setVisibility(View.INVISIBLE);

        tipImageView = new ImageView(getContext());
        tipImageView.setLayoutParams(params);
        //这是99+小红点
        tipImageView.setImageResource(R.drawable.skin_tips_newmessage_ninetynine);

        //加到ViewGroup里
        addView(tipImageView);
        addView(exploredImageView);
    }

    @Override
    //onLayout设置每个View在ViewGroup中的位置
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        exploredImageView.setX(startX - exploredImageView.getWidth()/2);
        exploredImageView.setY(startY - exploredImageView.getHeight()/2);

        tipImageView.setX(startX - tipImageView.getWidth()/2);
        tipImageView.setY(startY - tipImageView.getHeight()/2);

        super.onLayout(changed, left, top, right, bottom);
    }



    private List<Float> calculate(){
        //释放位置和起点的直线距离
        float distance = (float) Math.sqrt(Math.pow(y-startY, 2) + Math.pow(x-startX, 2));
        //小红点被拉扯就会在根部显示圆,这里设置这个圆的半径
        radius = -distance/15+DEFAULT_RADIUS;

        if(radius < 9){
            //动画展示状态字
            isAnimStart = true;

            //消失动画可见
            exploredImageView.setVisibility(View.VISIBLE);
            exploredImageView.setImageResource(R.drawable.tip_anim);
            //消失动画重启
            ((AnimationDrawable) exploredImageView.getDrawable()).stop();
            ((AnimationDrawable) exploredImageView.getDrawable()).start();

            //99+小红点设置隐藏,GONE是隐藏
            tipImageView.setVisibility(View.GONE);
        }

        // 根据角度算出四边形的四个点
        float offsetX = (float) (radius*Math.sin(Math.atan((y - startY) / (x - startX))));
        float offsetY = (float) (radius*Math.cos(Math.atan((y - startY) / (x - startX))));

        float x1 = startX - offsetX;
        float y1 = startY + offsetY;

        float x2 = x - offsetX;
        float y2 = y + offsetY;

        float x3 = x + offsetX;
        float y3 = y - offsetY;

        float x4 = startX + offsetX;
        float y4 = startY - offsetY;

        //以下为核心代码
        //清楚path设置的所有属性
        path.reset();
        path.moveTo(x1, y1);
		//quadTo绘制贝赛尔曲线,就是PS的钢笔工具,前两个为控制点,后面两个为结束点
        path.quadTo(anchorX, anchorY, x2, y2);
        path.lineTo(x3, y3);
        path.quadTo(anchorX, anchorY, x4, y4);
        path.lineTo(x1, y1);
        // 更改图标的位置
        tipImageView.setX(x - tipImageView.getWidth()/2);
        tipImageView.setY(y - tipImageView.getHeight()/2);

        //我改的
        List<Float> array = new ArrayList<Float>();
        array.add(x1);
        array.add(y1);
        array.add(x2);
        array.add(y2);
        array.add(x3);
        array.add(y3);
        array.add(x4);
        array.add(y4);
        array.add(offsetX);
        array.add(offsetY);
        array.add(anchorX);
        array.add(anchorY);

        return array;
    }

    @Override
	//onDraw调用了calculate方法,根据计算值绘制path和圆圈
    //onDraw是在View初化完成之后开始调用
    protected void onDraw(Canvas canvas){
        if(isAnimStart || !isTouch){
			//拉断后的效果
            //transparent是透明的意思
            //Porter/Duff是两个人名,提出了图形混合概念
            //两张图片叠在一起有多种混合模式
            //比如对比度混合,饱和度混合
            //PorterDuff.Mode.OVERLAY就是指定了混合模式,就是不混合,直接覆盖谁在上面显示谁
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
        }else{
            List<Float> array = calculate();
			//这个去掉也无所谓
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
			//绘制一条带有贝塞尔曲线的封闭路径
            canvas.drawPath(path, paint);
            canvas.drawText("A", array.get(0), array.get(1), paint);
            canvas.drawText("B", array.get(2), array.get(3), paint);
            canvas.drawText("C", array.get(4), array.get(5), paint);
            canvas.drawText("D", array.get(6), array.get(7), paint);
            canvas.drawText("Q", array.get(8), array.get(9), paint);
            canvas.drawText("R", array.get(10), array.get(11), paint);

			//绘制了两端的圆圈
            //99+和根部都是两个圆圈
            //99+原本是个贴图,拖动后还是贴图,但是加了个底部圆圈
            //canvas.drawCircle(startX, startY, radius, paint);
            //canvas.drawCircle(x, y, radius, paint);
        }
        super.onDraw(canvas);
    }


    @Override
	//提供信息供calculate计算
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            // 判断触摸点是否在tipImageView中
            Rect rect = new Rect();
            int[] location = new int[2];
            tipImageView.getDrawingRect(rect);
            tipImageView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.right + location[0];
            rect.bottom = rect.bottom + location[1];
            if (rect.contains((int)event.getRawX(), (int)event.getRawY())){
                isTouch = true;
            }
        }else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
            isTouch = false;
            tipImageView.setX(startX - tipImageView.getWidth()/2);
            tipImageView.setY(startY - tipImageView.getHeight()/2);
        }
        invalidate();
        if(isAnimStart){
            return super.onTouchEvent(event);
        }
		//锚点/控制点,起末点的中点
        anchorX =  (event.getX() + startX)/2;
        anchorY =  (event.getY() + startY)/2;
        x =  event.getX();
        y =  event.getY();
        return true;
    }


}
