package cn.beriru.weicoadsremover;

import java.util.Arrays;

import android.util.Log;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ReflectionUtils {
	public static final String TAG = AdsRemoverModule.class.getCanonicalName();
	
	public static Object getObjectFields(Object target,String... fields) throws Throwable{
		try{
			for(String s : fields){
				target = XposedHelpers.getObjectField(target, s);
			}
			return target;
		}catch(Throwable e){
			log(e);
			throw e;
		}
	}
	
	public static void setObjectFields(Object target,Object newValue,String... fields) throws Throwable{
		try{
					
			Object[] objs = Arrays.asList(fields).subList(0, fields.length - 1).toArray();
			String[] prefixes = new String[objs.length];
			for(int i = 0; i < objs.length; i++){
				prefixes[i] = objs[i].toString();
			}
			String field = fields[fields.length - 1];
			Object o = getObjectFields(target,prefixes);
			XposedHelpers.setObjectField(o, field, newValue);
		}catch(Throwable e){
			log(e);
			throw e;
		}
	}
	
	public static void log(String s){
		XposedBridge.log(TAG + s);
	}
	
	public static void log(Throwable e){
		XposedBridge.log(TAG + Log.getStackTraceString(e));
	}
}
