package cn.beriru.weicoadsremover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ReflectionUtils {
	public static final String TAG = AdsRemoverModule.class.getCanonicalName();
	
	/**
	 * support dot and recursive notation
	 * @param target
	 * @param fields
	 * @return
	 * @throws Throwable
	 */
	public static Object getObjectFields(Object target,String... fields) throws Throwable{
		try{
			List<String> compoundFields = new ArrayList<String>();
			for(String f : fields){
				compoundFields.addAll(Arrays.asList(f.split("\\.")));
			}
			
			for(String s : compoundFields){
				target = XposedHelpers.getObjectField(target, s);
			}
			return target;
		}catch(Throwable e){
			log(e);
			throw e;
		}
	}
	
	/**
	 * add support dot and recursive notation
	 * @param target
	 * @param newValue
	 * @param fields
	 * @throws Throwable
	 */
	public static void setObjectFields(Object target,Object newValue,String... fields) throws Throwable{
		try{
			if(fields.length < 1){
				throw new RuntimeException("field should not be null");
			}
			
			List<String> tokens = new ArrayList<String>();
			for(String tokenItem : fields){
				tokens.addAll(Arrays.asList(tokenItem.split("\\.")));
			}
			
			String setField = tokens.get(tokens.size() - 1);
			String[] prefixes = new String[]{};
			if(tokens.size() > 1){
				prefixes = tokens.subList(0, tokens.size() - 1).toArray(new String[0]);
			}
			
			Object o = getObjectFields(target,prefixes);
			XposedHelpers.setObjectField(o, setField, newValue);
			
		}catch(Throwable e){
			log(e);
			throw e;
		}
	}
	
	public static void log(String s){
		XposedBridge.log(TAG + " : " + s);
	}
	
	public static void log(Throwable e){
		XposedBridge.log(TAG + " : " + Log.getStackTraceString(e));
	}
}
