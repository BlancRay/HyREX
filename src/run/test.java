package run;

import java.io.*;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import Kernels.TKOutputGenerator;

public class test {
	private static String HyREX_DIR1 = System.getProperty("user.dir");
	private static String a = "a";
	private static String b = "ab";
	public static void main(String[] args) throws Exception {
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
//		System.out.println("当前时间为：" + df.format(new Date()));// new
//																// Date()为获取当前系统时间
//		args = "a,c, ".split(",");
//		System.out.println(args.length);
//		for (int i = 0; i <= args.length - 1; i++) {
//			System.out.println(args[i]);
//		}
//		System.out.println(HyREX_DIR1);
//		// TKOutputGenerator.main(args);
//		System.out.println("测试成功!");
//		System.out.println(a);
//		File path = new File(HyREX_DIR1 + "/a.txt");
//		path.deleteOnExit();
//		a = "b";
//		System.out.println(a);
//		try {
//			File writename = new File("./output.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件
//			writename.createNewFile(); // 创建新文件
//			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
//			out.write("我会写入文件啦\r\n"); // \r\n即为换行
//			out.flush(); // 把缓存区内容压入文件
//			out.close(); // 最后记得关闭文件
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		String CROSS_FOLD="";
//		System.out.println(CROSS_FOLD);
		
//		String[] p="a,s,d,f,s,d,f".split(",");
		
//		String tmp=p.toString();
//		System.out.println(tmp);
		
//		StringBuilder strBuilder = new StringBuilder();
//		for (int i = 0; i < p.length; i++) {
//		   strBuilder.append( p[i]+" " );
//		}
//		String tmp = strBuilder.toString();
//		System.out.println(tmp);
//		String tmp=Arrays.toString(p);
//		Runtime runtime=Runtime.getRuntime();
//		runtime.exec("cmd /c copy "+a+" "+b);   //copy并改名 

		
		double neg=4;
 		double pos=0;
 		double a=neg/pos;
 		
//	    	try {
//	    		FileReader reader = new FileReader("F:/DDI/sofeware/HyREX/out/test.tk");
//	    		BufferedReader br = new BufferedReader(reader);
//	    		String obj = null;
//	    		
//	    		while ((obj = br.readLine()) != null) {
//	    			char a=' ';
//	    			a=obj.charAt(0);
//	    		if(a=='-'){
//	    			neg=neg+1;
//	    		}else if(a=='1'){
//	    			pos=pos+1;
//	    		}
//	    		}
//	    		br.close();
//	    		reader.close();
//	    		} catch (IOException e) {
//	    		System.out.println("FileTool-->select(String src)-错误");
//	    		}
	    	System.out.println(neg);
	    	System.out.println(pos);
	    	System.out.println(a);
		
		
	}
}