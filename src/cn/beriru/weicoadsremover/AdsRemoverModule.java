package cn.beriru.weicoadsremover;

import java.sql.Ref;
import java.util.ArrayList;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static cn.beriru.weicoadsremover.ReflectionUtils.log;


public class AdsRemoverModule implements IXposedHookLoadPackage {

	public static class WEICO {
		public static final String PACKAGE_NAME = "com.eico.weico";
		public static final String HOOK_FUNC_SETSTATUS = "setcStatusList";
		public static final String HOOK_CLASS_NAME = "com.eico.weico.adapter.TimeLineAdapterOfText";
		public static final String HOOK_FUNC_NOTIFY = "notifyDataSetChanged";
	}
	
	
	public static class BUKA {
		public static final String PACKAGE_NAME = "cn.ibuka.manga.ui";
		public static final String HOOK_CLASS_NAME = "cn.ibuka.manga.ui.ActivityShake2";
		public static final String HOOK_FUNCNAME = "h";
		
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lp) throws Throwable{
		if(lp.packageName.equals(WEICO.PACKAGE_NAME)){
			log("findpackage: " + WEICO.PACKAGE_NAME); 
			XposedHelpers.findAndHookMethod(WEICO.HOOK_CLASS_NAME,lp.classLoader,WEICO.HOOK_FUNC_SETSTATUS,ArrayList.class,new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					log("hooking" + WEICO.HOOK_FUNC_SETSTATUS);
					ArrayList<Object> statusList = (ArrayList<Object>) param.args[0];
					int beforeSize = statusList.size();
					log("original params size: " + beforeSize);
					statusList	= filterAds(statusList);
					param.args[0] = statusList;
					log("after params size: " + statusList.size());
					if(beforeSize != statusList.size()){
						log("goodjob ");
					}
					super.beforeHookedMethod(param);
				}
			});
			
			
			XposedHelpers.findAndHookMethod(WEICO.HOOK_CLASS_NAME, lp.classLoader, WEICO.HOOK_FUNC_NOTIFY, new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					log("hookingnotifybegin");
					ArrayList<Object> status = (ArrayList<Object>) ReflectionUtils.getObjectFields(param.thisObject, "cDataProvider","cStatuses");
					if(status != null){
						log("beforecount:" + status.size());
						status = filterAds(status);
						log("aftercount:" + status.size());
						ReflectionUtils.setObjectFields(param.thisObject,status,"cDataProvider","cStatuses");
					}else{
						log("count: null");
					}
					super.beforeHookedMethod(param);
				}
			});
		}else if(lp.packageName.equals(BUKA.PACKAGE_NAME)){
			log("hookingbuka");
			XposedHelpers.findAndHookMethod(BUKA.HOOK_CLASS_NAME, lp.classLoader, BUKA.HOOK_FUNCNAME, new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					log("setfielda=0");
					XposedHelpers.setIntField(param.thisObject, "a", 0);
					super.beforeHookedMethod(param);
					log("done");
				}
			});
		}
	}
	
	
	public ArrayList<Object> filterAds(ArrayList<Object> statusList){
		if(statusList == null){
			return null;
		}
		
		
		ArrayList<Object> items = new ArrayList<Object>();
		for(Object o : statusList){
			Object result = XposedHelpers.callMethod(o, "isIsad", new Object[]{});
			Boolean isAd = (Boolean) result;
			if(!isAd){
				items.add(o);
			}
		}
		return items;
	}
	
	
}
