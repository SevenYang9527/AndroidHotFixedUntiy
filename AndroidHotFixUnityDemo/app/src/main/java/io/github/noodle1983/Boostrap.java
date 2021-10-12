package io.github.noodle1983;

public class Boostrap
{
    public static native void init(String filePath);
    public static native  String getarchabi();
    public static native void hook();
    public static native  String usedatadir(String _data_path,String _package_name);
	public static void InitNativeLibBeforeUnityPlay(String filePath)
	{
		System.loadLibrary("main");
        System.loadLibrary("unity");
        System.loadLibrary("bootstrap");
        init(filePath);
    }

}