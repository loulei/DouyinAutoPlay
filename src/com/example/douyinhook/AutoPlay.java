package com.example.douyinhook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class AutoPlay implements IXposedHookLoadPackage {
	static Object verticalViewPager = null;
	static long time = 0;
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		// TODO Auto-generated method stub
		if("com.ss.android.ugc.aweme".equals(lpparam.packageName) && "com.ss.android.ugc.aweme".equals(lpparam.processName)){
			System.out.println("douyin launch");

			
			
			XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					// TODO Auto-generated method stub
					if(param.hasThrowable())
						return;
					Class<?> clazz = (Class<?>) param.getResult();
					if(clazz != null){
						String classname = clazz.getName();
						if("tv.danmaku.ijk.media.player.AbstractMediaPlayer".equals(classname)){
							System.out.println("tv.danmaku.ijk.media.player.AbstractMediaPlayer loaded");
							hookPlayerFinish(clazz);
						}else if("com.ss.android.ugc.aweme.common.widget.VerticalViewPager".equals(classname)){
							System.out.println("com.ss.android.ugc.aweme.common.widget.VerticalViewPager loaded");
							hookOnTouchEvent(clazz);
						}
					}
				}
			});
		}
	}
	
	private void hookOnTouchEvent(Class<?> viewClazz) throws Throwable{
		XposedHelpers.findAndHookMethod(viewClazz, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// TODO Auto-generated method stub
				if(verticalViewPager == null){
					verticalViewPager = param.thisObject;
				}
				MotionEvent event = (MotionEvent) param.args[0];
				System.out.println("onTouchEvent:"+event.getAction()+","+event.getX()+","+event.getY());
			}
		});
	}
	
	
	private void hookPlayerFinish(Class<?> playerClazz) throws Throwable{
		XposedHelpers.findAndHookMethod(playerClazz, "notifyOnSeekComplete", new XC_MethodHook() {
			
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// TODO Auto-generated method stub
				System.out.println("notifyOnSeekComplete");
				if(verticalViewPager == null){
					return;
				}
				Method getCurrentItemMd = verticalViewPager.getClass().getDeclaredMethod("getCurrentItem");
				final Method setCurrentItemMd = verticalViewPager.getClass().getDeclaredMethod("setCurrentItem", int.class);
				if(System.currentTimeMillis() - time > 5000){
					final int currentItem = (int) getCurrentItemMd.invoke(verticalViewPager);
					System.out.println("currentItem:"+currentItem);
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								setCurrentItemMd.invoke(verticalViewPager, currentItem+1);
							} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					
				}
				time = System.currentTimeMillis();
			}
		});
	}
	

}
