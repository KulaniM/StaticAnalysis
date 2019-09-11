package android.hook.fileapi;


import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import com.google.android.apps.chromecast.app.n.k;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class HookAPI implements IXposedHookLoadPackage, Runnable {

    public XC_LoadPackage.LoadPackageParam loadPackageParam = null;

    @Override
    public void run() {
        while (true) {
            try {

               if (!loadPackageParam.packageName.equals("com.google.android.apps.chromecast.app"))
                    return;
                Log.v("xposed1", "com.google.android.apps.chromecast.app");


//                XposedHelpers.findAndHookConstructor("com.lifx.lifx.util.NetworkUtil", loadPackageParam.classLoader, Client.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        Log.v("xposed1", "constructor ");
//                    }
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Log.v("xposed1", "constructor ");
//                        //Object [] parameter = param.args;
//                        //String filePath,name = null;
//                        //filePath = (String)parameter[0];
//                     }
//                });
//

//                XposedHelpers.findAndHookConstructor("com.lifx.core.util.socket.SocketUDP", loadPackageParam.classLoader,String.class, int.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        Log.v("xposed1", "SocketUDP constructor ***********");
//                    }
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Log.v("xposed1", "SocketUDP constructor ************ ");
//                        //Object [] parameter = param.args;
//                        //String filePath,name = null;
//                        //filePath = (String)parameter[0];
//                     }
//                });
//
//                XposedHelpers.findAndHookConstructor("com.lifx.core.util.socket.SocketUDP", loadPackageParam.classLoader, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        Log.v("xposed1", "SocketUDP constructor ***********");
//                    }
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Log.v("xposed1", "SocketUDP constructor ************ "+ param.args[0]);
//                        //Object [] parameter = param.args;
//                        //String filePath,name = null;
//                        //filePath = (String)parameter[0];
//                    }
//                });
//

                findAndHookMethod("com.google.android.apps.chromecast.app.cp", loadPackageParam.classLoader, "a", "android.util.SparseArray", "com.google.android.apps.chromecast.app.n.k", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                       Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+param.args[0]);
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+param.args[1]);
                        Object x = XposedHelpers.newInstance(com.google.android.apps.chromecast.app.n.k.class, param.args[1]);
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ x);
                        com.google.android.apps.chromecast.app.n.k y = (com.google.android.apps.chromecast.app.n.k)x;
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y );
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.a());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.b());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.c());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.d());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.e());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.f());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.g());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.h());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.i());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.j());
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.cp########## a ################# ->"+ y.k());

                    }
                });



                findAndHookMethod("com.google.android.apps.chromecast.app.n.k", loadPackageParam.classLoader, "a", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Log.v("xposed1", "com.google.android.apps.chromecast.app.n.k########## a ################# ->"+param.args[0]);
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.n.k########## a ################# ->"+param.args[1]);
                        Log.v("xposed1", "com.google.android.apps.chromecast.app.n.k########## results ################# ->"+param.getResult());



                    }
                });

                findAndHookMethod("com.google.android.gms.common.internal.n", loadPackageParam.classLoader, "handleMessage", "android.os.Message", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Log.v("xposed1", "com.google.android.gms.common.internal.n########## handleMessage ################# ->"+param.args[0]);
                        param.setResult(null);



                    }
                });

                break;
            } catch (NoSuchMethodError | NoSuchFieldError nsme) {
                Log.v("xposed1", "from thread: " + nsme.toString());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException itre) {
                    continue;
                }
            }

            catch (Exception e) {
                Log.v("xposed1", "from thread: " + e.toString());
                break;
            }
        }
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        this.loadPackageParam = lpparam;

//======================================================================
// thread
//======================================================================
//        Thread t = new Thread(new HookAPI(param));
//        t.start();
        new Thread(this, "xposed1").start();
//======================================================================
    }


}


