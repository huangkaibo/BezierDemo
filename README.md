BezierDemo
==========

A demo to show bezier. Another sample is [SpringIndicator](https://github.com/chenupt/SpringIndicator).

![Alt text](https://raw.githubusercontent.com/chenupt/BezierDemo/master/pic/bezier.gif)
   
代码模拟了QQ未读消息小红点被拖拽后的拉扯效果和拖远后消失的动画效果


#代码解读(注释不是附带,都是自己打的)
阅读代码后发现拉扯效果是用一段绘制函数实现的
绘制函数中有这一段,是设置两个圆的半径,为了显示清楚,将半径放大

```
// 默认定点圆半径
public static final float DEFAULT_RADIUS = 20;
// 定点圆半径
float radius = DEFAULT_RADIUS;
float offsetX = (float) (radius*Math.sin(Math.atan((y - startY) / (x - startX))));
float offsetY = (float) (radius*Math.cos(Math.atan((y - startY) / (x - startX))));
```

将
```
public static final float DEFAULT_RADIUS = 20;
```
改成
```
public static final float DEFAULT_RADIUS = 150;
```

再次运行可以看到
![Alt text](https://raw.githubusercontent.com/chenupt/BezierDemo/master/pic/1.png)
也就是说,这段代码绘制了起点和终点处的两个圆圈

---

绘制过程有这句话
```
//Paint.Style.STROKE 只绘制图形轮廓（描边）
//Paint.Style.FILL 只绘制图形内容
//Paint.Style.FILL_AND_STROKE既绘制轮廓也绘制内容
paint.setStyle(Paint.Style.FILL_AND_STROKE);
```

将
```
paint.setStyle(Paint.Style.FILL_AND_STROKE);
```
改成
```
paint.setStyle(Paint.Style.STROKE);
```
也就是去掉内部填充,只显示边界
![Alt text](https://raw.githubusercontent.com/chenupt/BezierDemo/master/pic/2.png)
可以看出左右两侧绘制的圆
另外,发现拉扯的效果并没有那么神奇,只是对于中间这个不知道什么形状的东西填充颜色而已

---

去掉两端绘制的圆,开始看中间的拉扯效果如何实现

```
//绘制了两端的圆圈
//99+和根部都是两个圆圈
//99+原本是个贴图,拖动后还是贴图,但是加了个底部圆圈
//canvas.drawCircle(startX, startY, radius, paint);
//canvas.drawCircle(x, y, radius, paint);
```

去掉了圆的绘制
![Alt text](https://raw.githubusercontent.com/chenupt/BezierDemo/master/pic/3.png)

---

结合代码里出现的Path路径,可以看出拉扯效果的边界是用Path路径绘制的

```
//清楚path设置的所有属性
path.reset();
path.moveTo(x1, y1);
//quadTo绘制贝赛尔曲线,就是PS的钢笔工具,前两个为控制点,后面两个为结束点
path.quadTo(anchorX, anchorY, x2, y2);
path.lineTo(x3, y3);
path.quadTo(anchorX, anchorY, x4, y4);
path.lineTo(x1, y1);
```

---

为了认清X1/Y1至X4/Y4等几个点,我想在这几点上加上一个字符来标识
但是这个path被放在了计算函数calculate()里和绘制函数onDraw()不在一起
所以做了如下改动

```
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
```

将calculate()里的几个坐标打包传入onDraw()中

```
List<Float> array = calculate();
canvas.drawText("A", array.get(0), array.get(1), paint);
canvas.drawText("B", array.get(2), array.get(3), paint);
canvas.drawText("C", array.get(4), array.get(5), paint);
canvas.drawText("D", array.get(6), array.get(7), paint);
canvas.drawText("Q", array.get(8), array.get(9), paint);
canvas.drawText("R", array.get(10), array.get(11), paint);
```

效果如下
![Alt text](https://raw.githubusercontent.com/chenupt/BezierDemo/master/pic/4.png)
不太清晰,不过可以看出来
左下A: 对应X1,Y1
右下B: 对应X2,Y2
右上C: 对应X3,Y3
左上D: 对应X4,Y4
中间R: 对应offsetX,offsetY
左侧中间Q: 对应anchorX,anchor

---

再看路径绘制顺序

```
path.moveTo(x1, y1);
path.quadTo(anchorX, anchorY, x2, y2);
path.lineTo(x3, y3);
path.quadTo(anchorX, anchorY, x4, y4);
path.lineTo(x1, y1);
```

可以看出先定位A点,绘制曲线到B点,直线到C点,曲线到D点,直线回A点

经查询,曲线是贝赛尔曲线,大概就是经过三个点做出一个尽量光滑的曲线
所以除了起末点还需要一个控制点,也就是anchorX和anchorY
这两个点不难得出,就是起末点的中点

---

然后再说下几个贴图和动画
有个tip_anim.xml文件

 
![Alt text](https://raw.githubusercontent.com/chenupt/BezierDemo/master/pic/5.png)
就是定义了一个逐帧动画,把5个图片轮流播放,产生小红点的消失效果动画

//移走小红点后的逐帧动画
exploredImageView.setImageResource(R.drawable.tip_anim);
//设置不可见
exploredImageView.setVisibility(View.INVISIBLE);
该动画一直循环播放,只是不显示

当小红点被扯掉时显示
//消失动画可见
exploredImageView.setVisibility(View.VISIBLE);
exploredImageView.setImageResource(R.drawable.tip_anim);
//消失动画重启
((AnimationDrawable) exploredImageView.getDrawable()).stop();
((AnimationDrawable) exploredImageView.getDrawable()).start();

---

另一端就是一个贴图
![Alt text](https://raw.githubusercontent.com/chenupt/BezierDemo/master/pic/6.png)
放在这里
//这是99+小红点
tipImageView.setImageResource(R.drawable.skin_tips_newmessage_ninetynine);
