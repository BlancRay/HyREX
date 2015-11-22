package run;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import Kernels.TKOutputGenerator;
import Utility.CommonUtility;

public class sen_run {
	static String HyREX_DIR = "F:/DDI/sofeware/HyREX";
	static String DATA_HOME = HyREX_DIR + "/sample-data";
	static String SVM_LIGHT_TK = HyREX_DIR + "/SVM-Light-TK-1.5";
	static String OUT_DIR = HyREX_DIR + "/out";
	static String OUT_FILE = OUT_DIR + "/output.txt";
	static String PRED_FILE = SVM_LIGHT_TK + "/svm_predictions";
	static String TRACE_FILE = OUT_DIR + "/trace";
	static String LIB_DIR = HyREX_DIR + "/lib";
	static String BEST_All_PRED_FILE = OUT_DIR + "/best.base.stat.in";
	static String BEST_RES_FILE = OUT_DIR + "/best_tuned_result_ddi_sen";
	static String All_PRED_FILE = OUT_DIR + "/base.stat.in_ddi_sen";
	static String CROSS_FOLD = "";/*-foldFilesFolder  $DATA_HOME/folds";*/
	static String TRAIN_DATA_FULL = "-train  $DATA_HOME/sample.full";
	static String CORPUS_FILES_DIR = OUT_DIR + "/tk";

	static int m = 1024;
	static double T = 1.0;
	static int t = 0;
	static String C = "+";/* + */
	static int F = 4;
	static double cost = 0.2;
	static int b = 1;
	static double lambda = 0.4;
	static double mu = 0.4;
	static int d = 2; /* parameter d in polynomial kernel: (s a*b+c)^d */
	static int U = 0;
	static double best_lambda = lambda;
	static double best_mu = mu;
	static double best_cost = cost;
	static double best_d = d;
	static double prev_f1 = 0.0;
	static double f1 = 1;
	static int cont = 1;
	static double offset = 1.0;
	static int max_iter_sub_cost = 1;
	static int max_iter_sub_lambda = 2;
	static int max_iter_add = 25;
	static int NO_OF_FOLDS = 1;
	static int max_iter_sub;
	static int findNegatedSentence = run.findNegatedSentence;
	static String PST = "-pst";

	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式

	// System.out.println("当前时间为：" + df.format(new Date()));// new
	// // Date()为获取当前系统时间

	public static void writte(String filename, String str) {
		try {
			System.out.println("senrun 57");
			File writename = new File(filename); // 相对路径，如果没有则要建立一个新的output.txt文件
			writename.createNewFile(); // 创建新文件
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(str + "\n"); // \r\n即为换行
			out.flush(); // 把缓存区内容压入文件
			out.close(); // 最后记得关闭文件
			System.out.println("senrun 64");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void fnExp(int t2) {
		System.out.println("fnExp  in");
		if (CROSS_FOLD == "") {
			System.out.println("CROSS_FOLD =null");
			if (t2 == 50) {
				System.out.println("t==50");
				fnOptimize_poly_d();
			}
System.out.println("t!=50");
			fnSelectStartingCostValue();
			System.out.println("fnSelectStartingCostValue   out");
			if (PST != "") {
				int optL = 3;
				max_iter_sub = max_iter_sub_lambda;
				fnOptimizePARAM(optL);
				writte(TRACE_FILE, "lambda " + lambda);
				max_iter_sub = 2;
				// Optimize the mu parameter
				int optM = 2;
				fnOptimizePARAM(optM);
				writte(TRACE_FILE, "mu " + mu);
			}
			// Optimize the c parameter
			int optC = 1;
//			int max_iter_sub = max_iter_sub_cost;

			fnOptimizePARAM(optC);
			writte(TRACE_FILE, "cost " + cost);
			if (PST != "") // -n tests to see if the argument is non empty
				fnOptimizeT();
			else
				fnRun(t, C, F, cost, b, lambda, mu, m, U, T, "L", d);
			if (findNegatedSentence > 0) {
				String[] args = "F:/DDI/sofeware/HyREX/out/best.base.stat.in out/allSenIdsForTest neg"
						.split("\\s+");
				try {
					negation.NegatedSentenceAnalyser.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("fnExp  out");
	}

	public void fnSelectStartingLambdaValue() {
		lambda = 1.0;
		fnCrossFold(t, C, F, cost, b, lambda, mu, m, U, T);/*-V,L,-d,$d".*/
		f1 = fnReturnF1();
		System.out
				.println("(1) fnSelectStartingLambdaValue() f1="+f1+" prev_f1="+prev_f1);
		if (f1 > prev_f1) {
			prev_f1 = fnSetPrevF1(f1);
			fnPrint(t, C, F, cost, b, lambda, mu, m, U);
			max_iter_sub_lambda = 8;
		} else {
			lambda = 0.4;
			max_iter_sub_lambda = 2;
		}
	}

	// Select starting point for COST and lambda parameter
	public static void fnSelectStartingCostValue() {
		// c=1.0
		// L=.5
		// M=0.4
		// T=1.1
System.out.println("fnSelectStartingCostValue  in");
		cost = 0.2;
		System.out.println("t:" + t + " C:" + C + " F:" + F + " cost:" + cost + " b:"
				+ b + " lambda:" + lambda + " mu:" + mu + " m:" + m + " U:" + U + " T:"
				+ T);
		fnCrossFold(t, C, F, cost, b, lambda, mu, m, U, T);
		System.out.println("fnCrossFold  out");
		prev_f1 = fnReturnF1();
System.out.println("fnReturnF1:"+prev_f1+",  fnReturnF1   out");
		fnPrint(t, C, F, cost, b, lambda, mu, m, U);
		System.out.println("fnPrint   out");
		cost = 1.0;
		f1 = 0;
		// fnCrossFold(t,C,F,cost,b,lambda,mu,m,U,T);
		// f1=fnReturnF1();
		writte(TRACE_FILE, "f1=" + f1 + " prev_f1=" + prev_f1);
		System.out.println(TRACE_FILE);
		System.out
				.println("(1) fnSelectStartingCostValue() f1="+f1+" prev_f1="+prev_f1);
		if (f1 > prev_f1) {
			prev_f1 = fnSetPrevF1(f1);
			max_iter_sub_cost = 8;
			fnPrint(t, C, F, cost, b, lambda, mu, m, U);
		} else
			cost = 0.2;
		max_iter_sub_cost = 1;
		// echo "F1 $prev_f1" >> $BEST_RES_FILE
	}

	// Optimize the d parameter by incrementing 1
	public static void fnOptimize_poly_d() {
		int at = 1;
		int best_d = 2;
		System.out
				.println("f1=" + f1 + " prev_f1=" + prev_f1 + " offset=" + at);
		for (int inc_var = 2; inc_var <= 9; inc_var++) {
			d = inc_var;
			fnCrossFold(t, C, F, cost, b, lambda, mu, m, U, T);
			f1 = fnReturnF1();
			writte(TRACE_FILE, "f1=" + f1 + " prev_f1=" + prev_f1 + " offset="
					+ at);
			System.out
					.println("(1) fnOptimize_poly_d() f1="+f1+" prev_f1="+prev_f1+" offset="+at);
			if (f1 > prev_f1) {
				prev_f1 = fnSetPrevF1(f1);
				fnPrint(t, C, F, cost, b, lambda, mu, m, U);
				best_d = d;
			} else
				inc_var = 1000;
			d = best_d;
		}
	}

	// Optimize the T parameter by incrementing 0.1
	public static void fnOptimizeT() {

		double at = 0.1;
		double T = 1.1;
		for (int inc_var = 1; inc_var <= 25; inc_var++) {
			fnCrossFold(t, C, F, cost, b, lambda, mu, m, U, T);
			f1 = fnReturnF1();
			writte(TRACE_FILE, "f1=" + f1 + " prev_f1=" + prev_f1 + " offset="
					+ at);
			System.out
					.println("(1) fnOptimizeT() f1="+f1+" prev_f1="+prev_f1+" offset="+at);

			if (f1 > prev_f1) {
				prev_f1 = fnSetPrevF1(f1);
				fnPrint(t, C, F, cost, b, lambda, mu, m, U);

				T = T + at;
			} else
				inc_var = 1000;
		}
		T = 0.9;
		for (int inc_var = 1; inc_var <= 8; inc_var++) {
			fnCrossFold(t, C, F, cost, b, lambda, mu, m, U, T);
			f1 = fnReturnF1();
			writte(TRACE_FILE, "f1=" + f1 + " prev_f1=" + prev_f1 + " offset="
					+ at);
			System.out
					.println("(2) fnOptimizeT() f1="+f1+" prev_f1="+prev_f1+" offset="+at);

			if (f1 > prev_f1) {
				prev_f1 = fnSetPrevF1(f1);
				fnPrint(t, C, F, cost, b, lambda, mu, m, U);

				T = T + at;
			} else
				inc_var = 1000;
		}
	}

	public static void fnOptimizePARAM(int opt) {

		int change = opt;
		writte(TRACE_FILE, "change=" + change);

		T = 1.0;
		offset = 0.1;

		best_lambda = lambda;
		best_mu = mu;
		best_cost = cost;

		double init_lambda = lambda;
		double init_mu = mu;
		double init_cost = cost;

		for (int inc_var = 2; inc_var <= max_iter_add; inc_var++) {

			if (change > 2)
				lambda = lambda + offset;
			else if (change > 1)
				mu = mu + offset;
			else if (change > 0)
				cost = cost + offset;

			fnCrossFold(t, C, F, cost, b, lambda, mu, m, U, T);
			f1 = fnReturnF1();
			writte(TRACE_FILE, "f1=" + f1 + " prev_f1=" + prev_f1 + " offset="
					+ offset);

			System.out
					.println("(1) fnOptimizePARAM() f1="+f1+" prev_f1="+prev_f1+" offset="+offset);
			if (f1 > prev_f1) {
				prev_f1 = fnSetPrevF1(f1);

				best_lambda = fnSetPrevF1(lambda);
				best_mu = fnSetPrevF1(mu);
				best_cost = fnSetPrevF1(cost);

				fnPrint(t, C, F, cost, b, lambda, mu, m, U);
			} else
				inc_var = 1000;
		}

		lambda = init_lambda;
		mu = init_mu;
		cost = init_cost;

		for (int inc_var = 1; inc_var <= max_iter_sub; inc_var++) {

			if (change > 2)
				lambda = lambda - offset;
			else if (change > 1)
				mu = mu - offset;
			else if (change > 0)
				cost = cost - offset;

			fnCrossFold(t, C, F, cost, b, lambda, mu, m, U, T);
			f1 = fnReturnF1();
			writte(TRACE_FILE, "f1=" + f1 + " prev_f1=" + prev_f1 + " offset="
					+ offset);

			System.out
					.println("(2) fnOptimizePARAM() f1="+f1+" prev_f1="+prev_f1+" offset="+offset);
			if (f1 > prev_f1) {
				prev_f1 = fnSetPrevF1(f1);

				best_lambda = fnSetPrevF1(lambda);
				best_mu = fnSetPrevF1(mu);
				best_cost = fnSetPrevF1(cost);

				fnPrint(t, C, F, cost, b, lambda, mu, m, U);
			} else
				inc_var = 1000;
		}

		lambda = best_lambda;
		mu = best_mu;
		cost = best_cost;
	}

	// Run the tool
	public static void fnRun(int t2, String c2, int f2, double cost2, int b2,
			double lambda2, double mu2, int m2, int u2, double t3,
			String string, int d2) {
		string = "L";
		// String[]
		// Parameters="-t,$1,-C,$2,-F,$3,-c,$4,-b,$5,-L,$6,-M,$7,-m,$8,-U,$9,-T,$T,-V,L,-d,$d".split(",")
		// ;
		String Parameters = "-t " + t2 + " -C " + c2 + " -F " + f2 + " -c "
				+ cost2 + " -b " + b2 + " -L " + lambda2 + " -M " + mu2
				+ " -m " + m2 + " -U " + u2 + " -T " + t3 + " -V " + string
				+ " -d " + d2;

		writte(TRACE_FILE, Parameters);

		String[] f = { OUT_FILE, TRACE_FILE, PRED_FILE, All_PRED_FILE };
		for (int i = 0; i <= f.length; i++) {
			File path = new File(OUT_DIR + f[i]);
			path.deleteOnExit();
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+OUT_DIR + "/train.tk");
			double[] pn = CountClass(OUT_DIR + "/train.tk");
			double neg = pn[1];
			double pos = pn[0];
			System.out.println("train.tk    pos==="+pos);
			System.out.println("train.tk    neg==="+neg);
			// int pos=`grep "^1" $OUT_DIR/train.tk | wc -l`//文件按行匹配，开始为1的个数
			// int neg=`grep "^-1" $OUT_DIR/train.tk | wc -l`//文件按行匹配，开始为-的个数

			if (pos < 1) {
				System.out
						.println("No of positive instance is zero. Aborting training.");
				findNegatedSentence = 0;
				return;
			}
			//
			// if (pos < 1)
			// then
			// echo "No of negative instance is zero. Aborting training."
			// findNegatedSentence=0
			// return
			// fi

			double cf = neg / pos;

			writte(OUT_FILE, "pos=" + pos + " neg=" + neg + " cf=" + cf);
			if (TRAIN_DATA_FULL != "") {
				System.out.println(df.format(new Date())
						+ " -> Training started...");
				writte(TRACE_FILE, df.format(new Date())
						+ " -> Training started...");
			}
			openExe("F:/DDI/sofeware/HyREX/SVM-Light-TK-1.5/svm_classify.exe " + Parameters + " -j " + cf + OUT_DIR
					+ "/train.tk " + OUT_DIR + "/model");
			// ./svm_learn $Parameters -j $cf $OUT_DIR/train.tk $OUT_DIR/model;
		}
		System.out.println(df.format(new Date()) + " -> Testing started...");
		writte(TRACE_FILE, df.format(new Date()) + " -> Testing started...");
		openExe("F:/DDI/sofeware/HyREX/SVM-Light-TK-1.5/svm_classify " + OUT_DIR + "/test.tk " + OUT_DIR + "/model");
		// ./svm_classify $OUT_DIR/test.tk $OUT_DIR/model >> $OUT_FILE;
		less(PRED_FILE, All_PRED_FILE);

		writte(OUT_FILE, "");
		fnPrint(t, C, F, cost, b, lambda, mu, m, U);
	}

	// Do n-fold cross validation
	public static void fnCrossFold(int t, String C, int F, double cost, int b,
			double lambda, double mu, double m, double U, double T) {
		System.out.println("fnCrossFold   in");
		String t2s, F2s, cost2s, b2s, lambda2s, mu2s, m2s, U2s, T2s, d2s;
		t2s = Double.toString(t);
		F2s = Double.toString(F);
		cost2s = Double.toString(cost);
		b2s = Double.toString(b);
		lambda2s = Double.toString(lambda);
		mu2s = Double.toString(mu);
		m2s = Double.toString(m);
		U2s = Double.toString(U);
		T2s = Double.toString(T);
		d2s = Double.toString(d);
		String[] Parameters = { "-t", t2s, "-C ", C, " -F ", F2s, " -c ",
				cost2s, " -b ", b2s, " -L ", lambda2s, " -M ", mu2s, " -m ",
				m2s, " -U ", U2s, " -T ", T2s, " -V L -d ", d2s };
		

		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < Parameters.length; i++) {
			strBuilder.append(Parameters[i] + " ");
		}
		String tmp = strBuilder.toString();
		writte(TRACE_FILE, tmp);

		String[] f = { OUT_FILE, PRED_FILE, All_PRED_FILE };
		System.out.println("文件F");
		for (int i = 0; i <= f.length-1; i++) {
			System.out.println("f"+i+f[i]);
			File path = new File(OUT_DIR + f[i]);
			path.deleteOnExit();
		}

		for (int i = 1; i <= NO_OF_FOLDS; i++) {
			System.out.println(CORPUS_FILES_DIR + "/train-203-" + i + ".tk");
			double[] pn = CountClass(CORPUS_FILES_DIR + "/train-203-" + i + ".tk");
			double neg = pn[1];
			double pos = pn[0];
			// int pos=`grep "^1" $CORPUS_FILES_DIR/train-203-$i.tk | wc -l`;
			// int neg=`grep "^-1" $CORPUS_FILES_DIR/train-203-$i.tk | wc -l`;
			double cf = neg/pos;
			writte(OUT_FILE, "pos="+pos+" neg="+neg+" cf="+cf);

			System.out.println("Learning on train fold " + i);
			writte(TRACE_FILE, df.format(new Date()) + "-> training for fold "
					+ i);
			openExe("F:/DDI/sofeware/HyREX/SVM-Light-TK-1.5/svm_classify.exe " + tmp + "-j" + cf + CORPUS_FILES_DIR
					+ "/train-203-" + i + ".tk " + OUT_DIR + "/model");
			// ./svm_learn $Parameters -j $cf CORPUS_FILES_DIR/train-203-$i.tk
			// $OUT_DIR/model

			writte(TRACE_FILE, "Testing on test fold " + i);
			writte(TRACE_FILE, "Testing on test fold " + i);
			writte(TRACE_FILE, df.format(new Date()) + "testing for fold " + i);
			openExe("F:/DDI/sofeware/HyREX/SVM-Light-TK-1.5/svm_classify.exe " + CORPUS_FILES_DIR + "/test-203-" + i
					+ ".tk " + OUT_DIR + "/model");
			// ./svm_classify $CORPUS_FILES_DIR/test-203-$i.tk $OUT_DIR/model >>
			// $OUT_FILE;
			less(PRED_FILE, All_PRED_FILE);

			writte(OUT_FILE, "");

		}

		writte(TRACE_FILE, "");
		writte(TRACE_FILE, "----------------------------------------");
		writte(TRACE_FILE, "");
		System.out.println("----------------------------------------");
		System.out.println("----------------------------------------");
	}

	// #####################################################
	// # Run HyREX to extract positive relations
	// #####################################################

	public static void fnExtractRels() {
		String[] args = {All_PRED_FILE,OUT_DIR+"/entPairFileName_TK"};
		try {
			Others.PredictionResult.main(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("");
		System.out
				.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println(df.format(new Date())
				+ "-> Extracted relations are written in " + OUT_DIR
				+ "/extracted_relations.txt");
		System.out
				.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}

	// #####################################################

	// Return F1 score
	public static double fnReturnF1() {
		// cd $HyREX_DIR
System.out.println("fnReturnF1   in");
		double f_score = 0;
		 try {
			TKOutputGenerator.main("-ev,tk,-evalOutFile,F:/DDI/sofeware/HyREX/out/output.txt".split(","));
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		// /*`java $JVM_ARGS Kernels.TKOutputGenerator -ev tk -evalOutFile
		// $OUT_FILE`;*/
		// System.out.println(f_score);
		CommonUtility.calculateCrossFoldResult(OUT_FILE, false);
		return f_score;
	}

	// Set the current F1 value as prev F1
	public static double fnSetPrevF1(double f) {
		double fn = f;
		return fn;
		// System.out.println(f1);
	}

	public void fnPrint(int t, String C, double F, double cost, int b,
			double lambda, double mu, int m, int U, double T, int d) {
		less(OUT_FILE, BEST_RES_FILE);
		writte(TRACE_FILE, "Updating " + BEST_RES_FILE + " file ....");
		writte(BEST_RES_FILE, "-t " + t + " -C " + C + " -F " + F + " -c "
				+ cost + " -b " + b + " -L " + lambda + " -M " + mu + " -m "
				+ m + " -U " + U + " -T " + T + " -V L -d " + d);
		less(All_PRED_FILE, BEST_All_PRED_FILE);
		String[] args = "-train,F:/DDI/sofeware/HyREX/sample-data/sample.full,-ev,full,-evalOutFile,./out/"
				.split(",");
		try {
			TKOutputGenerator.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// java $JVM_ARGS Kernels.TKOutputGenerator $TRAIN_DATA_FULL -ev full
		// -evalOutFile $OUT_FILE >> $BEST_RES_FILE

		fnExtractRels();
		// less $OUT_DIR/extracted_relations.txt >
		// $OUT_DIR/best_extracted_relations.txt
	}

	public static void fnPrint(int t, String C, double F, double cost, int b,
			double lambda, double mu, int m, int U) {
		System.out.println("fnPrint   in");
		less(OUT_FILE, BEST_RES_FILE);// copy
		writte(TRACE_FILE, "Updating " + BEST_RES_FILE + " file ....");
		writte(BEST_RES_FILE, "-t " + t + " -C " + C + " -F " + F + " -c "
				+ cost + " -b " + b + " -L " + lambda + " -M " + mu + " -m "
				+ m + " -U " + U + " -T " + T + " -V L -d " + d);
		less(All_PRED_FILE, BEST_All_PRED_FILE);
		String[] args = {"-train",DATA_HOME+"/sample.full","-ev","full","-evalOutFile",OUT_FILE};
		try {
			TKOutputGenerator.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// java $JVM_ARGS Kernels.TKOutputGenerator $TRAIN_DATA_FULL -ev full
		// -evalOutFile $OUT_FILE >> $BEST_RES_FILE

		fnExtractRels();
		// less $OUT_DIR/extracted_relations.txt >
		// $OUT_DIR/best_extracted_relations.txt
	}

	public static void openExe(String dir) {
		Runtime rn = Runtime.getRuntime();
		try {
			rn.exec(dir);
		} catch (Exception e) {
			System.out.println("Error!");
		}
	}

	public static void less(String dir1, String dir2) {
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("cmd /c copy " + dir1 + " " + dir2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double[] CountClass(String file) {
		double neg = 0;
		double pos = 0;
		double[] pn = { pos, neg };
		try {
			FileReader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			String obj = null;

			while ((obj = br.readLine()) != null) {
				char a = ' ';
				a = obj.charAt(0);
				System.out.println(a);
				if (a == '-') {
					neg = neg + 1;
				} else if (a == '1') {
					pos = pos + 1;
				}
			}
			br.close();
			reader.close();
		} catch (IOException e) {
			System.out.println("FileTool-->select(String src)-错误");
		}
		return pn;
	}
}
