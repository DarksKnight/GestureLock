package com.lockdemo.widget;

import com.lockdemo.R;
import com.lockdemo.entity.Point;
import com.lockdemo.util.LockUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by apple on 3/7/15.
 */
public class CustomLockView extends View {
    //圆初始状态时的图片
    private Bitmap locus_round_original;
    //圆点击时的图片
    private Bitmap locus_round_click;
    //错误
    private Bitmap locus_round_error;
    //方向箭头
    private Bitmap locus_arrow;
    //方向箭头
    private Bitmap locus_arrow_error;


    //控件宽度
    private float w = 0;
    //控件高度
    private float h = 0;
    //是否已缓存
    private boolean isCache = false;
    //画笔
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //九宫格的圆
    private Point[][] mPoints = new Point[3][3];
    //圆的半径
    private float r = 0;
    //选中圆的集合
    private List<Point> sPoints = new ArrayList<Point>();
    //判断是否正在绘制并且未到达下一个点
    private boolean movingNoPoint = false;
    //正在移动的x,y坐标
    float moveingX, moveingY;
    //是否可操作
    private boolean isTouch = true;
    //密码最小长度
    private int passwordMinLength = 3;
    //判断是否触摸屏幕
    private boolean checking = false;
    //刷新
    private TimerTask task = null;
    //计时器
    private Timer timer = new Timer();
    //监听
    private OnCompleteListener mCompleteListener;
    //清除痕迹的时间
    private long CLEAR_TIME = 0;
    //错误限制 默认为4次
    private int errorTimes=4;
    //记录上一次滑动的密码
    private int[] mIndexs=null;
    //记录当前第几次触发 默认为0次
    private int showTimes=0;
    //当前密码是否正确 默认为正确
    private boolean isCorrect=true;
    //是否显示滑动方向 默认为显示
    private boolean isShow=true;
    //验证或者设置 0:设置 1:验证
    private int status=0;
    //用于执行清除界面
    private Handler handler=new Handler();
    //用于定时执行清除界面
    private Runnable run=new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(run);
            reset();
            postInvalidate();
        }
    };
    private String errorStr="";

    public CustomLockView(Context context) {
        super(context);
    }

    public CustomLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isCache) {
            initCache();
        }
        //绘制圆以及显示当前状态
        drawToCanvas(canvas);
    }

    /**
     * 初始化Cache信息
     */
    private void initCache() {
        w = this.getWidth();
        h = this.getHeight();
        float x = 0;
        float y = 0;
        // 以最小的为准
        // 纵屏
        x = (w - h) / 2;
        w = h;
        locus_round_original = BitmapFactory.decodeResource(getResources(), R.mipmap.unselected);
        locus_round_click = BitmapFactory.decodeResource(getResources(), R.mipmap.selected);
        locus_arrow = BitmapFactory.decodeResource(getResources(), R.mipmap.gesturetrianglebrown);
        locus_round_error=BitmapFactory.decodeResource(getResources(),R.mipmap.selected_error);
        locus_arrow_error=BitmapFactory.decodeResource(getResources(),R.mipmap.gesturetrianglebrownerror);
        // 计算圆圈图片的大小
        float canvasMinW = w;
        if (w > h) {
            canvasMinW = h;
        }
        float roundMinW = canvasMinW / 8.0f * 2;
        float roundW = roundMinW / 2.f;
        float deviation = canvasMinW % (8 * 2) / 2;
        x += deviation;
        if (locus_round_original != null) {
            if (locus_round_original.getWidth() > roundMinW) {
                roundW = locus_round_original.getWidth() / 2;
            }
            mPoints[0][0] = new Point(roundW, y + 0 + roundW);
            mPoints[0][1] = new Point(x + w / 2-5, y + 0 + roundW);
            mPoints[0][2] = new Point(x + w - roundW-5, y + 0 + roundW);
            mPoints[1][0] = new Point(roundW, y + h / 2);
            mPoints[1][1] = new Point(x + w / 2-5, y + h / 2);
            mPoints[1][2] = new Point(x + w - roundW-5, y + h / 2);
            mPoints[2][0] = new Point(roundW, y + h - roundW);
            mPoints[2][1] = new Point(x + w / 2-5, y + h - roundW);
            mPoints[2][2] = new Point(x + w - roundW-5, y + h - roundW);
            int k = 0;
            for (Point[] ps : mPoints) {
                for (Point p : ps) {
                    p.index = k;
                    k++;
                }
            }
            //获得圆形的半径
            r = locus_round_original.getHeight() / 2;// roundW;
            isCache = true;
        }
    }

    /**
     * 图像绘制
     *
     * @param canvas
     */
    private void drawToCanvas(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        // 画连线
        if (sPoints.size() > 0) {
            Point tp = sPoints.get(0);
            for (int i = 1; i < sPoints.size(); i++) {
                //根据移动的方向绘制线
                Point p = sPoints.get(i);
                if(isCorrect){
                    drawLine(canvas, tp, p);
                }else{
                    drawErrorLine(canvas,tp,p);
                }
                tp = p;
            }
            if (this.movingNoPoint) {
                //到达下一个点停止移动绘制固定的方向
                drawLine(canvas, tp, new Point((int) moveingX + 20, (int) moveingY));
            }
        }
        // 画所有点
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
                if (p != null) {
                    if (p.state == Point.STATE_CHECK) {
                        canvas.drawBitmap(locus_round_click, p.x - r, p.y - r, mPaint);
                    } else if(p.state == Point.STATE_CHECK_ERROR){
                        canvas.drawBitmap(locus_round_error, p.x - r, p.y - r, mPaint);
                    }else {
                        canvas.drawBitmap(locus_round_original, p.x - r, p.y - r, mPaint);
                    }
                }
            }
        }
        if(isShow){
            // 绘制方向图标
            if (sPoints.size() > 0) {
                Point tp = sPoints.get(0);
                for (int i = 1; i < sPoints.size(); i++) {
                    //根据移动的方向绘制方向图标
                    Point p = sPoints.get(i);
                    if(isCorrect){
                        drawDirection(canvas, tp, p);
                    }else{
                        drawErrorDirection(canvas, tp, p);
                    }
                    tp = p;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 不可操作
//        if (!isTouch) {
//            return false;
//        }
        isCorrect=true;
        handler.removeCallbacks(run);
        movingNoPoint = false;
        float ex = event.getX();
        float ey = event.getY();
        boolean isFinish = false;
        Point p = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 点下
                // 如果正在清除密码,则取消
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                // 删除之前的点
                reset();
                p = checkSelectPoint(ex, ey);
                if (p != null) {
                    checking = true;
                }
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                if (checking) {
                    p = checkSelectPoint(ex, ey);
                    if (p == null) {
                        movingNoPoint = true;
                        moveingX = ex;
                        moveingY = ey;
                    }
                }
                break;
            case MotionEvent.ACTION_UP: // 提起
                p = checkSelectPoint(ex, ey);
                checking = false;
                isFinish = true;
                break;
            default:
                movingNoPoint = true;
                break;
        }
        if (!isFinish && checking && p != null) {
            int rk = crossPoint(p);
            if (rk == 2) {
                //与非最后一重叠
                movingNoPoint = true;
                moveingX = ex;
                moveingY = ey;
            } else if (rk == 0) {
                //一个新点
                p.state = Point.STATE_CHECK;
                addPoint(p);
            }
        }
        if (isFinish) {
            handler.postDelayed(run,1500);
            if (this.sPoints.size() == 1) {
                this.reset();
            } else if (this.sPoints.size() < passwordMinLength
                    && this.sPoints.size() > 0) {
                clearPassword();
                Toast.makeText(this.getContext(), "密码太短,请重新输入!", Toast.LENGTH_SHORT).show();
            } else if (mCompleteListener != null) {
                if (this.sPoints.size() >= passwordMinLength) {
                    int[] indexs=new int[sPoints.size()];
                    for(int i=0;i<sPoints.size();i++){
                        indexs[i]=sPoints.get(i).index;
                    }
                    if(status==0){
                        invalidatePass(indexs);
                    }else if(status==1){
                        invalidateOldPsw(indexs);
                    }
                }
            }
        }
        postInvalidate();
        return true;
    }

    /**
     * 向选中点集合中添加一个点
     *
     * @param point
     */
    private void addPoint(Point point) {
        this.sPoints.add(point);
    }

    /**
     * 检查点是否被选择
     *
     * @param x
     * @param y
     * @return
     */
    private Point checkSelectPoint(float x, float y) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
                if (LockUtil.checkInRound(p.x, p.y, r, (int) x, (int) y)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * 判断点是否有交叉 返回 0,新点 ,1 与上一点重叠 2,与非最后一点重叠
     *
     * @param p
     * @return
     */
    private int crossPoint(Point p) {
        // 重叠的不最后一个则 reset
        if (sPoints.contains(p)) {
            if (sPoints.size() > 2) {
                // 与非最后一点重叠
                if (sPoints.get(sPoints.size() - 1).index != p.index) {
                    return 2;
                }
            }
            return 1; // 与最后一点重叠
        } else {
            return 0; // 新点
        }
    }

    /**
     * 重置点状态
     */
    public void reset() {
        for (Point p : sPoints) {
            p.state = Point.STATE_NORMAL;
        }
        sPoints.clear();
    }

    /**
     * 清空当前信息
     */
    public void clearCurrent(){
        showTimes=0;
        errorTimes=4;
        isCorrect=true;
        reset();
        postInvalidate();
    }

    /**
     * 画两点的连接
     *
     * @param canvas
     * @param a
     * @param b
     */
    private void drawLine(Canvas canvas, Point a, Point b) {
        int color=R.color.yellow;
        mPaint.setColor(getResources().getColor(color));
        mPaint.setStrokeWidth(3);
        canvas.drawLine(a.x, a.y, b.x, b.y, mPaint);
    }

    /**
     * 错误线
     * @param canvas
     * @param a
     * @param b
     */
    private void drawErrorLine(Canvas canvas, Point a, Point b) {
        int color=R.color.red;
        mPaint.setColor(getResources().getColor(color));
        mPaint.setStrokeWidth(3);
        canvas.drawLine(a.x, a.y, b.x, b.y, mPaint);
    }

    /**
     * 绘制方向图标
     *
     * @param canvas
     * @param a
     * @param b
     */
    private void drawDirection(Canvas canvas, Point a, Point b) {
        //获取角度
        float degrees = LockUtil.getDegrees(a, b);
        //根据两点方向旋转
        canvas.rotate(degrees, a.x, a.y);
        float x = a.x + r/2;
        float y = a.y - locus_arrow.getHeight() / 2.0f;
        if(degrees==270){
            y = a.y - locus_arrow.getHeight() / 2.0f;
        }
        //绘制箭头
        canvas.drawBitmap(locus_arrow, x, y, mPaint);
        //旋转方向
        canvas.rotate(-degrees, a.x, a.y);
    }

    /**
     * 错误方向
     * @param canvas
     * @param a
     * @param b
     */
    private void drawErrorDirection(Canvas canvas, Point a, Point b) {
        //获取角度
        float degrees = LockUtil.getDegrees(a, b);
        //根据两点方向旋转
        canvas.rotate(degrees, a.x, a.y);
        float x = a.x + r/2;
        float y = a.y - locus_arrow.getHeight() / 2.0f;
        if(degrees==270){
            y = a.y - locus_arrow.getHeight() / 2.0f;
        }
        //绘制箭头
        canvas.drawBitmap(locus_arrow_error, x, y, mPaint);
        //旋转方向
        canvas.rotate(-degrees, a.x, a.y);
    }

    /**
     * 清除密码
     */
    private void clearPassword() {
        clearPassword(CLEAR_TIME);
    }

    /**
     * 清除密码
     */
    private void clearPassword(final long time) {
        if (time > 1) {
            if (task != null) {
                task.cancel();
            }
            postInvalidate();
            task = new TimerTask() {
                public void run() {
                    reset();
                    postInvalidate();
                }
            };
            timer.schedule(task, time);
        } else {
            reset();
            postInvalidate();
        }
    }

    /**
     * 设置已经选中的为错误
     */
    private void error() {
        for (Point p : sPoints) {
            p.state = Point.STATE_CHECK_ERROR;
        }
    }

    /**
     * 验证设置密码，滑动两次密码是否相同
     * @param indexs
     */
    private void invalidatePass(int[] indexs){
        if(showTimes==0){
            mIndexs=indexs;
            mCompleteListener.onComplete(indexs);
            showTimes++;
            reset();
        }else if(showTimes==1){
            if(mIndexs.length==indexs.length){
                for(int i=0;i<mIndexs.length;i++){
                    if(mIndexs[i]!=indexs[i]){
                        isCorrect=false;
                        break;
                    }
                }
            }else{
                isCorrect=false;
            }
            if(!isCorrect){
                error();
                mCompleteListener.onError();
                postInvalidate();
            }else{
                mCompleteListener.onComplete(indexs);
            }
        }
    }

    /**
     * 验证本地密码与当前滑动密码是否相同
     * @param indexs
     */
    private void invalidateOldPsw(int[] indexs){
        if(mIndexs.length==indexs.length){
            for(int i=0;i<mIndexs.length;i++){
                if(mIndexs[i]!=indexs[i]){
                    isCorrect=false;
                    break;
                }
            }
        }else{
            isCorrect=false;
        }
        if(!isCorrect){
            errorTimes--;
            error();
            mCompleteListener.onError();
            postInvalidate();
        }else{
            mCompleteListener.onComplete(indexs);
        }
    }

    /**
     * 设置监听
     *
     * @param mCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
    }

    /**
     * 轨迹球画完监听事件
     */
    public interface OnCompleteListener {
        /**
         * 画完了
         */
        public void onComplete(int[] indexs);
        /**
         * 绘制错误
         */
        public void onError();
    }

    public int getErrorTimes() {
        return errorTimes;
    }

    public void setErrorTimes(int errorTimes) {
        this.errorTimes = errorTimes;
    }

    public int[] getmIndexs() {
        return mIndexs;
    }

    public void setmIndexs(int[] mIndexs) {
        this.mIndexs = mIndexs;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }
}
