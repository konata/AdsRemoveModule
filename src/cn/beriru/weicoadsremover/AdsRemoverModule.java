package cn.beriru.weicoadsremover;

import static cn.beriru.weicoadsremover.ReflectionUtils.log;

import java.util.ArrayList;

import android.os.Bundle;

import com.meizu.mstore.license.LicenseResult;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


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
		public static final String FUNCNAME_C = "c";
		public static final String FUNCNAME_ONCREATE = "onCreate";
	}
	
	public static class MEIZU {
		public static final String HOOK_CLASSNAME_RESULT = "com.meizu.mstore.license.LicenseResult";
		public static final String HOOK_CLASSNAME_CHECKER = "com.meizu.mstore.license.LicenseCheckHelper";
		
		public static final String HOOK_FUNCNAME_GETRESULT_CODE = "getResponseCode"; // (void)
		public static final String HOOK_FUNCNAME_CHECKRESULT = "checkResult"; // (String ,LicenseResult)
		public static final String HOOK_FUNCNAME_PURCHASETYPE = "getPurchaseType"; // (void)
		
		public static final int RESP_SUCC = 1;
		public static final int PURCHASE_TYPE = 1;
		
		// LicenseResult result = mLicensingService.checkLicense(pkgName); 						    // hook to not null (skip)
		// if( result != null && result.getResponseCode() == LicenseResult.RESPONSE_CODE_SUCCESS){  // hook getResponseCode == SUCC
		// LicenseCheckHelper.checkResult(APP_KEY, result);  										// hook result to true
		// result.getPurchaseType() == LicenseResult.PURCHASE_TYPE_NORMAL 							// hook type == normal

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
			
			
			
			/** hooking source
			 * if(Math.abs(v0 - this.z) > 1000 && !this.D) {
			 */
			log("hookingmethodsc");
			XposedHelpers.findAndHookMethod(BUKA.HOOK_CLASS_NAME, lp.classLoader, BUKA.FUNCNAME_C, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					log("hookingc");
					XposedHelpers.setIntField(param.thisObject, "z", -1001);
					XposedHelpers.setBooleanField(param.thisObject, "D", false);
					super.beforeHookedMethod(param);
					log(new RuntimeException("tianyeshigedashabi"));
					log("donehooingc");
				}
			});
			
			/** hooking source
			 *  protected void onCreate(Bundle arg11) {
			 */
			log("hookingOnCreate");
			XposedHelpers.findAndHookMethod(BUKA.HOOK_CLASS_NAME, lp.classLoader, BUKA.FUNCNAME_ONCREATE, Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					log("hookingOnCreateBegin");
					super.afterHookedMethod(param);
					Object o = ReflectionUtils.getObjectFields(param.thisObject, "n");
					XposedHelpers.callMethod(o, "removeAllViews");
					XposedHelpers.callMethod(param.thisObject, BUKA.FUNCNAME_C);
					log("doneOnCreate");
				}
			});
		}

	
		/**
		 * meizu license result check hooking
		 */
		// LicenseResult result = mLicensingService.checkLicense(pkgName); 						    // hook to not null (skip)
		// if( result != null && result.getResponseCode() == LicenseResult.RESPONSE_CODE_SUCCESS){  // hook getResponseCode == SUCC
		// LicenseCheckHelper.checkResult(APP_KEY, result);  										// hook result to true
		// result.getPurchaseType() == LicenseResult.PURCHASE_TYPE_NORMAL 							// hook type == normal
		
		if(shouldHookMeizuChecker(lp)){
			try{
				log("meizu hook begin");
				Class<?> LicenseResult = XposedHelpers.findClass(MEIZU.HOOK_CLASSNAME_RESULT, lp.classLoader);
				log("meizu hook getresult");
				XposedHelpers.findAndHookMethod(LicenseResult, MEIZU.HOOK_FUNCNAME_GETRESULT_CODE, new XC_MethodHook(){
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						super.afterHookedMethod(param);
						log("meizu hook getresult to 1");
						param.setResult(MEIZU.RESP_SUCC);
					}
				});

				log("meizu hook purchase type");
				XposedHelpers.findAndHookMethod(LicenseResult, MEIZU.HOOK_FUNCNAME_PURCHASETYPE, new XC_MethodHook(){
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						super.afterHookedMethod(param);
						log("meizu hook purchase type to 1");
						param.setResult(MEIZU.PURCHASE_TYPE);
					}
				});

				log("meizu hook checkresult");
				XposedHelpers.findAndHookMethod(MEIZU.HOOK_CLASSNAME_CHECKER, lp.classLoader, MEIZU.HOOK_FUNCNAME_CHECKRESULT, String.class,LicenseResult,new XC_MethodHook(){
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						super.afterHookedMethod(param);
						log("meizu hook checkresult to true");
						param.setResult(true);
					}
				});
			}catch(Exception e){
				log(e);
				throw e;
			}
			log("meizu hook end");
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
	
	
	public static boolean shouldHookMeizuChecker(LoadPackageParam param){
		try{
			XposedHelpers.findClass(MEIZU.HOOK_CLASSNAME_RESULT, param.classLoader);
			log("begin hook meizu for package " + param.packageName + " ❤~~♪~~");
			return true;
		}catch(ClassNotFoundError cne){
			return false;
		}
	}
	
}
