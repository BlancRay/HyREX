package run;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Generated;

import Kernels.*;
import Clause.*;
import DataProcessor.*;
import negation.*;

import org.fbk.it.hlt.bioRE.*;
import org.fbk.it.hlt.bioRE.multiStage.*;
import org.omg.CORBA.PRIVATE_MEMBER;

import com.sun.org.apache.bcel.internal.generic.FNEG;

import sun.security.util.Length;
import Others.*;
import Structures.*;
import Utility.*;

public class run {
	// 设置环境变量
//	static String HyREX_DIR = System.getProperty("user.dir");
	static String HyREX_DIR ="F:/DDI/sofeware/HyREX";
	static String DATA_HOME = HyREX_DIR + "/sample-data";
	static String SVM_LIGHT_TK = HyREX_DIR + "/SVM-Light-TK-1.5";
	static String OUT_DIR = HyREX_DIR + "/out";
	static String OUT_FILE = OUT_DIR + "/output.txt";
	static String PRED_FILE = SVM_LIGHT_TK + "/svm_predictions";
	static String TRACE_FILE = OUT_DIR + "/trace";
	static String LIB_DIR = HyREX_DIR + "/lib";
	static String CROSS_FOLD = "-foldFilesFolder  $DATA_HOME/folds";
	static String BEST_RES_FILE = OUT_DIR + "/best_tuned_result_ddi_sen";
	static String All_PRED_FILE = OUT_DIR + "/base.stat.in_ddi_sen";
	
	// 设置默认参数
	static String TRAIN_DATA_FULL = "-train  $DATA_HOME/sample.full";
	static String TRAIN_DATA_PARSED = "-trainParse $DATA_HOME/sample.parsed.bllip.complete";
	static String HELDOUT_DATA_FULL = "-test  $DATA_HOME/sample.full";
	static String HELDOUT_DATA_PARSED = "-testParse $DATA_HOME/sample.parsed.bllip.complete";
	static String TEST_DATA_FULL = "-test  $DATA_HOME/sample.full";
	static String TEST_DATA_PARSED = "-testParse $DATA_HOME/sample.parsed.bllip.complete";
	static String CR = "-icrTraini -icrTest";
	static String CB = "-cbTrain $DATA_HOME/$CORP/edu_seg/$CORP.parsed.bllip.seg";
	static String JVM_ARGS = "-Xmx1048m  -XX:MaxPermSize=512m -cp ./bin:$LIB_DIR/jsre.jar:$LIB_DIR/log4j-1.2.8.jar:$LIB_DIR/commons-digester.jar:$LIB_DIR/commons-collections.jar:$LIB_DIR/commons-logging.jar:$LIB_DIR/commons-beanutils.jar:$LIB_DIR/jutil.jar:$LIB_DIR/libsvm.jar:$LIB_DIR/bioRelEx.jar:$LIB_DIR/stanford-parser-2012-03-09-models.jar:$LIB_DIR/stanford-parser.jar";
	
	static int m = 1024;
	static int NO_OF_FOLDS = 1;
	static String JSRE = "-jsre"; /*
								 * Possible value: "-jsre" (Giuliano, Lavelli
								 * and Romano, EACL 2006)
								 */
	static String KERNEL_JSRE = "SL"; /*
									 * Possible value: "SL", "LC" and "GC" --
									 * all of them are vector based kernels
									 * (Giuliano, Lavelli and Romano, EACL 2006)
									 */

	static String DT = "-wv";/*
							 * #-zhou2005" 	# Possible values: "-dt" and "-wv".
							 * # "-dt" is for MEDT tree kernel compuation
							 * (Chowdhury, Lavelli and Moschitti, BioNLP 2011) #
							 * "-wv" is for TPWF vector-based kernel compuation
							 * (Chowdhury and Lavelli, EACL 2012)
							 */
	static String PST = "-pst";/*
								 * Possible value: "-pst" for PET tree kernel
								 * computation (Moschitti, ACL 2004; Chowdhury
								 * and Lavelli, EACL 2012)
								 */
	static double T;
	static int t;
	static String C;/* + */
	static int F;
	static double cost;
	static int b;
	static double lambda;
	static double mu;
	static int d; /* parameter d in polynomial kernel: (s a*b+c)^d */
	static int U;
	static double best_lambda;
	static double best_mu;
	static double best_cost;
	static int best_d;
	static double prev_f1;
	static int f1;
	static int cont;
	static double offset;
	static int max_iter_sub_cost;
	static int max_iter_sub_lambda;
	static int max_iter_add;
	static int findNegatedSentence;
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式

	public static void main(String[] args) throws Exception {

		CROSS_FOLD = "";
		// 初始化SVM-Light-TK参数
		// values of 't':
		// 5 = 1 tree + 1 vector kernels
		// 0 = 1 vector kernel (linear)
		// 50 = 2 vector kernels
		// 502 = 2 vector + 1 tree kernels
		// set parameter values here
		T = 1.0;
		t = 0;
		C = "+";/* + */
		F = 4;
		cost = 0.2;
		b = 1;
		lambda = 0.4;
		mu = 0.4;
		d = 2; /* parameter d in polynomial kernel: (s a*b+c)^d */
		U = 0;
		best_lambda = lambda;
		best_mu = mu;
		best_cost = cost;
		best_d = d;
		prev_f1 = 0.0;
		f1 = 1;
		cont = 1;
		offset = 1.0;
		max_iter_sub_cost = 1;
		max_iter_sub_lambda = 2;
		max_iter_add = 25;

		// 生成识别的训练和测试实例
		args = "-medtType,6,-trainParse,F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete,-train,F:/DDI/sofeware/HyREX/sample-data/sample.full,-testParse,F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete,-test,F:/DDI/sofeware/HyREX/sample-data/sample.full,,,-nf,1,-trigFile,F:/DDI/sofeware/HyREX/db/ddi_trigger,-classifySentences"
				.split(",");
		// System.out.println(args);
		// cd $HyREX_DIR
		System.out.println("开始生成实例！");
		
		System.out.println("当前时间为：" + df.format(new Date()));// new
																// Date()为获取当前系统时间
		String starttime = df.format(new Date());
		try {
			System.out.println("TK Start..........");
			System.out.println("args="+args.length);
			String[] args1="-medtType 6 -trainParse F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete -train F:/DDI/sofeware/HyREX/sample-data/sample.full -testParse F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete -test F:/DDI/sofeware/HyREX/sample-data/sample.full -nf 1 -trigFile F:/DDI/sofeware/HyREX/db/ddi_trigge -classifySentences".split("\\s+");
			TKOutputGenerator.main(args1);
			System.out.println("TK End..........");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			File writename = new File(TRACE_FILE); // 相对路径，如果没有则要建立一个新的txt文件
			writename.createNewFile(); // 创建新文件
			System.out.println("create TRACE_FILE");
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(""); // \r\n即为换行
			out.flush(); // 把缓存区内容压入文件
			out.close(); // 最后记得关闭文件
			System.out.println("输出"+TRACE_FILE);

		} catch (Exception e) {
			e.printStackTrace();
		}
			findNegatedSentence = 1;
			System.out.println("sen_fun  in!!");
			sen_run.fnExp(t);
			System.out.println("sen_run  out!!");
			// sen_run();

			// ==============================================================

			NO_OF_FOLDS = 1;
			BEST_RES_FILE = OUT_DIR + "/best_tuned_result";
			All_PRED_FILE = OUT_DIR + "/base.stat.in";

			// 初始化核函数所要用到的参数

			// static String JSRE = "-jsre"; /*
			// * Possible value: "-jsre" (Giuliano,
			// * Lavelli and Romano, EACL 2006)
			// */
			// static String KERNEL_JSRE = "SL"; /*
			// * Possible value: "SL", "LC" and "GC"
			// * -- all of them are vector based
			// * kernels (Giuliano, Lavelli and
			// * Romano, EACL 2006)
			// */
			//
			// static String DT = "-wv";/*
			// * #-zhou2005" 	# Possible values: "-dt" and "-wv
			// * ". # "-dt" is for MEDT tree kernel compuation
			// * (Chowdhury, Lavelli and Moschitti, BioNLP
			// * 2011) # "-wv" is for TPWF vector-based kernel
			// * compuation (Chowdhury and Lavelli, EACL 2012)
			// */
			// static String PST = "-pst";/*
			// * Possible value: "-pst" for PET tree
			// * kernel computation (Moschitti, ACL 2004;
			// * Chowdhury and Lavelli, EACL 2012)
			// */

			// 初始化SVM-Light-TK参数
			// values of 't':
			// 5 = 1 tree + 1 vector kernels
			// 0 = 1 vector kernel (linear)
			// 50 = 2 vector kernels
			// 502 = 2 vector + 1 tree kernels
			// set parameter values here
			// T = 1.0;
			t = 502;
			// C = "+";/* + */
			// F = 4;
			// cost = 0.2;
			// b = 1;
			// lambda = 0.4;
			// mu = 0.4;
			// d = 2; /* parameter d in polynomial kernel: (s a*b+c)^d */
			// U = 0;
			//
			// best_lambda = lambda;
			// best_mu = mu;
			// best_cost = cost;
			// best_d = d;
			//
			// prev_f1 = 0.0;
			// f1 = 1;
			// cont = 1;
			// offset = 1.0;
			//
			// max_iter_sub_cost = 1;
			// max_iter_sub_lambda = 2;
			// max_iter_add = 25;

			// 生成关系抽取的训练与测试实例

			// 生成参数
			args = "-pst,-jsre,-wv,-kjsre,SL,-medtType,6,-trainParse,F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete,-train,F:/DDI/sofeware/HyREX/sample-data/sample.full,-testParse,F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete,-test,F:/DDI/sofeware/HyREX/sample-data/sample.full,,,,-nf,1,-trigFile,F:/DDI/sofeware/HyREX/db/ddi_trigger"
					.split("，");
			System.out.println("234: 生成关系抽取的训练与测试实例");
			try {
				System.out.println("Start..............");
				String[] args2 = "-pst -jsre -wv -kjsre SL -medtType 6 -trainParse F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete -train F:/DDI/sofeware/HyREX/sample-data/sample.full -testParse F:/DDI/sofeware/HyREX/sample-data/sample.parsed.bllip.complete -test F:/DDI/sofeware/HyREX/sample-data/sample.full -nf 1 -trigFile F:/DDI/sofeware/HyREX/db/ddi_trigger".split("\\s+");
				TKOutputGenerator.main(args2);
				System.out.println("End..............");
			} catch (Exception e) {
				e.printStackTrace();
			}

			findNegatedSentence = 0;
			System.out.println("fnExp   Start..............");
			sen_run.fnExp(t);
			System.out.println("End..............");
//			 sen_run();
			System.out.println("");
			System.out.println(df.format(new Date())+"-> All processing are done.(Started at "+starttime+")");
			System.out.println("");
			System.out
					.println("Results of evaluation can be found in "+OUT_DIR+"/output.txt.");
			System.out.println("");

			// 删除临时文件
			String[] f = { "/entPairFileName_DT", "/entPairFileName_JSRE",
					"/entPairFileName_WV", "/entPairFileName_PST",
					"*.parsed.*", "model", "/base.stat.in",
					"/extracted_relations.txt", "/all_vect_by_pair", "/tk",
					"/train.*", "/test.*", "/tpwf.*", "PRED_FILE" };
			for (int i = 0; i <= f.length-1; i++) {
				File path = new File(OUT_DIR + f[i]);
				path.deleteOnExit();
			}
		}
	}