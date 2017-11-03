package aimissu.com.animationinputbox;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

import io.reactivex.functions.Consumer;


/**
 * author：dz-hexiang on 2017/10/30.
 * email：472482006@qq.com
 * 仿ios动画输入框
 */

public  class AnimEditText extends LinearLayout {

    private TextInputEditText mEditText;
    private TextInputLayout mEditTextContainer;

    private AnimatedVectorDrawableCompat mSplitAnim;
    private AnimatedVectorDrawableCompat mMergeAnim;
    private VectorDrawableCompat noAnimBg;

    private String mHit;
    private float mHitSize;
    private int mHitColor;
    private  String mText;
    private float mTextSize;
    private int mTextColor;
    private boolean mIsPwd;
    private int mMaxLength;
    private boolean mIsNumber;


    @SuppressLint("NewApi")
    public AnimEditText(Context context) {
        super(context);
       initView(context,null,-1);
    }


    @SuppressLint("NewApi")
    public AnimEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs,-1);
    }


    @SuppressLint("NewApi")
    public AnimEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context, attrs,defStyleAttr);

    }


    @SuppressLint("NewApi")
    public AnimEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs,defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


    @SuppressLint("NewApi")
    public void initView(Context context, AttributeSet attrs, int defStyleRes)
    {
        LayoutInflater.from(context).inflate(R.layout.view_anim_edit_text, this);
        mEditText = (TextInputEditText) findViewById(R.id.et);
        mEditTextContainer = (TextInputLayout) findViewById(R.id.et_container);



        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.animedittext_style);
        if(typedArray != null){
            //这里要注意，String类型是没有默认值的，所以必须定义好，不然又是空指针大法
            mHit = typedArray.getString(R.styleable.animedittext_style_hit);
            mHitColor = typedArray.getColor(R.styleable.animedittext_style_hitColor, ContextCompat.getColor(context,R.color.login_input_text_color));
            mHitSize = typedArray.getDimension(R.styleable.animedittext_style_hitSize, 13);

            mText = typedArray.getString(R.styleable.animedittext_style_text);
            mTextColor = typedArray.getColor(R.styleable.animedittext_style_textColor, ContextCompat.getColor(context,R.color.login_input_text_color));
            mTextSize = typedArray.getDimensionPixelSize(R.styleable.animedittext_style_textSize, 13);

            mIsPwd = typedArray.getBoolean(R.styleable.animedittext_style_isPwd, false);

            mIsNumber = typedArray.getBoolean(R.styleable.animedittext_style_isNumber, false);

            mMaxLength = typedArray.getInt(R.styleable.animedittext_style_maxLength,0);
        }
        if(!TextUtils.isEmpty(mText))
            mEditText.setText(mText);
        else
            mEditText.setText("");

        mEditText.setTextColor(mTextColor);


        if(mIsPwd)
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);


        if(!TextUtils.isEmpty(mHit))
            mEditTextContainer.setHint(mHit);
        else
            mEditTextContainer.setHint("");

        mEditText.setHintTextColor(mHitColor);

        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,mTextSize);



        if(mIsNumber)
        {
            mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if(mMaxLength >0)
            mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});



//        mSplitAnim = (AnimatedVectorDrawable) ContextCompat.getDrawable(context,R.drawable.login_input_vector_split_anim);
//        mMergeAnim = (AnimatedVectorDrawable) ContextCompat.getDrawable(context,R.drawable.login_input_vector_merge_anim);


        mSplitAnim= AnimatedVectorDrawableCompat.create(context,R.drawable.login_input_vector_split_anim);
        mMergeAnim= AnimatedVectorDrawableCompat.create(context,R.drawable.login_input_vector_merge_anim);

        noAnimBg= VectorDrawableCompat.create(context.getResources(), R.drawable.login_input_no_anim_vector_drawable,null);
        mEditTextContainer.setBackground(noAnimBg);

        mEditText.setOnFocusChangeListener(new AOnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                super.onFocusChange(v, hasFocus);
            }
        });
        mEditText.addTextChangedListener(new ATextWatcher());

    }

    public boolean mIsSplit=false;
    public abstract class AOnFocusChangeListener implements OnFocusChangeListener {




        @SuppressLint("NewApi")
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            setHitNotice();


            if (hasFocus) {
                if(!TextUtils.isEmpty(mEditText.getText().toString()))
                    return;
                /**
                 * 只有当为空值的时候才提示hit，和分开动画
                 */
                mEditTextContainer.setBackground(mSplitAnim);
                Drawable drawable =    mEditTextContainer.getBackground();
                if (drawable instanceof Animatable){
                    ((Animatable) drawable).start();
                    mIsSplit=true;
                }
            }
            else{
                if(!mIsSplit)
                    return;
                /**
                 * 只有当分开的拾柴可以触发合并动画
                 */
                mEditTextContainer.setBackground(mMergeAnim);
                Drawable drawable =    mEditTextContainer.getBackground();
                if (drawable instanceof Animatable){
                    ((Animatable) drawable).start();
                    mIsSplit=false;
                }

            }

        }
    }

    /**
     * 设置hit提示
     * @return
     * 返回true 设置了hit ，表示没有数据
     *
     * 返回false 没有hit提示，表示有数据
     */
    private  boolean setHitNotice()
    {
        String str= mEditText.getText().toString();
        if(!TextUtils.isEmpty(str))
        {
            mEditTextContainer.setHint("");
            return false;
        }

        else
        {
            mEditTextContainer.setHint(mHit);
            return true;
        }
    }
    public  class ATextWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @SuppressLint("NewApi")
        @Override
        public void afterTextChanged(Editable s) {

            /**
             *没有数据 并且合并 ，应该进行分开动画给出提示
             * 为了增加体验延迟设置hit
             */
            if(TextUtils.isEmpty(mEditText.getText().toString())&&!mIsSplit)
            {
                mEditTextContainer.setBackground(mSplitAnim);
                Drawable drawable =    mEditTextContainer.getBackground();
                if (drawable instanceof Animatable){
                    ((Animatable) drawable).start();
                    mIsSplit=true;
                }
                Flowable.timer(350, TimeUnit.MILLISECONDS)
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(@NonNull Long aLong) throws Exception {
                                mEditTextContainer.setHint(mHit);
                            }
                        });

            }
            /**
             * 如果有数，但是分开着，应该进行合并动画，并且清楚hit
             */

            if(!TextUtils.isEmpty(mEditText.getText().toString())&&mIsSplit)
            {
                mEditTextContainer.setBackground(mMergeAnim);
                Drawable drawable =    mEditTextContainer.getBackground();
                if (drawable instanceof Animatable){
                    ((Animatable) drawable).start();
                    mIsSplit=false;
                }
                Flowable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(@NonNull Long aLong) throws Exception {
                                mEditTextContainer.setHint("");
                            }
                        });
            }



        }
    }


    public void setOnFocusChangeListener(AOnFocusChangeListener aOnFocusChangeListener)
    {
        if(aOnFocusChangeListener!=null)
            mEditText.setOnFocusChangeListener(aOnFocusChangeListener);
    }

    public void addTextChangedListener(ATextWatcher aTextWatcher)
    {
        if(aTextWatcher!=null)
            mEditText.addTextChangedListener(aTextWatcher);
    }

    public String getText()
    {
        return mEditText.getText().toString();
    }
    public void setText(String str)
    {
        mEditText.setText(str);
    }

    public void setmHit(String mHit)
    {
        this.mHit = mHit;
        mEditTextContainer.setHint(mHit);
    }




}
