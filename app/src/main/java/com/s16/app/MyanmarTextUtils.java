package com.s16.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyanmarTextUtils {

	private static final int NULL_CHAR = 0x00;
	
	private static final Pattern patternZawGyi;
	private static final String[] patternsZawGyiToUni;
	private static final String[] patternsUniToZawGyi;
	static {
		patternZawGyi = Pattern.compile("\u1039[\u1000-\u1021]\u103A|[\u105A\u1060-\u1097]" +
            "|[\u1033\u1034]|\u1031\u108F|\u1031[\u103B-\u103E]|[\u102B-\u1030\u1032]\u1031| \u1031| \u103B" +
            "|^\u1031|^\u103B|\u1038\u103B|\u1038\u1031|[\u102D\u102E\u1032]\u103B|\u1039[^\u1000-\u1021]" +
            "|\u1039$|\u1004\u1039[\u1001-\u102A\u103F\u104E]|\u1039[^u1000-\u102A\u103F\u104E]|\u103C\u103B" +
            "|\u103D\u103B|\u103E\u103B|\u103D\u103C|\u103E\u103C|\u103E\u103D|\u103B\u103C" +
            "|[\u102F\u1030\u102B\u102C][\u102D\u102E\u1032]|[\u102B\u102C][\u102F\u102C]|[\u1040-\u1049]" +
            "[\u102B-\u103E\u102B-\u1030\u1032\u1036\u1037\u1038\u103A]|^[\u1040\u1047][^\u1040-\u1049]" +
            "|[\u1000-\u102A\u103F\u104E]\u1039[\u101A\u101B\u101D\u101F\u1022-\u103F]|\u103A\u103E|\u1036\u102B]" +
            "|\u102D[\u102E\u1032]|\u102E[\u102D\u1032]|\u1032[\u102D\u102E]|\u102F\u1030|\u1030\u102F|\u102B\u102C" +
            "|\u102C\u102B|[\u1090-\u1099][\u102B-\u1030\u1032\u1037\u103A-\u103E]|[\u1000-\u10F4][\u1090-\u1099]" +
            "[\u1000-\u104F]|^[\u1090-\u1099][\u1000-\u102A\u103F\u104E\u104A\u104B]|[\u1000-\u104F][\u1090-\u1099]$" +
            "|[\u105E-\u1060\u1062-\u1064\u1067-\u106D\u1071-\u1074\u1082-\u108D\u108F\u109A-\u109D][\u102B-\u103E]" +
            "|[\u1000-\u102A]\u103A[\u102D\u102E\u1032]|[\u102B-\u1030\u1032\u1036-\u1038\u103A]\u1031|[\u1087-\u108D]" +
            "[\u106E-\u1070\u1072-\u1074]|^[\u105E-\u1060\u1062-\u1064\u1067-\u106D\u1071-\u1074\u1082-\u108D\u108F\u109A-\u109D]" +
            "|[ \u104A\u104B][\u105E-\u1060\u1062-\u1064\u1067-\u106D\u1071-\u1074\u1082-\u108D\u108F\u109A-\u109D]" +
            "|[\u1036\u103A][\u102D-\u1030\u1032]|[\u1025\u100A]\u1039|[\u108E-\u108F][\u1050-\u108D]" +
            "|\u102D-\u1030\u1032\u1036-\u1037]\u1039]|[\u1000-\u102A\u103F\u104E]\u1037\u1039|[\u1000-\u102A\u103F\u104E]" +
            "\u102C\u1039[\u1000-\u102A\u103F\u104E]|[\u102B-\u1030\u1032][\u103B-\u103E]|\u1032[\u103B-\u103E]|\u101B\u103C" +
            "|[\u1000-\u102A\u103F\u104E]\u1039[\u1000-\u102A\u103F\u104E]\u1039[\u1000-\u102A\u103F\u104E]" +
            "|[\u1000-\u102A\u103F\u104E]\u1039[\u1000-\u102A\u103F\u104E][\u102B\u1032\u103D]" +
            "|[\u1000\u1005\u100F\u1010\u1012\u1014\u1015\u1019\u101A]\u1039\u1021|[\u1000\u1010]\u1039\u1019|\u1004\u1039\u1000" +
            "|\u1015\u1039[\u101A\u101E]|\u1000\u1039\u1001\u1036|\u1039\u1011\u1032|\u1037\u1032|\u1036\u103B|\u102F\u102F");
			
		patternsZawGyiToUni = new String[] { 
			  "\u200B", ""
            , "([\u103D\u1087])", "\u103E" 
            , "\u103C", "\u103D" 
            , "([\u103B\u107E\u107F\u1080\u1081\u1082\u1083\u1084])", "\u103C" 
            , "([\u103A\u107D])", "\u103B" 
            , "\u1039", "\u103A" 
            , "([\u1066\u1067])", "\u1039\u1006" 
            , "\u106A", "\u1009" 
            , "\u106B", "\u100A" 
            , "\u106C", "\u1039\u100B" 
            , "\u106D", "\u1039\u100C" 
            , "\u106E", "\u100D\u1039\u100D" 
            , "\u106F", "\u100D\u1039\u100E" 
            , "\u1070", "\u1039\u100F" 
            , "([\u1071\u1072])", "\u1039\u1010" 
            , "\u1060", "\u1039\u1000" 
            , "\u1061", "\u1039\u1001" 
            , "\u1062", "\u1039\u1002" 
            , "\u1063", "\u1039\u1003" 
            , "\u1065", "\u1039\u1005" 
            , "\u1068", "\u1039\u1007" 
            , "\u1069", "\u1039\u1008" 
            , "([\u1073\u1074])", "\u1039\u1011" 
            , "\u1075", "\u1039\u1012" 
            , "\u1076", "\u1039\u1013" 
            , "\u1077", "\u1039\u1014" 
            , "\u1078", "\u1039\u1015" 
            , "\u1079", "\u1039\u1016" 
            , "\u107A", "\u1039\u1017" 
            , "\u107C", "\u1039\u1019" 
            , "\u1085", "\u1039\u101C" 
            , "\u1033", "\u102F" 
            , "\u1034", "\u1030" 
            , "\u103F", "\u1030" 
            , "\u1086", "\u103F" 
            , "\u1036\u1088", "\u1088\u1036" 
            , "\u1088", "\u103E\u102F" 
            , "\u1089", "\u103E\u1030" 
            , "\u108A", "\u103D\u103E" 
            , "([\u1000-\u1021])\u1064", "\u1004\u103A\u1039$1" 
            , "([\u1000-\u1021])\u108B", "\u1004\u103A\u1039$1\u102D" 
            , "([\u1000-\u1021])\u108C", "\u1004\u103A\u1039$1\u102E" 
            , "([\u1000-\u1021])\u108D", "\u1004\u103A\u1039$1\u1036" 
            , "\u108E", "\u102D\u1036" 
            , "\u108F", "\u1014" 
            , "\u1090", "\u101B" 
            , "\u1091", "\u100F\u1039\u100D" 
            , "\u1019\u102C([\u107B\u1093])", "\u1019\u1039\u1018\u102C" 
            , "([\u107B\u1093])", "\u1039\u1018" 
            , "([\u1094\u1095])", "\u1037" 
            , "\u1096", "\u1039\u1010\u103D" 
            , "\u1097", "\u100B\u1039\u100B" 
            , "\u103C([\u1000-\u1021])([\u1000-\u1021])?", "$1\u103C$2" 
            , "([\u1000-\u1021])\u103C\u103A", "\u103C$1\u103A" 
            , "\u1031([\u1000-\u1021])(\u103E)?(\u103B)?", "$1$2$3\u1031" 
            , "([\u1000-\u1021])\u1031([\u103B\u103C\u103D\u103E]+)", "$1$2\u1031" 
            , "\u1032\u103D", "\u103D\u1032" 
            , "\u103D\u103B", "\u103B\u103D" 
            , "\u1037\u103A", "\u103A\u1037"
            , "\u102F([\u102D\u102E\u1036\u1037])\u102F", "\u102F$1" 
            , "\u102F\u102F", "\u102F" 
            , "([\u102F\u1030])([\u102D\u102E])", "$2$1" 
            , "(\u103E)([\u103B\u1037])", "$2$1" 
            , "\u1025([\u103A\u102C])", "\u1009$1" 
            , "\u1025\u102E", "\u1026" 
            , "\u1005\u103B", "\u1008" 
            , "\u1036([\u102F\u1030])", "$1\u1036" 
            , "\u1031\u1037\u103E", "\u103E\u1031\u1037" 
            , "\u1031\u103E\u102C", "\u103E\u1031\u102C" 
            , "\u105A", "\u102B\u103A" 
            , "\u1031\u103B\u103E", "\u103B\u103E\u1031" 
            , "([\u102D\u102E])([\u103D\u103E])", "$2$1" 
            , "\u102C\u1039([\u1000-\u1021])", "\u1039$1\u102C" 
            , "\u103C\u1004\u103A\u1039([\u1000-\u1021])", "\u1004\u103A\u1039$1\u103C" 
            , "\u1039\u103C\u103A\u1039([\u1000-\u1021])", "\u103A\u1039$1\u103C" 
            , "\u103C\u1039([\u1000-\u1021])", "\u1039$1\u103C" 
            , "\u1036\u1039([\u1000-\u1021])", "\u1039$1\u1036" 
            , "\u1092", "\u100B\u1039\u100C" 
            , "\u104E", "\u104E\u1004\u103A\u1038" 
            , "\u1040([\u102B\u102C\u1036])", "\u101D$1" 
            , "\u1025\u1039", "\u1009\u1039" 
            , "([\u1000-\u1021])\u103C\u1031\u103D", "$1\u103C\u103D\u1031" 
            , "([\u1000-\u1021])\u103D\u1031\u103B", "$1\u103B\u103D\u1031" 
            , "([\u1000-\u1021])\u1031(\u1039[\u1000-\u1021])", "$1$2\u1031" 
		};
		
		patternsUniToZawGyi = new String[] { 
              "\u1004\u103A\u1039", "\u1064" 
            , "\u1039\u1010\u103D", "\u1096" 
            , "\u1014(?=[\u1030\u103D\u103E\u102F\u1039])", "\u108F" 
            , "\u102B\u103A", "\u105A" 
            , "\u100B\u1039\u100C", "\u1092" 
            , "\u102D\u1036", "\u108E" 
            , "\u104E\u1004\u103A\u1038", "\u104E" 
            , "[\u1025\u1009](?=[\u1039\u102F\u1030])", "\u106A" 
            , "[\u1025\u1009](?=[\u103A])", "\u1025" 
            , "\u100A(?=[\u1039\u102F\u1030\u103D])", "\u106B" 
            , "(\u1039[\u1000-\u1021])\u102F", "$1\u1033" 
            , "(\u1039[\u1000-\u1021])\u1030", "$1\u1034" 
            , "\u1039\u1000", "\u1060" 
            , "\u1039\u1001", "\u1061" 
            , "\u1039\u1002", "\u1062" 
            , "\u1039\u1003", "\u1063" 
            , "\u1039\u1005", "\u1065" 
            , "\u1039\u1006", "\u1066" 
            , "\u1039\u1007", "\u1068" 
            , "\u1039\u1008", "\u1069" 
            , "\u100A(?=[\u1039\u102F\u1030])", "\u106B" 
            , "\u1039\u100B", "\u106C" 
            , "\u1039\u100C", "\u106D" 
            , "\u100D\u1039\u100D", "\u106E" 
            , "\u100E\u1039\u100D", "\u106F" 
            , "\u1039\u100F", "\u1070" 
            , "\u1039\u1010", "\u1071" 
            , "\u1039\u1011", "\u1073" 
            , "\u1039\u1012", "\u1075" 
            , "\u1039\u1013", "\u1076" 
            , "\u1039\u1013", "\u1076" 
            , "\u1039\u1014", "\u1077" 
            , "\u1039\u1015", "\u1078" 
            , "\u1039\u1016", "\u1079" 
            , "\u1039\u1017", "\u107A" 
            , "\u1039\u1018", "\u107B" 
            , "\u1039\u1019", "\u107C" 
            , "\u1039\u101C", "\u1085" 
            , "\u103F", "\u1086" 
            , "(\u103C)\u103E", "$1\u1087" 
            , "\u103D\u103E", "\u108A" 
            , "(\u1064)([\u1031]?)([\u103C]?)([\u1000-\u1021])\u102D", "$2$3$4\u108B" 
            , "(\u1064)([\u1031]?)([\u103C]?)([\u1000-\u1021])\u102E", "$2$3$4\u108C" 
            , "(\u1064)([\u1031]?)([\u103C]?)([\u1000-\u1021])\u1036", "$2$3$4\u108D" 
            , "(\u1064)([\u1031]?)([\u103C]?)([\u1000-\u1021])", "$2$3$4$1" 
            , "\u101B(?=[\u102F\u1030\u103D\u108A])", "\u1090" 
            , "\u100F\u1039\u100D", "\u1091" 
            , "\u100B\u1039\u100B", "\u1097" 
            , "([\u1000-\u1021\u108F\u1029\u1090])([\u1060-\u1069\u106C\u106D\u1070-\u107C\u1085\u108A])?([\u103B-\u103E]*)?\u1031", "\u1031$1$2$3" 
            , "([\u1000-\u1021\u1029])([\u1060-\u1069\u106C\u106D\u1070-\u107C\u1085])?(\u103C)", "$3$1$2" 
            , "\u103A", "\u1039" 
            , "\u103B", "\u103A" 
            , "\u103C", "\u103B" 
            , "\u103D", "\u103C" 
            , "\u103E", "\u103D" 
            , "\u103D\u102F", "\u1088" 
            , "([\u102F\u1030\u1014\u101B\u103C\u108A\u103D\u1088])([\u1032\u1036]{0,1})\u1037", "$1$2\u1095" 
            , "\u102F\u1095", "\u102F\u1094" 
            , "([\u1014\u101B])([\u1032\u1036\u102D\u102E\u108B\u108C\u108D\u108E])\u1037", "$1$2\u1095" 
            , "\u1095\u1039", "\u1094\u1039" 
            , "([\u103A\u103B])([\u1000-\u1021])([\u1036\u102D\u102E\u108B\u108C\u108D\u108E]?)\u102F", "$1$2$3\u1033" 
            , "([\u103A\u103B])([\u1000-\u1021])([\u1036\u102D\u102E\u108B\u108C\u108D\u108E]?)\u1030", "$1$2$3\u1034" 
            , "\u103A\u102F", "\u103A\u1033" 
            , "\u103A([\u1036\u102D\u102E\u108B\u108C\u108D\u108E])\u102F", "\u103A$1\u1033" 
            , "([\u103A\u103B])([\u1000-\u1021])\u1030", "$1$2\u1034" 
            , "\u103A\u1030", "\u103A\u1034" 
            , "\u103A([\u1036\u102D\u102E\u108B\u108C\u108D\u108E])\u1030", "\u103A$1\u1034" 
            , "\u103D\u1030", "\u1089" 
            , "\u103B([\u1000\u1003\u1006\u100F\u1010\u1011\u1018\u101A\u101C\u101A\u101E\u101F])", "\u107E$1" 
            , "\u107E([\u1000\u1003\u1006\u100F\u1010\u1011\u1018\u101A\u101C\u101A\u101E\u101F])([\u103C\u108A])([\u1032\u1036\u102D\u102E\u108B\u108C\u108D\u108E])", "\u1084$1$2$3" 
            , "\u107E([\u1000\u1003\u1006\u100F\u1010\u1011\u1018\u101A\u101C\u101A\u101E\u101F])([\u103C\u108A])", "\u1082$1$2" 
            , "\u107E([\u1000\u1003\u1006\u100F\u1010\u1011\u1018\u101A\u101C\u101A\u101E\u101F])([\u1033\u1034]?)([\u1032\u1036\u102D\u102E\u108B\u108C\u108D\u108E])", "\u1080$1$2$3" 
            , "\u103B([\u1000-\u1021])([\u103C\u108A])([\u1032\u1036\u102D\u102E\u108B\u108C\u108D\u108E])", "\u1083$1$2$3" 
            , "\u103B([\u1000-\u1021])([\u103C\u108A])", "\u1081$1$2" 
            , "\u103B([\u1000-\u1021])([\u1033\u1034]?)([\u1032\u1036\u102D\u102E\u108B\u108C\u108D\u108E])", "\u107F$1$2$3" 
            , "\u103A\u103D", "\u103D\u103A" 
            , "\u103A([\u103C\u108A])", "$1\u107D" 
            , "([\u1033\u1034])\u1094", "$1\u1095" 
            , "\u108F\u1071", "\u108F\u1072" 
            , "([\u1000-\u1021])([\u107B\u1066])\u102C",  "$1\u102C$2" 
            , "\u102C([\u107B\u1066])\u1037",  "\u102C$1\u1094"
		};
	};
	
	private static CharSequence RegexReplace(CharSequence data, CharSequence find, CharSequence replacement) {
		if ((data == null) || (data == "")) return data;
		StringBuffer result = new StringBuffer();
		Pattern pattern = Pattern.compile(find.toString());
		Matcher m = pattern.matcher(data);
		while(m.find()) {
			StringBuffer replacementBuffer = new StringBuffer();
			boolean foundGroup = false;
			for(int c = 0; c < replacement.length(); c++) {
				char ch = replacement.charAt(c);
				if(foundGroup) {
					switch(ch) {
						case '1':
							replacementBuffer.append((m.group(1) == null ? "" : "$1"));
							break;
						case '2':
							replacementBuffer.append((m.group(2) == null ? "" : "$2"));
							break;
						case '3':
							replacementBuffer.append((m.group(3) == null ? "" : "$3"));
							break;
						case '4':
							replacementBuffer.append((m.group(4) == null ? "" : "$4"));
							break;
						case '5':
							replacementBuffer.append((m.group(5) == null ? "" : "$5"));
							break;
						case '6':
							replacementBuffer.append((m.group(6) == null ? "" : "$6"));
							break;
						case '7':
							replacementBuffer.append((m.group(7) == null ? "" : "$7"));
							break;
						case '8':
							replacementBuffer.append((m.group(8) == null ? "" : "$8"));
							break;
						case '9':
							replacementBuffer.append((m.group(9) == null ? "" : "$9"));
							break;
						default:
							break;
					}
					
					foundGroup = false;
					continue;
				}
				
				if (ch == '$') {
					foundGroup = true;
					continue;
				}
				
				replacementBuffer.append(ch);
			}
			
			m.appendReplacement(result, replacementBuffer.toString());
		}
		m.appendTail(result);
		return result.toString();
	}
	
	public static boolean isMyChar(int code) {
		return (code >= 0x1000 && code <= 0x109F) || (code >= 0xAA60 && code <= 0xAA7B);
	}
	
	public static boolean isMyChar(int[] codes) {
		if(codes == null) return false;
		boolean isMyChar = false;
    	for(int i = 0; i < codes.length; i++) {
    		if(isMyChar(codes[i])) {
    			isMyChar = true;
    			break;
    		}
    	}
		return isMyChar;
	}
	
	public static boolean isMyChar(CharSequence label) {
		if(label == null) return false;
		boolean isMyChar = false;
    	for(int i = 0; i < label.length(); i++) {
    		if(isMyChar(label.charAt(i))) {
    			isMyChar = true;
    			break;
    		}
    	}
		return isMyChar;
	}

	public static CharSequence zawGyiDrawFix(CharSequence input) {
		return zawGyiDrawFix(input, 0xEA00);
	}
		
	public static CharSequence zawGyiDrawFix(CharSequence input, int fixCode) {
		if (fixCode== 0x0) return input;
		String output = input.toString();
		int index = 0;
		char[] chArray = new char[output.length()];
		for(int i = 0; i < output.length(); i++) {
			int ch = (int)output.charAt(i);
			if((ch != NULL_CHAR) && (isMyChar(ch))) {
				chArray[index++] = (char)(ch + fixCode); // 0xEA00
			}
			else {
				chArray[index++] = (char)ch;
			}
    	}
		return String.valueOf(chArray);
	}
	
	public static boolean isZawgyi(CharSequence input) {
		return patternZawGyi.matcher(input).find();
	}
	
	public static CharSequence uniToZawgyi(CharSequence input) {
		CharSequence output = input;
		final String[] strPatterns = patternsUniToZawGyi;
		final int patCount = strPatterns.length / 2;
		for (int i = 0; i < patCount; i++) {
			final int idx = i * 2;
			output = RegexReplace(output, strPatterns[idx], strPatterns[(idx + 1)]);
		}
		return output;
	}
	
	public static CharSequence zawgyiToUni(CharSequence input) {
		CharSequence output = input;
		final String[] strPatterns = patternsZawGyiToUni;
		final int patCount = strPatterns.length / 2;
		for (int i = 0; i < patCount; i++) {
			final int idx = i * 2;
			output = RegexReplace(output, strPatterns[idx], strPatterns[(idx + 1)]);
		}
		return output;
	}
}
