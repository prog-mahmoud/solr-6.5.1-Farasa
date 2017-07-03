/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.ar;



import static org.apache.lucene.analysis.util.StemmerUtil.*;
import com.qcri.farasa.segmenter.Farasa;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintStream;

/**
 *  Stemmer for Arabic.
 *  <p>
 *  Stemming  is done in-place for efficiency, operating on a termbuffer.
 *  <p>
 *  Stemming is defined as:
 *  <ul>
 *  <li> Removal of attached definite article, conjunction, and prepositions.
 *  <li> Stemming of common suffixes.
 * </ul>
 *
 */
public class ArabicStemmer {
  public static final char ALEF = '\u0627';
  public static final char BEH = '\u0628';
  public static final char TEH_MARBUTA = '\u0629';
  public static final char TEH = '\u062A';
  public static final char FEH = '\u0641';
  public static final char KAF = '\u0643';
  public static final char LAM = '\u0644';
  public static final char NOON = '\u0646';
  public static final char HEH = '\u0647';
  public static final char WAW = '\u0648';
  public static final char YEH = '\u064A';

  public static Farasa farasaSegmenter = null;

  public static final char prefixes[][] = {
      ("" + ALEF + LAM).toCharArray(), 
      ("" + WAW + ALEF + LAM).toCharArray(), 
      ("" + BEH + ALEF + LAM).toCharArray(),
      ("" + KAF + ALEF + LAM).toCharArray(),
      ("" + FEH + ALEF + LAM).toCharArray(),
      ("" + LAM + LAM).toCharArray(),
      ("" + WAW).toCharArray(),
  };
  
  public static final char suffixes[][] = {
    ("" + HEH + ALEF).toCharArray(), 
    ("" + ALEF + NOON).toCharArray(), 
    ("" + ALEF + TEH).toCharArray(), 
    ("" + WAW + NOON).toCharArray(), 
    ("" + YEH + NOON).toCharArray(), 
    ("" + YEH + HEH).toCharArray(),
    ("" + YEH + TEH_MARBUTA).toCharArray(),
    ("" + HEH).toCharArray(),
    ("" + TEH_MARBUTA).toCharArray(),
    ("" + YEH).toCharArray(),
};

   /**
   * Load Farasa Segmenter.
   * set the static variable 
   */

    public void ArabicStemmerLoadFarasa() {
      try {
      
        System.err.print("1Initializing the system ....");

        farasaSegmenter = new Farasa();
       
        System.err.print("\r");
        System.err.println("1System ready!               ");

      } catch (IOException | ClassNotFoundException ex) {
        // default set should always be present as it is part of the
        // distribution (JAR)
        throw new RuntimeException("1Unable to load load Farasa!");

      }
    
  }

  /**
   * Stem an input buffer of Arabic text.
   * 
   * @param s input buffer
   * @param len length of input buffer
   * @return length of input buffer after normalization
   */
  public int stem(char s[], int len) {
    
    if(farasaSegmenter == null) {
       ArabicStemmerLoadFarasa();
    }
    String s2 = String.valueOf(s);
    ArrayList<String> lemmas = farasaSegmenter.lemmatizeLine(s2);
    char s3[] = lemmas.get(0).toCharArray();
    len = lemmas.get(0).length();

    for(int i=0;i<len;i++) 
        s[i] = s3[i];
    return len;
  }
  
  /**
   * Stem a prefix off an Arabic word.
   * @param s input buffer
   * @param len length of input buffer
   * @return new length of input buffer after stemming.
   */
  public int stemPrefix(char s[], int len) {
    for (int i = 0; i < prefixes.length; i++) 
      if (startsWithCheckLength(s, len, prefixes[i]))
        return deleteN(s, 0, len, prefixes[i].length);
    return len;
  }

  /**
   * Stem suffix(es) off an Arabic word.
   * @param s input buffer
   * @param len length of input buffer
   * @return new length of input buffer after stemming
   */
  public int stemSuffix(char s[], int len) {
    for (int i = 0; i < suffixes.length; i++) 
      if (endsWithCheckLength(s, len, suffixes[i]))
        len = deleteN(s, len - suffixes[i].length, len, suffixes[i].length);
    return len;
  }
  
  /**
   * Returns true if the prefix matches and can be stemmed
   * @param s input buffer
   * @param len length of input buffer
   * @param prefix prefix to check
   * @return true if the prefix matches and can be stemmed
   */
  boolean startsWithCheckLength(char s[], int len, char prefix[]) {
    if (prefix.length == 1 && len < 4) { // wa- prefix requires at least 3 characters
      return false;
    } else if (len < prefix.length + 2) { // other prefixes require only 2.
      return false;
    } else {
      for (int i = 0; i < prefix.length; i++)
        if (s[i] != prefix[i])
          return false;
        
      return true;
    }
  }
  
  /**
   * Returns true if the suffix matches and can be stemmed
   * @param s input buffer
   * @param len length of input buffer
   * @param suffix suffix to check
   * @return true if the suffix matches and can be stemmed
   */
  boolean endsWithCheckLength(char s[], int len, char suffix[]) {
    if (len < suffix.length + 2) { // all suffixes require at least 2 characters after stemming
      return false;
    } else {
      for (int i = 0; i < suffix.length; i++)
        if (s[len - suffix.length + i] != suffix[i])
          return false;
        
      return true;
    }
  }  
}
