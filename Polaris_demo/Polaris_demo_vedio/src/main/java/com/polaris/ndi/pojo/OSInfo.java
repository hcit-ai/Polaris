package com.polaris.ndi.pojo;

public class OSInfo {
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	private static OSInfo _instance = new OSInfo();
	
	private EnumPlatform platform;
	
	private OSInfo(){}
	
	public static boolean isLinux(){
		return OS.indexOf("linux")>=0;
	}
	
	public static boolean isMacOS(){
		return OS.indexOf("mac")>=0&&OS.indexOf("os")>0&&OS.indexOf("x")<0;
	}
	
	public static boolean isMacOSX(){
		return OS.indexOf("mac")>=0&&OS.indexOf("os")>0&&OS.indexOf("x")>0;
	}
	
	public static boolean isWindows(){
		return OS.indexOf("windows")>=0;
	}
	
	public static boolean isOS2(){
		return OS.indexOf("os/2")>=0;
	}
	
	public static boolean isSolaris(){
		return OS.indexOf("solaris")>=0;
	}
	
	public static boolean isSunOS(){
		return OS.indexOf("sunos")>=0;
	}
	
	public static boolean isMPEiX(){
		return OS.indexOf("mpe/ix")>=0;
	}
	
	public static boolean isHPUX(){
		return OS.indexOf("hp-ux")>=0;
	}
	
	public static boolean isAix(){
		return OS.indexOf("aix")>=0;
	}
	
	public static boolean isOS390(){
		return OS.indexOf("os/390")>=0;
	}
	
	public static boolean isFreeBSD(){
		return OS.indexOf("freebsd")>=0;
	}
	
	public static boolean isIrix(){
		return OS.indexOf("irix")>=0;
	}
	
	public static boolean isDigitalUnix(){
		return OS.indexOf("digital")>=0&&OS.indexOf("unix")>0;
	}
	
	public static boolean isNetWare(){
		return OS.indexOf("netware")>=0;
	}
	
	public static boolean isOSF1(){
		return OS.indexOf("osf1")>=0;
	}
	
	public static boolean isOpenVMS(){
		return OS.indexOf("openvms")>=0;
	}
	
	/**
	 * 获取操作系统名字
	 * @return 操作系统名
	 */
	public static EnumPlatform getOSname(){
		if(isAix()){
			_instance.platform = EnumPlatform.AIX;
		}else if (isDigitalUnix()) {
			_instance.platform = EnumPlatform.Digital_Unix;
		}else if (isFreeBSD()) {
			_instance.platform = EnumPlatform.FreeBSD;
		}else if (isHPUX()) {
			_instance.platform = EnumPlatform.HP_UX;
		}else if (isIrix()) {
			_instance.platform = EnumPlatform.Irix;
		}else if (isLinux()) {
			_instance.platform = EnumPlatform.Linux;
		}else if (isMacOS()) {
			_instance.platform = EnumPlatform.Mac_OS;
		}else if (isMacOSX()) {
			_instance.platform = EnumPlatform.Mac_OS_X;
		}else if (isMPEiX()) {
			_instance.platform = EnumPlatform.MPEiX;
		}else if (isNetWare()) {
			_instance.platform = EnumPlatform.NetWare_411;
		}else if (isOpenVMS()) {
			_instance.platform = EnumPlatform.OpenVMS;
		}else if (isOS2()) {
			_instance.platform = EnumPlatform.OS2;
		}else if (isOS390()) {
			_instance.platform = EnumPlatform.OS390;
		}else if (isOSF1()) {
			_instance.platform = EnumPlatform.OSF1;
		}else if (isSolaris()) {
			_instance.platform = EnumPlatform.Solaris;
		}else if (isSunOS()) {
			_instance.platform = EnumPlatform.SunOS;
		}else if (isWindows()) {
			_instance.platform = EnumPlatform.Windows;
		}else{
			_instance.platform = EnumPlatform.Others;
		}
		return _instance.platform;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(OSInfo.getOSname());
	}
}
