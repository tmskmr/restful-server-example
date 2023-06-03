package ca.uhn.example.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;

public class Common {
	private static SqlSessionFactory TheSqlSessionFactory = null;
    private static Properties theProperties;

    /*
     * 初期化する
     */
	public static void Initialize(String dir) {
        try {
        	String path = dir + File.separator + "classes" + File.separator + "application.properties";

        	theProperties = new Properties();
            theProperties.load(Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8));
        } catch (IOException e) {
        	e.printStackTrace();
        }

 		// DB
		String resource = "mybatis-config.xml";
		try {
			TheSqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream(resource), theProperties);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MyBatis のセッションをオープンする
	 */
	public static SqlSession OpenSqlSession() {
		if (TheSqlSessionFactory == null) {
			return null;
		}

		return TheSqlSessionFactory.openSession();
	}

	public static String GetSDIso21090ENRepresentation() {
		return "http://hl7.org/fhir/StructureDefinition/iso21090-EN-representation";
	}

	public static String GetNSPatient() {
		return theProperties.getProperty("patient.namespace");
	}

	public static String SAFESTR(String s) { return s != null ? s : ""; }

	/**
	 * HumanName オブジェクトを生成する
	 *
	 * @param name		人名
	 * @param style		スタイル(IDE=漢字,SYL=カナ)
	 * @return			HumanNameオブジェクト
	 */
	public static HumanName CreateHumanName(String family, String given, String style) {
		if (family == null || given == null)
			return null;

	     HumanName humanName = new HumanName()
	     	.setText(family + " " + given)
	     	.setUse(NameUse.OFFICIAL)
	    	.setFamily(family)
	    	.addGiven(given);

	      // Create an extension
	      if (style != null) {
		      humanName.addExtension()
 		        // "http://hl7.org/fhir/StructureDefinition/iso21090-EN-representation"
		      	.setUrl(GetSDIso21090ENRepresentation())
		      	.setValue(new CodeType(style));
	      }

	      return humanName;
	}

    /*================================================================================================
	 * 日本語文字処理
	 *===============================================================================================*/
    private static final String IS_FULL_KANA = "^[\\u3000\\u30A0-\\u30FF]+$";
    private static final String HAS_FULL_KANA = ".*[\\u30A0-\\u30FF]+.*";

    /**
           * 文字列がすべて全角カタカナかどうかを返す
     *
     * @param str	対象の文字列
     * @return		すべて全角カタカナの場合trueを返す
     */
    public static boolean IsFullKatakana(String str) { return str.matches(IS_FULL_KANA); }

    /**
          * 文字列に全角カタカナが含まれるかどうかを返す
     *
     * @param str	対象の文字列
     * @return		全角カタカナが含まれる場合trueを返す
     */
    public static boolean HasFullKatakana(String str) { return str.matches(HAS_FULL_KANA); }
};
