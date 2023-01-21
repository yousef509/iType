package com.example.iType;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


public class Seq2WordsEngine {

    public static HashMap<String, Queue<Word>> Seq2wordsDictionary = new LinkedHashMap<String, Queue<Word>>();
    private static AssetManager assetManager;
    Seq2WordsEngine(AssetManager assetManager) {
        this.assetManager = assetManager;
        prepareDict();
    }


    public static Queue<Word> getResponse(String query) {
        Queue<Word> WordQueue = null;
        try {
            WordQueue = getWordQueue(query);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return WordQueue;
    }

    private static Queue<Word> getWordQueue(String query) throws Exception {
        if (!Seq2wordsDictionary.containsKey(query)) {
            Set<String> Keys = Seq2wordsDictionary.keySet();
            for (String eachKey : Keys) {
                if (eachKey.startsWith(query)) {
                    System.out.println("in loop: "+eachKey);
                    return (Seq2wordsDictionary.get(eachKey));
                }
            }
            throw (new Exception(query) );
        }
        return Seq2wordsDictionary.get(query);
    }

    public static void prepareDict() {
        InputStream inputStream ;

        try {
            inputStream = assetManager.open("13.txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String strLine;
            String sequence;
            while ((strLine = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(strLine);
                while(st.hasMoreTokens()) {
                    String word = st.nextToken();
                    sequence = generateSeq(word.toLowerCase());
                    insert(sequence, word.toLowerCase());
                }
            }
            inputStream.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static String generateSeq(String word) {
        StringBuilder sequence = new StringBuilder();
        for (char c : word.toCharArray())
            sequence.append(getDigit(c));
        return sequence.toString();
    }


    public static void insert(String sequence, String word) {
        if (Seq2wordsDictionary.containsKey(sequence)) {

            Queue<Word> wordQueue = Seq2wordsDictionary.get(sequence);
            if (wordQueue.contains(new Word(word))) {
                Word toUpdate = removeWord(wordQueue, new Word(word));

                toUpdate.setFrequency(toUpdate.getFrequency() + 1);
                wordQueue.add(toUpdate);
            } else {
                Word ne = new Word(word);
                wordQueue.add(ne);

            }
        } else {
            Queue<Word> wordQueue = new PriorityQueue<Word>(1,
                    new Comparator<Word>() {

                        public int compare(Word o1, Word o) {
                            if (o1.word == o.word) {
                                return 0;
                            } else if (o1.frequency < o.frequency) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }

                    });
            wordQueue.add(new Word(word));
            Seq2wordsDictionary.put(sequence, (Queue<Word>) wordQueue);
        }
    }

    private static Word removeWord(Queue<Word> wordQueue, Word word) {
        for (Word eachWordIn : wordQueue) {
            if (eachWordIn.equals(word)) {
                wordQueue.remove(eachWordIn);
                return eachWordIn;
            }
        }
        return null;
    }

    public static char getDigit(char alphabet) {

        switch (alphabet) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return '1';
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
                return '2';
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
                return '3';
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                return '4';
            default:
                return '0';
        }
    }
}
